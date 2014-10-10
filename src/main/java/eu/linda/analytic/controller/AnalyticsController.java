/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytic.controller;

import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytic.formats.OutputFormat;
import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
import java.util.AbstractList;
import org.json.JSONArray;

/**
 *
 * @author eleni
 */
public class AnalyticsController {

    
    AnalyticsFactory factory;
    Analytics analytics;
    
    public AnalyticsController(AnalyticsFactory factory) {
        this.factory = factory;
    }
    
    public AnalyticsInfo runAnalytics(String inputformat, String algorithm,String ouputformat){
   
        InputFormat in;
        AnalyticProcess ap;
        OutputFormat out;
        
        AnalyticsInfo info = factory.createAnalytics(inputformat,algorithm,ouputformat);
        
        in = info.getInputformat();
        ap = info.getAnalyticProcess();
        out = info.getOutputformat();
        
        ap.train(analytics);
        AbstractList resultToExport = ap.eval(analytics);
        out.exportData(analytics,resultToExport);
        
        return info;
        
    }
    
    public Analytics connectToAnalyticsTable(int id) {
        ConnectionController connectionController = new ConnectionController();
        connectionController.readProperties();
        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        Analytics analytics = dbsynchronizer.getlindaAnalytics_analytics(id);
        this.analytics = analytics;
        return analytics;
    }
    
}
