package eu.linda.analytics.formats;

import com.hp.hpl.jena.rdf.model.Model;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.RDFGenerationFactory;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class RDFOutputFormat extends OutputFormat {

    RDFGenerationFactory rdfGenerationFactory;
    RDFGenerator rdfGenerator;
    ConnectionController connectionController;

    public RDFOutputFormat() {
        super();
        connectionController = ConnectionController.getInstance();
        rdfGenerationFactory = new RDFGenerationFactory();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {
        if (dataToExport.size() != 0) {
            float timeToExportData = 0;
            long startTimeToExportData = System.currentTimeMillis();

            Instances triplets = (Instances) dataToExport;

            if (!Util.isURLValid(triplets.get(1).toString(0))) {
                DBSynchronizer.updateLindaAnalyticsProcessMessage("There is no valid URL as analytics input  node. \n So no RDF was created.\n"
                        + "Please select a different output format. ", analytics.getId());
                return;
            }

            Util.nicePrintMessage("Export to RDF");

            //save rdf file
            String targetFileName = ("results/analyticsID" + analytics.getId() + "_" + analytics.getAlgorithm_name() + "_resultdocument").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;

            //create rdf file & save
            rdfGenerator = rdfGenerationFactory.createRDF(analytics.getCategory_id());

            Model model = rdfGenerator.generateRDFModel(analytics, dataToExport);
            String fileName = "";
            try {

                FileWriter outToSave = null;
                if (analytics.getExportFormat().equalsIgnoreCase("RDFXML")) {

                    fileName = targetFileNameFullPath + ".rdf";
                    targetFileName = targetFileName + ".rdf";
                    outToSave = new FileWriter(fileName);
                    model.write(outToSave, "RDF/XML-ABBREV");

                } else if (analytics.getExportFormat().equalsIgnoreCase("TTL")) {

                    fileName = targetFileNameFullPath + ".ttl";
                    targetFileName = targetFileName + ".ttl";
                    outToSave = new FileWriter(fileName);
                    model.write(outToSave, "TTL");

                } else if (analytics.getExportFormat().equalsIgnoreCase("N-Tripples")) {

                    fileName = targetFileNameFullPath + ".nt";
                    targetFileName = targetFileName + ".nt";
                    outToSave = new FileWriter(fileName);
                    model.write(outToSave, "N3");

                }
                outToSave.close();
                System.out.println("RDF File save to:" + fileName);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeneralRDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GeneralRDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

            DBSynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
            DBSynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());
            DBSynchronizer.updateLindaAnalyticsRDFInfo("", false, analytics.getId());

            // Get elapsed time in milliseconds
            long elapsedTimeToExportData = System.currentTimeMillis() - startTimeToExportData;
            // Get elapsed time in seconds
            timeToExportData = elapsedTimeToExportData / 1000F;
            System.out.println("timeToExportData" + timeToExportData);
            analytics.setTimeToCreate_RDF(timeToExportData);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);

        } else {
            Util.nicePrintMessage("There are no data to be exported to RDF");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                Util.cleanPreviousInfo(analytics);
            }
        }

    }

    @Override
    public void exportData(Analytics analytics, RConnection re) {

        try {

            float timeToExportData = 0;
            long startTimeToExportData = System.currentTimeMillis();

            org.rosuda.REngine.REXP uriAsCharacter = re.eval("as.character(loaded_data$uri)");
            String[] urisAsStringArray = uriAsCharacter.asStrings();

            if (urisAsStringArray.length != 0) {

                if (!Util.isURLValid(urisAsStringArray[0])) {
                    DBSynchronizer.updateLindaAnalyticsProcessMessage("There is no valid URL as analytics input  node. \n So no RDF was created.\n"
                            + "Please select a different output format. ", analytics.getId());
                    return;
                }

            }

            Util.nicePrintMessage("Export to RDF");
            String targetFileName = "results/analyticsID" + analytics.getId() + "_" + "version" + analytics.getVersion() + "_" + analytics.getAlgorithm_name() + "_resultdocument";
            String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;

            //create rdf file & save
            rdfGenerator = rdfGenerationFactory.createRDF(analytics.getCategory_id());

            Model model = rdfGenerator.generateRDFModel(analytics, re);

            re.close();

            String fileName = "";

            FileWriter outToSave = null;
            if (analytics.getExportFormat().equalsIgnoreCase("RDFXML")) {

                fileName = targetFileNameFullPath + ".rdf";
                targetFileName = targetFileName + ".rdf";
                outToSave = new FileWriter(fileName);
                model.write(outToSave, "RDF/XML-ABBREV");

            } else if (analytics.getExportFormat().equalsIgnoreCase("TTL")) {

                fileName = targetFileNameFullPath + ".ttl";
                targetFileName = targetFileName + ".ttl";
                outToSave = new FileWriter(fileName);
                model.write(outToSave, "TTL");

            } else if (analytics.getExportFormat().equalsIgnoreCase("N-Tripples")) {

                fileName = targetFileNameFullPath + ".nt";
                targetFileName = targetFileName + ".nt";
                outToSave = new FileWriter(fileName);
                model.write(outToSave, "N3");

            }
            outToSave.close();
            System.out.println("RDF File save to:" + fileName);

            DBSynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
            DBSynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());
            DBSynchronizer.updateLindaAnalyticsRDFInfo("", false, analytics.getId());

            // Get elapsed time in milliseconds
            long elapsedTimeToExportData = System.currentTimeMillis() - startTimeToExportData;
            // Get elapsed time in seconds
            timeToExportData = elapsedTimeToExportData / 1000F;
            System.out.println("timeToExportData" + timeToExportData);
            analytics.setTimeToCreate_RDF(timeToExportData);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);

        } catch (RserveException ex) {
            Logger.getLogger(RDFOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(RDFOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RDFOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
