/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.formats.CSVOutputFormat;
import eu.linda.analytics.model.Analytics;
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

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream("RESTfulLINDA.properties");
            try {
                prop.load(input);
            } catch (IOException ex) {
                Logger.getLogger(CSVOutputFormat.class.getName()).log(Level.SEVERE, null, ex);
            }

            // load a properties file
            Configuration.dbport = Integer.parseInt(prop.getProperty("dbport").trim());
            Configuration.dbip = prop.getProperty("dbip").trim();
            Configuration.username = prop.getProperty("username").trim();
            Configuration.password = prop.getProperty("password").trim();
            Configuration.dbname = prop.getProperty("dbname").trim();
            Configuration.rdf2anyServer = prop.getProperty("rdf2anyServer").trim();
            Configuration.docroot = prop.getProperty("docroot").trim();
            Configuration.lindaworkbenchURI = prop.getProperty("lindaworkbenchURI").trim();
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

