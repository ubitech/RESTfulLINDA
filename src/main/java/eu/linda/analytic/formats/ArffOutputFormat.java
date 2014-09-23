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

/**
 *
 * @author eleni
 */
public class ArffOutputFormat extends OutputFormat {

    DBSynchronizer dbsynchronizer;
    HelpfulFuncions helpfulFuncions;

    public ArffOutputFormat() {
        dbsynchronizer = new DBSynchronizer();
        helpfulFuncions = new HelpfulFuncions();
    }

    @Override
    public void exportData(Analytics analytics, String dataToExport) {
   
        helpfulFuncions.nicePrintMessage("Export to Arff");
        
        String[] splitedSourceFileName = analytics.getDocument().split(".arff");

        String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");
        String targetFileNameFullPath = Configuration.docroot + targetFileName;

        helpfulFuncions.saveFile(targetFileNameFullPath, dataToExport);
        dbsynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());

    }

}
