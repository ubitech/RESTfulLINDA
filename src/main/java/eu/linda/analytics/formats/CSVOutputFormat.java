/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.utils.Util;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author eleni
 */
public class CSVOutputFormat extends OutputFormat {

    ConnectionController connectionController;

    public CSVOutputFormat() {
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {
        if (dataToExport.size() != 0) {
            float timeToExportData = 0;
            long startTimeToExportData = System.currentTimeMillis();

            Util.nicePrintMessage("Export to CSV");
            String[] splitedSourceFileName = analytics.getDocument().split("\\.");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");

            String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;
            Util.saveFile(targetFileNameFullPath, dataToExport.toString());

            String targetFileNameCSV = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument." + analytics.getExportFormat()).replace("datasets", "results");
            String targetFileNameCSVFull = Configuration.analyticsRepo + targetFileNameCSV;

            Util.saveFileAsCSV(targetFileNameFullPath, targetFileNameCSVFull);
            DBSynchronizer.updateLindaAnalytics(targetFileNameCSV, "resultdocument", analytics.getId());
            DBSynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

            // Get elapsed time in milliseconds
            long elapsedTimeToExportData = System.currentTimeMillis() - startTimeToExportData;
            // Get elapsed time in seconds
            timeToExportData = elapsedTimeToExportData / 1000F;
            System.out.println("timeToExportData" + timeToExportData);
            analytics.setTimeToCreate_RDF(timeToExportData);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);

        } else {
            Util.nicePrintMessage("There are no data to be exported to CSV");
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
            
            Util.nicePrintMessage("Export to CSV");
            String[] splitedSourceFileName = analytics.getDocument().split("\\.");
            
            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.csv").replace("datasets", "results");
            
            String targetFileNameFullPath = Configuration.analyticsRepo + targetFileName;
            System.out.println("targetFileNameFullPath" + targetFileNameFullPath);
            re.eval("write.table(df_to_export, file = '" + targetFileNameFullPath + "',row.names=FALSE,sep = ';', dec='.');");
            re.eval("rm(list=ls());");
            
           DBSynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
            DBSynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());
            
            // Get elapsed time in milliseconds
            long elapsedTimeToExportData = System.currentTimeMillis() - startTimeToExportData;
            // Get elapsed time in seconds
            timeToExportData = elapsedTimeToExportData / 1000F;
            System.out.println("timeToExportData" + timeToExportData);
            analytics.setTimeToCreate_RDF(timeToExportData);
            DBSynchronizer.updateLindaAnalyticsProcessPerformanceTime(analytics);
        } catch (RserveException ex) {
            Logger.getLogger(CSVOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
