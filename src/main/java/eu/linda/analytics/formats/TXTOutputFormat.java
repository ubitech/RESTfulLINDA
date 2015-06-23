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
import org.rosuda.REngine.Rserve.RConnection;


/**
 *
 * @author eleni
 */
public class TXTOutputFormat extends OutputFormat {

    ConnectionController connectionController;

    public TXTOutputFormat() {
        connectionController = ConnectionController.getInstance();
    }

    @Override
    public void exportData(Analytics analytics, AbstractList dataToExport) {
        if (dataToExport.size() != 0) {
            Util.nicePrintMessage("Export to TXT");

            String[] splitedSourceFileName = analytics.getDocument().split("\\.");

            String targetFileName = (splitedSourceFileName[0] + "_" + analytics.getAlgorithm_name() + "_resultdocument.txt").replace("datasets", "results");

            String targetFileNameFullPath = Configuration.docroot + targetFileName;

            Util.saveFile(targetFileNameFullPath, dataToExport.toString());

            DBSynchronizer.updateLindaAnalytics(targetFileName, "resultdocument", analytics.getId());
           DBSynchronizer.updateLindaAnalyticsVersion(analytics.getVersion(), analytics.getId());

        } else {
            Util.nicePrintMessage("There are no data to be exported to TXT");
            if (!analytics.getResultdocument().equalsIgnoreCase("")) {
                Util.cleanPreviousInfo(analytics);
            }
        }
    }

    
    @Override
    public void exportData(Analytics analytics, RConnection dataToExport) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
