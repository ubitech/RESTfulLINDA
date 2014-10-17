/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.util.AbstractList;

/**
 *
 * @author eleni
 */
public class ArffOutputFormat extends OutputFormat {

    DBSynchronizer dbsynchronizer;
    HelpfulFunctions helpfulFuncions;

    public ArffOutputFormat() {
        dbsynchronizer = new DBSynchronizer();
        helpfulFuncions = new HelpfulFunctions();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {

        if (dataToExport.size() != 0) {

            helpfulFuncions.nicePrintMessage("Export to Arff");

            String[] splitedSourceFileName = analytics.getDocument().split(".arff");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.docroot + targetFileName;

            helpfulFuncions.saveFile(targetFileNameFullPath, dataToExport.toString());
            dbsynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
            dbsynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

        } else {
            helpfulFuncions.nicePrintMessage("There are no data to be exported to Arff");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                helpfulFuncions.cleanPreviousInfo(analytics);
            }

        }
    }

}
