/**
 * 
 */
package sysdproject;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * @author Stephen
 *
 */
public class DBConnection {
	private Properties props;
	private int MSorUser; // 0 for Media Server(MS); 1 for User
    
    public DBConnection(int type) {
        //System.out.println("Connection started");
        MSorUser = type;
    }
    
    public void readDatabaseProperties() throws IOException {
        props = new Properties();
       //System.out.println("read database properties");
        
        String databasePropertiesName;
        if (MSorUser == 0) { // MS
        	databasePropertiesName = "databaseForMS.properties";
        } else { // User
        	databasePropertiesName = "databaseForUser.properties";
        }
        
        try (InputStream in = Files.newInputStream(Paths.get("src", "sysdproject", databasePropertiesName))) {
           
            props.load(in);
           
            //load driver class
            Class.forName(props.getProperty("DB_DRIVER_CLASS"));
           
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() throws SQLException {
        String url = props.getProperty("DB_URL");
        String username = props.getProperty("DB_USERNAME");
        String password = props.getProperty("DB_PASSWORD");
        
        return DriverManager.getConnection(url, username, password);
    }
}
