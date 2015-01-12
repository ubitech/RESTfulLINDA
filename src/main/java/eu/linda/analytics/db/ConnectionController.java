/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.formats.CSVOutputFormat;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.model.Query;
import eu.linda.analytics.weka.utils.HelpfulFunctionsSingleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

public final class ConnectionController {

    //private final static Logger LOGGER = LoggerFactory.getLogger();
    private final Properties prop = new Properties();
    private InputStream input = null;
    DBSynchronizer dbsynchronizer;

    private static ConnectionController instance = null;

    protected ConnectionController() {
        this.readProperties();
        dbsynchronizer = new DBSynchronizer();
    }

    public void closeConnection() {
        dbsynchronizer.closeConnection();
    }

    public static ConnectionController getInstance() {
        if (instance == null) {
            instance = new ConnectionController();
        }
        return instance;
    }

    public Analytics connectToAnalyticsTable(int id) {
        dbsynchronizer.establishConnection();
        Analytics analytics = dbsynchronizer.getlindaAnalytics_analytics(id);
        return analytics;
    }

    public void updateProcessMessageToAnalyticsTable(String message, int id) {
        dbsynchronizer.updateLindaAnalyticsProcessMessage(message, id);

    }

    public void emptyLindaAnalyticsResultInfo(int analytics_id) {

        //empty the analytic result document & processMessage
        dbsynchronizer.emptyLindaAnalyticsResultInfo(analytics_id);

    }

    public String getQueryURI(String query_id) {
        String query_uri = null;

        try {
            Query linda_query = dbsynchronizer.getQueryURI(Integer.parseInt(query_id));

            System.out.println("linda_query encoded" + URIUtil.encodeQuery(linda_query.getSparql()));

            query_uri = Configuration.rdf2anyServer + "/rdf2any/v1.0/convert/csv-converter.csv?dataset=" + linda_query.getEndpoint() + "&query=" + URIUtil.encodeQuery(linda_query.getSparql());

        } catch (URIException ex) {
            Logger.getLogger(HelpfulFunctionsSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("query_uri" + query_uri);
        return query_uri;

    }

    public void updateLindaAnalyticsModel(String resultPath, int analytics_id) {
        dbsynchronizer.updateLindaAnalyticsModel(resultPath, analytics_id);
    }//EoM updateLindaAnalyticsModel   

    public void updateLindaAnalyticsModelReadable(String resultPath, int analytics_id) {
        dbsynchronizer.updateLindaAnalyticsModelReadable(resultPath, analytics_id);
    }//EoM updateLindaAnalyticsModelReadable 

    public void updateLindaAnalytics(String resultPath, String column, int analytics_id) {
        dbsynchronizer.updateLindaAnalytics(resultPath, column, analytics_id);
    }//EoM updateLindaAnalytics  

    public void updateLindaAnalyticsRDFInfo(String rdfContextURL, boolean publishedToTriplestore, int analytics_id) {
        dbsynchronizer.updateLindaAnalyticsRDFInfo(rdfContextURL, publishedToTriplestore, analytics_id);
    }//EoM updateLindaAnalyticsRDFInfo 

    public void updateLindaAnalyticsVersion(int version, int analytics_id) {
        dbsynchronizer.updateLindaAnalyticsVersion(version, analytics_id);
    }//EoM updateLindaAnalyticsVersion 

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
            Configuration.analyticsRepo = prop.getProperty("analyticsRepo").trim();
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

    public long manageNewPlot(Analytics analytics, String description, String filepath, String plot) {

        long plot_id = dbsynchronizer.addPlot(description, filepath);
        dbsynchronizer.updateLindaAnalyticsPlot(analytics.getId(), plot_id, plot);

        return plot_id;
    }//EoM addPlot 

    public void deletePlot(int plot_id) {

        dbsynchronizer.deletePlot(plot_id);

    }//EoM addPlot 

}//EoC

