package eu.linda.analytics.db;

import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.model.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Types.NULL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBSynchronizer {

    Connection connection;

    public DBSynchronizer() {
        connection = ConnectionFactory.getInstance();
    }//Constructor

    public void establishConnection() {
        connection = ConnectionFactory.getInstance();
    }

    public void closeConnection() {
        try {

            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = null;
        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Fetch analytics_analytics by id
     */
    public Analytics getlindaAnalytics_analytics(int id) {
        Analytics analytics = null;
        PreparedStatement preparedStatement = null;
        try {

            String query = "SELECT  analytics.*,alg.*,user.username FROM analytics_analytics  AS analytics, analytics_algorithm AS alg, auth_user as user WHERE analytics.id =? AND analytics.algorithm_id = alg.id AND user.id =analytics.user_id";
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
                        rs.getString("username")
                );
                analytics.setAlgorithm_name(rs.getString("name"));
                rs.close();
                preparedStatement.close();
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
    public Query getQueryURI(int id) {
        Query query = null;
        PreparedStatement preparedStatement = null;
        try {

            String querytodb = "SELECT  * FROM linda_app_query  AS query WHERE query.id =? ";
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
                break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return query;
    }//EoM  

    /*
     * Update LINDA Analytics with result file
     */
    public void updateLindaAnalytics(String resultPath, String column, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set " + column + "=? where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalytics   

    /*
     * Updates LindaAnalyticsModel after Train algorithm
     */
    public void updateLindaAnalyticsModel(String resultPath, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set model=? where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsModel   

    /*
     * Updates LindaAnalyticsModel verbose file after Train algorithm
     */
    public void updateLindaAnalyticsModelReadable(String resultPath, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set modelReadable=? where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsModelReadable   

    /*
     * Updates LindaAnalyticsVersion 
     */
    public void updateLindaAnalyticsVersion(int version, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {
            String query = "update analytics_analytics set version=? where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, version + 1);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsVersion 

    /*
     * Updates LindaAnalyticsRDFInfo 
     */
    public void updateLindaAnalyticsRDFInfo(String rdfContextURL, boolean publishedToTriplestore, int analytics_id) {
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
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, rdfContextInfo);
            preparedStatement.setBoolean(2, publishedToTriplestore);
            preparedStatement.setInt(3, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsRDFInfo 

    /*
     * Updates LindaAnalyticsProcessMessage 
     */
    public void updateLindaAnalyticsProcessMessage(String message, int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {

            String query = "update analytics_analytics set processMessage=? where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, message);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }
    }//EoM updateLindaAnalyticsProcessMessage 

    /*
     * Updates LindaAnalyticsResultInfo
     */
    public void emptyLindaAnalyticsResultInfo(int analytics_id) {
        PreparedStatement preparedStatement = null;
        try {

            String query = "update analytics_analytics set processinfo=? , resultdocument=? , processMessage=?, plot1_id=null , plot2_id=null where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "");
            preparedStatement.setString(2, "");
            preparedStatement.setString(3, "");
            preparedStatement.setInt(4, analytics_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM emptyLindaAnalyticsResultDocument 

    /*
     * Add LindaAnalyticsPlot 
     */
    public long addPlot(String description, String image) {
        PreparedStatement preparedStatement = null;
        long plot_id = 0;
        try {
            String query = "INSERT INTO analytics_plot (description, image) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, description);
            preparedStatement.setString(2, image);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs != null && rs.next()) {
                plot_id = rs.getLong(1);
            }

            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

        return plot_id;
    }//EoM Add LindaAnalyticsPlot

    /*
     * updatePlot  
     */
    public long updatePlot(int plot_id, String image) {
        PreparedStatement preparedStatement = null;

        try {
            String query = "update analytics_plot  set image=? where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, image);
            preparedStatement.setInt(2, plot_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

        return plot_id;
    }//EoM updatePlot

    /*
     * Updates updateLindaAnalyticsPlot
     */
    public void updateLindaAnalyticsPlot(int analytics_id, long plot_id, String plot) {
        PreparedStatement preparedStatement = null;
        try {

            String query = "update analytics_analytics set " + plot + "=?  where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, (int) plot_id);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM updateLindaAnalyticsPlot 

    /*
     * delete old plot Plot
     */
    public void deletePlot(int plot_id) {
        PreparedStatement preparedStatement = null;
        try {

            String query = "delete from analytics_plot  where id=?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, (int) plot_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE" + ex);
        }

    }//EoM delete plot

}//EoC
