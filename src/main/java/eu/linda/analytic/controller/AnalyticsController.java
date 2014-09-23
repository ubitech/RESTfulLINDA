/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytic.controller;

import eu.linda.analytics.db.ConnectionController;
import eu.linda.analytics.db.DBSynchronizer;
import eu.linda.analytics.model.Analytics;
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
    
    public AnalyticsInfo runAnalytics(String algorithm,String ouputformat){
   
        AnalyticProcess ap;
        OutputFormat out;
        
        AnalyticsInfo info = factory.createAnalytics(algorithm,ouputformat);
        
        ap = info.getAnalyticProcess();
        out = info.getOutputformat();
        
        ap.train(analytics);
        String resultToExport = ap.eval(analytics);
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
