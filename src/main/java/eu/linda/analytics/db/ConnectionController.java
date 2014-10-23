/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.logger.LoggerFactory;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionController {

    //private final static Logger LOGGER = LoggerFactory.getLogger();
    private final Properties prop = new Properties();
    private InputStream input = null;

    public Analytics connectToAnalyticsTable(int id) {
        //ConnectionController connectionController = new ConnectionController();
        this.readProperties();
        DBSynchronizer dbsynchronizer = new DBSynchronizer();
        Analytics analytics = dbsynchronizer.getlindaAnalytics_analytics(id);
        return analytics;
    }


    public void readProperties() {
        try {
            String path = new java.io.File(".").getCanonicalPath();
            //LOGGER.log(Level.INFO, "PATH:{0}", path);

            input = this.getClass().getResourceAsStream("/RESTfulLINDA.properties");

            // load a properties file
            prop.load(input);
            Configuration.dbport = Integer.parseInt(prop.getProperty("dbport").trim());
            Configuration.dbip = prop.getProperty("dbip").trim();
            Configuration.username = prop.getProperty("username").trim();
            Configuration.password = prop.getProperty("password").trim();
            Configuration.dbname = prop.getProperty("dbname").trim();
            Configuration.docroot = prop.getProperty("docroot").trim();
            Configuration.lindaworkbenchURI = prop.getProperty("lindaworkbenchURI").trim();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionController.class.getName()).log(Level.SEVERE, null, ex);
                }
               
            }
        }
    }//EoM readproperties  

}//EoC

