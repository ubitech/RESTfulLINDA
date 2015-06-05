/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.AbstractList;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author eleni
 */
public class CSVOutputFormat extends OutputFormat {

    ConnectionController connectionController;
    HelpfulFunctionsSingleton helpfulFunctions;

    public CSVOutputFormat() {
        connectionController = ConnectionController.getInstance();
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {
        if (dataToExport.size() != 0) {
            float timeToExportData = 0;
            long startTimeToExportData = System.currentTimeMillis();

            helpfulFunctions.nicePrintMessage("Export to CSV");
            String[] splitedSourceFileName = analytics.getDocument().split("\\.");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");

            String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;
            helpfulFunctions.saveFile(targetFileNameFullPath, dataToExport.toString());

            String targetFileNameCSV = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument." + analytics.getExportFormat()).replace("datasets", "results");
            String targetFileNameCSVFull = Configuration.analyticsRepo + targetFileNameCSV;

            helpfulFunctions.saveFileAsCSV(targetFileNameFullPath, targetFileNameCSVFull);
            connectionController.updateLindaAnalytics(targetFileNameCSV, "resultdocument", analytics.getId());
            connectionController.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

            // Get elapsed time in milliseconds
            long elapsedTimeToExportData = System.currentTimeMillis() - startTimeToExportData;
            // Get elapsed time in seconds
            timeToExportData = elapsedTimeToExportData / 1000F;
            System.out.println("timeToExportData" + timeToExportData);
            analytics.setTimeToCreate_RDF(timeToExportData);
            connectionController.updateLindaAnalyticsProcessPerformanceTime(analytics);

        } else {
            helpfulFunctions.nicePrintMessage("There are no data to be exported to CSV");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                helpfulFunctions.cleanPreviousInfo(analytics);
            }
        }
    }

    @Override
    public void exportData(Analytics analytics, Rengine re) {
        float timeToExportData = 0;
        long startTimeToExportData = System.currentTimeMillis();

        helpfulFunctions.nicePrintMessage("Export to CSV");
        String[] splitedSourceFileName = analytics.getDocument().split("\\.");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.csv").replace("datasets", "results");

        String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;
        System.out.println("targetFileNameFullPath" + targetFileNameFullPath);
        re.eval("write.table(df_to_export, file = '" + targetFileNameFullPath + "',row.names=FALSE,sep = ';', dec='.');");
        re.eval("rm(list=ls());");

        connectionController.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
        connectionController.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

        // Get elapsed time in milliseconds
        long elapsedTimeToExportData = System.currentTimeMillis() - startTimeToExportData;
        // Get elapsed time in seconds
        timeToExportData = elapsedTimeToExportData / 1000F;
        System.out.println("timeToExportData" + timeToExportData);
        analytics.setTimeToCreate_RDF(timeToExportData);
        connectionController.updateLindaAnalyticsProcessPerformanceTime(analytics);

    }
    
    @Override
    public void exportData(Analytics analytics, RConnection dataToExport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
