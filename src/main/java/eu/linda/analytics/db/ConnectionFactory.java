package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Panagiotis Gouvas
 */
public class ConnectionFactory {
    private static volatile Connection instance = null;
    
    private ConnectionFactory() { 
    }//EoC
    
    public static Connection getInstance() {
        if (instance == null) {
            synchronized (ConnectionFactory.class) {
                if (instance == null) {
                    try  
                    {  
                        Class.forName("com.mysql.jdbc.Driver");  
                       instance = DriverManager.getConnection("jdbc:mysql://"+Configuration.dbip+":"+Configuration.dbport+"/"+Configuration.dbname,Configuration.username,Configuration.password);             
                    }  
                    catch(ClassNotFoundException ee)  
                    {  
                        System.out.println("Class Error");  
                    }  
                    catch(SQLException ee)  
                    {  
                        System.out.println("Connection Error");  
                    } 
                }
            }
        }
        return instance;
    }//EoM
    
    
}//EoC