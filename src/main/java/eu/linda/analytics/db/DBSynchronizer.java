package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.model.Query;
import eu.linda.analytics.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

public class DBSynchronizer {

    public DBSynchronizer() {
    }

    /*
     * Fetch analytics_analytics by id
     */
    public static synchronized Analytics getlindaAnalytics_analytics(int id) {
        Analytics analytics = null;
        PreparedStatement preparedStatement = null;
        try {
            String query = "SELECT  analytics.*,alg.*,user.username FROM analytics_analytics  AS analytics, analytics_algorithm AS alg, auth_user as user WHERE analytics.id =? AND analytics.algorithm_id = alg.id AND user.id =analytics.user_id";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                analytics = new Analytics(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getInt("algorithm_id"),
                        rs.getString("document"),
                        rs.getString("testdocument"),
                        rs.getInt("trainQuery_id"),
                        rs.getInt("evaluationQuery_id"),
                        rs.getString("model"),
                        rs.getString("modelReadable"),
                        rs.getString("processinfo"),
                        rs.getString("resultdocument"),
                        rs.getString("exportFormat"),
                        rs.getInt("version"),
                        rs.getString("description"),
                        rs.getBoolean("publishedToTriplestore"),
                        rs.getString("loadedRDFContext"),
                        rs.getString("parameters"),
                        rs.getInt("plot1_id"),
                        rs.getInt("plot2_id"),
                        rs.getString("username"),
                        rs.getFloat("timeToGet_data"),
                        rs.getFloat("data_size"),
                        rs.getFloat("timeToRun_analytics"),
                        rs.getFloat("timeToCreate_RDF"),
                        rs.getBoolean("createModel")
                );
                analytics.setAlgorithm_name(rs.getString("name"));
                rs.close();
                preparedStatement.close();
                connection.close();
                break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return analytics;
    }//EoM  

    /*
     * Fetch linda_app_query by id
     */
    public static synchronized String getQueryURI(int id) {
        Query query = null;
        PreparedStatement preparedStatement = null;
        String query_uri = "";
        try {

            String querytodb = "SELECT  * FROM linda_app_query  AS query WHERE query.id =? ";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(querytodb);

            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                query = new Query(
                        rs.getInt("id"),
                        rs.getString("endpoint"),
                        rs.getString("sparql"),
                        rs.getString("description")
                );
                rs.close();
                preparedStatement.close();
                connection.close();
                break;
            }
            query_uri = Configuration.rdf2anyServer + "/rdf2any/v1.0/convert/csv-converter.csv?dataset=" + query.getEndpoint() + "&query=" + URIUtil.encodeQuery(query.getSparql());

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URIException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return query_uri;
    }//EoM  

    /*
     * Update LINDA Analytics with result file
     */
    public static synchronized void updateLindaAnalytics(String resultPath, String column, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set " + column + "=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalytics   

    /*
     * Updates LindaAnalyticsModel after Train algorithm
     */
    public static synchronized void updateLindaAnalyticsModel(String resultPath, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set model=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsModel   

    /*
     * Updates LindaAnalyticsModel verbose file after Train algorithm
     */
    public static synchronized void updateLindaAnalyticsModelReadable(String resultPath, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set modelReadable=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsModelReadable   

    /*
     * Updates LindaAnalyticsVersion 
     */
    public static synchronized void updateLindaAnalyticsVersion(int version, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set version=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, version + 1);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsVersion 

    /*
     * Updates LindaAnalyticsRDFInfo 
     */
    public static synchronized void updateLindaAnalyticsRDFInfo(String rdfContextURL, boolean publishedToTriplestore, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String rdfContextInfo = "";
            if (!rdfContextURL.equalsIgnoreCase("")) {
                rdfContextInfo = "Result RDF file has been succesfully loaded to LinDA Triplestore."
                        + "You can access the rdf file at : <a href=" + rdfContextURL + ">" + rdfContextURL + "</a> ."
                        + "You can submit a new query to the consumption application "
                        + "in order to explore the rdf output in compination with the rest data of triplestore";

            }

            String query = "update analytics_analytics set loadedRDFContext=?, publishedToTriplestore=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, rdfContextInfo);
            preparedStatement.setBoolean(2, publishedToTriplestore);
            preparedStatement.setInt(3, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsRDFInfo 

    /*
     * Updates LindaAnalyticsProcessMessage 
     */
    public static synchronized void updateLindaAnalyticsProcessMessage(String message, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set processMessage=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, message);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsProcessMessage 

    /*
     * Updates LindaAnalyticsResultInfo
     */
    public static synchronized void emptyLindaAnalyticsResultInfo(int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set processinfo=? , resultdocument=? , processMessage=?, model=?, modelReadable=?, plot1_id=null , plot2_id=null, timeToGet_data=0 , data_size=0 , timeToRun_analytics=0 , timeToCreate_RDF=0 where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, "");
            preparedStatement.setString(3, "");
            preparedStatement.setString(4, "");
            preparedStatement.setString(5, "");
            preparedStatement.setInt(6, analytics_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM emptyLindaAnalyticsResultDocument 

    /*
     * Updates updateLindaAnalyticsPerformanceTime
     */
    public static synchronized void updateLindaAnalyticsProcessPerformanceTime(Analytics analytics) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set  timeToRun_analytics=? , timeToCreate_RDF=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setFloat(1, analytics.getTimeToRun_analytics());
            preparedStatement.setFloat(2, analytics.getTimeToCreate_RDF());
            preparedStatement.setInt(3, analytics.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM updateLindaAnalyticsPerformanceTime 

    /*
     * Updates updateLindaAnalyticsPerformanceTime
     */
    public static synchronized void updateLindaAnalyticsInputDataPerformanceTime(Analytics analytics) {
        PreparedStatement preparedStatement = null;
        try {

            String query = "update analytics_analytics set timeToGet_data=? , data_size=?  where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setFloat(1, analytics.getTimeToGet_data());
            preparedStatement.setFloat(2, analytics.getData_size());
            preparedStatement.setInt(3, analytics.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM updateLindaAnalyticsPerformanceTime 

    /*
     * Add LindaAnalyticsPlot 
     */
    public static synchronized long addPlot(String description, String image) {
        PreparedStatement preparedStatement = null;
        long plot_id = 0;
        try {
            String query = "INSERT INTO analytics_plot (description, image) VALUES (?, ?)";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, description);
            preparedStatement.setString(2, image);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                plot_id = rs.getLong(1);
            }

            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

        return plot_id;
    }//EoM Add LindaAnalyticsPlot

    /*
     * updatePlot  
     */
    public static synchronized long updatePlot(int plot_id, String image) {
        PreparedStatement preparedStatement = null;

        try {
            String query = "update analytics_plot  set image=? where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, image);
            preparedStatement.setInt(2, plot_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

        return plot_id;
    }//EoM updatePlot

    /*
     * Updates updateLindaAnalyticsPlot
     */
    public static synchronized void updateLindaAnalyticsPlot(int analytics_id, long plot_id, String plot) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set " + plot + "=?  where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, (int) plot_id);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM updateLindaAnalyticsPlot 

    /*
     * Updates updateLindaAnalyticsPlotToNull
     */
    public static synchronized void updateLindaAnalyticsPlotToNull(int analytics_id, String plot) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set " + plot + "=null  where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, analytics_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM updateLindaAnalyticsPlotToNull 

    /*
     * delete old plot Plot
     */
    public static synchronized void deletePlot(int plot_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "delete from analytics_plot  where id=?";
            Connection connection = ConnectionFactory.getInstance();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, (int) plot_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM delete plot

}//EoC
