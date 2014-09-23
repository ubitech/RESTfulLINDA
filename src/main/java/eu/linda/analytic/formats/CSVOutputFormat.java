/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytic.formats;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFuncions;
import org.json.JSONArray;

/**
 *
 * @author eleni
 */
public class CSVOutputFormat extends OutputFormat {

    DBSynchronizer dbsynchronizer;
    HelpfulFuncions helpfulFuncions;

    public CSVOutputFormat() {
        dbsynchronizer = new DBSynchronizer();
        helpfulFuncions = new HelpfulFuncions();
    }

    @Override
    public void exportData(Analytics analytics, String dataToExport) {
   
        helpfulFuncions.nicePrintMessage("Export to CSV");
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");

        String targetFileNameFullPath = Configuration.docroot + targetFileName;
        helpfulFuncions.saveFile(targetFileNameFullPath, dataToExport.toString());

        String targetFileNameCSV = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument." + analytics.getExportFormat()).replace("datasets", "results");
        String targetFileNameCSVFull = Configuration.docroot + targetFileNameCSV;

        helpfulFuncions.saveFileAsCSV(targetFileNameFullPath, targetFileNameCSVFull);
        dbsynchronizer.updateLindaAnalytics(targetFileNameCSV, "resultdocument", analytics.getId());

    }

}
