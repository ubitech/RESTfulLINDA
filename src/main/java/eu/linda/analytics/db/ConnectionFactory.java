package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author Panagiotis Gouvas
 */
public class ConnectionFactory {

    private static volatile Connection instance = null;

    private ConnectionFactory() {
    }//EoC

//    public static Connection getInstance() {
//
//        try {
//            if (instance == null || instance.isClosed()) {
//                synchronized (ConnectionFactory.class) {
//                    if (instance == null || instance.isClosed()) {
//                        try {
//                            Class.forName("com.mysql.jdbc.Driver");
//                            instance = DriverManager.getConnection("jdbc:mysql://" + Configuration.dbip + ":" + Configuration.dbport + "/" + Configuration.dbname + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10", Configuration.username, Configuration.password);
//
//                        } catch (SQLException ee) {
//                            System.out.println("Connection Error");
//                        } catch (ClassNotFoundException ex) {
//                            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return instance;
//    }

    public static Connection getInstance() throws SQLException {

        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName("com.mysql.jdbc.Driver");
        source.setUsername(Configuration.username);
        source.setPassword(Configuration.password);
        source.setUrl( "jdbc:mysql://" + Configuration.dbip + ":" + Configuration.dbport + "/" + Configuration.dbname);
        Connection connection = source.getConnection();
        return connection;
    }
    
    

}//EoC
