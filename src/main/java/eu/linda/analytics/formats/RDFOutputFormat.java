/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import com.hp.hpl.jena.rdf.model.Model;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.controller.RDFGenerationFactory;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */
public class RDFOutputFormat extends OutputFormat {

    DBSynchronizer dbsynchronizer;
    HelpfulFunctionsSingleton helpfulFuncions;
    RDFGenerationFactory rdfGenerationFactory;
    RDFGenerator rdfGenerator;

    public RDFOutputFormat() {
        dbsynchronizer = new DBSynchronizer();
        helpfulFuncions = HelpfulFunctionsSingleton.getInstance();
        rdfGenerationFactory = new RDFGenerationFactory();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {
        if (dataToExport.size() != 0) {

            helpfulFuncions.nicePrintMessage("Export to RDF");

            //save rdf file
            String[] splitedSourceFileName = analytics.getDocument().split("\\.");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.docroot + targetFileName;
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

                } else if (analytics.getExportFormat().equalsIgnoreCase("NTRIPLES")) {

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

            dbsynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
            dbsynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());
            dbsynchronizer.updateLindaAnalyticsRDFInfo("", false, analytics.getId());

        } else {
            helpfulFuncions.nicePrintMessage("There are no data to be exported to RDF");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                helpfulFuncions.cleanPreviousInfo(analytics);
            }
        }

    }

    @Override
    public void exportData(Analytics analytics, Rengine re) {
//    if (dataToExport.size() != 0) {

        helpfulFuncions.nicePrintMessage("Export to RDF");

        //save rdf file
        String[] splitedSourceFileName = analytics.getDocument().split("\\.");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument").replace("datasets", "results");
        String targetFileNameFullPath = Configuration.docroot + targetFileName;
        //create rdf file & save

        rdfGenerator = rdfGenerationFactory.createRDF(analytics.getCategory_id());

        Model model = rdfGenerator.generateRDFModel(analytics, re);

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

            } else if (analytics.getExportFormat().equalsIgnoreCase("NTRIPLES")) {

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

        dbsynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
        dbsynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());
        dbsynchronizer.updateLindaAnalyticsRDFInfo("", false, analytics.getId());

        //   }
//    else {
//            helpfulFuncions.nicePrintMessage("There are no data to be exported to RDF");
//            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
//                helpfulFuncions.cleanPreviousInfo(analytics);
//            }
    }

}
