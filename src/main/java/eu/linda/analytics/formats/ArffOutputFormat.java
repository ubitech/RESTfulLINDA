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
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.Rserve.RConnection;


/**
 *
 * @author eleni
 */
public class ArffOutputFormat extends OutputFormat {

    ConnectionController connectionController;
    Util helpfulFuncions;

    public ArffOutputFormat() {
        connectionController = ConnectionController.getInstance();
        helpfulFuncions = Util.getInstance();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {

        if (dataToExport.size() != 0) {

            helpfulFuncions.nicePrintMessage("Export to Arff");

            String[] splitedSourceFileName = analytics.getDocument().split(".arff");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.arff").replace("datasets", "results");
            String targetFileNameFullPath = Configuration.docroot + targetFileName;

            helpfulFuncions.saveFile(targetFileNameFullPath, dataToExport.toString());
            DBSynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
            DBSynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

        } else {
            helpfulFuncions.nicePrintMessage("There are no data to be exported to Arff");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                helpfulFuncions.cleanPreviousInfo(analytics);
            }

        }
    }

    @Override
    public void exportData(Analytics analytics, Rengine dataToExport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void exportData(Analytics analytics, RConnection dataToExport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



}
