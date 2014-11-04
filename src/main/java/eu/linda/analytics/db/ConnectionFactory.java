package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas
 */
public class ConnectionFactory {

    private static volatile Connection instance = null;

    private ConnectionFactory() {
    }//EoC

    public static Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                synchronized (ConnectionFactory.class) {
                    if (instance == null || instance.isClosed()) {
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            instance = DriverManager.getConnection("jdbc:mysql://" + Configuration.dbip + ":" + Configuration.dbport + "/" + Configuration.dbname+"?autoReconnect=true", Configuration.username, Configuration.password);
                        } catch (ClassNotFoundException ee) {
                            System.out.println("Class Error");
                        } catch (SQLException ee) {
                            System.out.println("Connection Error");
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return instance;
    }

}//EoC
