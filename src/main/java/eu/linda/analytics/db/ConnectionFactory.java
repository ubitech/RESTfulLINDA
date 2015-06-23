package eu.linda.analytics.db;

import eu.linda.analytics.config.Configuration;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class ConnectionFactory {

    private ConnectionFactory() {
    }//EoC

    public static Connection getInstance() throws SQLException {

        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName("com.mysql.jdbc.Driver");
        source.setUsername(Configuration.username);
        source.setPassword(Configuration.password);
        source.setUrl("jdbc:mysql://" + Configuration.dbip + ":" + Configuration.dbport + "/" + Configuration.dbname);
        Connection connection = source.getConnection();
        return connection;
    }

}//EoC
