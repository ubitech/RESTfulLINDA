/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.util.AbstractList;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author eleni
 */
public class CSVOutputFormat extends OutputFormat {

    DBSynchronizer dbsynchronizer;
    HelpfulFunctionsSingleton helpfulFunctions;

    public CSVOutputFormat() {
        dbsynchronizer = new DBSynchronizer();
        helpfulFunctions = HelpfulFunctionsSingleton.getInstance();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {
        if (dataToExport.size() != 0) {

            helpfulFunctions.nicePrintMessage("Export to CSV");
            String[] splitedSourceFileName = analytics.getDocument().split("\\.");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");

            String targetFileNameFullPath = Configuration.docroot + targetFileName;
            helpfulFunctions.saveFile(targetFileNameFullPath, dataToExport.toString());

            String targetFileNameCSV = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument." + analytics.getExportFormat()).replace("datasets", "results");
            String targetFileNameCSVFull = Configuration.docroot + targetFileNameCSV;

            helpfulFunctions.saveFileAsCSV(targetFileNameFullPath, targetFileNameCSVFull);
            dbsynchronizer.updateLindaAnalytics(targetFileNameCSV, "resultdocument", analytics.getId());
            dbsynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

        } else {
            helpfulFunctions.nicePrintMessage("There are no data to be exported to CSV");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                helpfulFunctions.cleanPreviousInfo(analytics);
            }
        }
    }

    @Override
    public void exportData(Analytics analytics, Rengine re) {

        helpfulFunctions.nicePrintMessage("Export to CSV");
        String[] splitedSourceFileName = analytics.getDocument().split("\\.");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.csv").replace("datasets", "results");

        String targetFileNameFullPath = Configuration.docroot + targetFileName;
        System.out.println("targetFileNameFullPath" + targetFileNameFullPath);
        re.eval("write.table(df_to_export, file = '" + targetFileNameFullPath + "',row.names=FALSE,sep = ';', dec='.');");
        re.eval("rm(list=ls());");

        dbsynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
        dbsynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

    }

}
