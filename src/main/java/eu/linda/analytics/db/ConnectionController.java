/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.formats.CSVOutputFormat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionController {

    private final Properties prop = new Properties();

    DBSynchronizer dbsynchronizer;

    private static ConnectionController instance = null;

    protected ConnectionController() {
        readProperties();
        dbsynchronizer = new DBSynchronizer();
    }

    public static ConnectionController getInstance() {
        if (instance == null) {
            instance = new ConnectionController();
        }
        return instance;
    }

    public void readProperties() {
        try {
            InputStream input;
            
            input = this.getClass().getClassLoader().getResourceAsStream("RESTfulLINDA.properties");
            
            prop.load(input);
            
            
            // load a properties file
            Configuration.dbport = Integer.parseInt(prop.getProperty("dbport").trim());
            Configuration.dbip = prop.getProperty("dbip").trim();
            Configuration.username = prop.getProperty("username").trim();
            Configuration.password = prop.getProperty("password").trim();
            Configuration.dbname = prop.getProperty("dbname").trim();
            Configuration.rdf2anyServer = prop.getProperty("rdf2anyServer").trim();
            Configuration.docroot = prop.getProperty("docroot").trim();
            Configuration.lindaworkbenchURI = prop.getProperty("lindaworkbenchURI").trim();
            Configuration.analyticsRepo = prop.getProperty("analyticsRepo").trim();
            
            input.close();
        } //EoM readproperties
        catch (IOException ex) {
            Logger.getLogger(ConnectionController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}//EoC

