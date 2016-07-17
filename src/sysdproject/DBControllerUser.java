/**
 * 
 */
package sysdproject;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author Stephen
 *
 */
public class DBControllerUser {
	private static DBConnection dbConn;
	
	public DBControllerUser() {
		startConnection();
	}

	/**
	 * @author Stephen
	 * 
	 * Initialize the dbConn variable
	 * 
	 */
	public void startConnection() {
		//try to read the database.properties file
        try {
            dbConn = new DBConnection(1); // 1 indicates User
            dbConn.readDatabaseProperties();
        } catch (IOException e) {
            System.err.println("ERROR: Unable to read Database Properties: " + e);
            System.exit(1);
        }
	}
	
	/**
	 * @author Stephen
	 * 
	 * @return Vector<Vector<String>> results - contains results of query; need to validate not null on return
	 * 
	 * This method queries the whole database
	 */
	public Vector<Vector<String>> queryAll() {
		Vector<Vector<String>> results = null;
		
		String queryAllMovies = "SELECT TV.TV_Movie_name, GROUP_CONCAT(INET_NTOA(MS.IP)), GROUP_CONCAT(MS.Port), Cat.Category_name, "
				+ "GROUP_CONCAT(Avail.URL) FROM MEDIA_SERVERS MS, TV_MOVIES TV, CATEGORIES Cat, "
				+ "AVAILABLE_TV_MOVIES Avail, TV_MOVIE_CATEGORY TV_cat WHERE MS.Server_id=Avail.Server_id "
				+ "AND TV.TV_Movie_id=Avail.TV_Movie_id AND TV.TV_Movie_id=TV_cat.TV_Movie_id AND "
				+ "Cat.Category_id=TV_cat.Category_id GROUP BY TV_Movie_name, Category_name ORDER BY TV_Movie_name;";
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				ResultSet rs = stat.executeQuery(queryAllMovies);
				
				results = new Vector<Vector<String>>();
				
				while(rs.next()) {
					Vector<String> row = new Vector<String>(5);

					row.add(rs.getString(1));
					row.add(rs.getString(2));
					row.add(rs.getString(3));
					row.add(rs.getString(4));
					row.add(rs.getString(5));
					
					results.add(row);
				}
			}
		} catch (SQLException e) {
			for (Throwable t : e)
				System.err.println("ERROR: " + t.getMessage());
		}
		
		return results;
	}
	
	/**
	 * @author Stephen
	 * 
	 * @param String categoryCrit - contains search criteria for category (null if not used)
	 * @param String movieNameCrit - contains search criteria for movie name (null if not used)
	 * @param String serverCrit - contains search criteria for server (null if not used)
	 * 
	 * @return Vector<Vector<String>> results - contains results of query; need to validate not null on return
	 * 
	 * This method queries for specific criteria
	 */
	public Vector<Vector<String>> queryCriteria(String categoryCrit, String movieNameCrit, String serverCrit) {
		Vector<Vector<String>> results = null;
		
		// base query
		String queryCriteria = "SELECT TV.TV_Movie_name, GROUP_CONCAT(INET_NTOA(MS.IP)), GROUP_CONCAT(MS.Port), Cat.Category_name, "
				+ "GROUP_CONCAT(Avail.URL) FROM MEDIA_SERVERS MS, TV_MOVIES TV, CATEGORIES Cat, "
				+ "AVAILABLE_TV_MOVIES Avail, TV_MOVIE_CATEGORY TV_cat WHERE MS.Server_id=Avail.Server_id "
				+ "AND TV.TV_Movie_id=Avail.TV_Movie_id AND TV.TV_Movie_id=TV_cat.TV_Movie_id AND "
				+ "Cat.Category_id=TV_cat.Category_id";
		
		// add category criteria if available
		if(categoryCrit != null) {
			queryCriteria += " AND Cat.Category_name LIKE '%" + categoryCrit + "%'";
		}
		
		// add category criteria if available
		if(movieNameCrit != null) {
			queryCriteria += " AND TV.TV_Movie_name LIKE '%" + movieNameCrit + "%'";
		}
		
		// add category criteria if available
		if(serverCrit != null) {
			queryCriteria += " AND INET_NTOA(MS.IP) LIKE '%" + serverCrit + "%'";
		}
		
		// add ending to query
		queryCriteria +=  " GROUP BY TV_Movie_name, Category_name ORDER BY TV_Movie_name;";
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				ResultSet rs = stat.executeQuery(queryCriteria);
				
				results = new Vector<Vector<String>>();
				
				while(rs.next()) {
					Vector<String> row = new Vector<String>(5);

					row.add(rs.getString(1));
					row.add(rs.getString(2));
					row.add(rs.getString(3));
					row.add(rs.getString(4));
					row.add(rs.getString(5));
					
					results.add(row);
				}
			}
		} catch (SQLException e) {
			for (Throwable t : e)
				System.err.println("ERROR: " + t.getMessage());
		}
		
		return results;
	}
	
	/**
	 * @author Stephen
	 * 
	 * @param ResultSet rs - contains results of query; need to validate not null
	 * 
	 * Need to handle case where ResultSet may be null (connection failed)
	 * Need to handle case where ResultSet may be empty (No matches found)
	 * Need to also extract the first value from the comma-separated results
	 */
	public void testParser(Vector<Vector<String>> rs) {
		if ( rs == null) {
			System.out.println("Error connecting to database.  Please try again later.");
		} else if (rs.isEmpty()) {
			System.out.println("No movies matched your criteria");
		} else {
			Enumeration<Vector<String>> eRS = rs.elements();
			
			while(eRS.hasMoreElements()) {
				Enumeration<String> eRow = eRS.nextElement().elements();
				
				String movieName = eRow.nextElement();
				String serverIP = eRow.nextElement();
				String serverPort = eRow.nextElement();
				String category = eRow.nextElement();
				String url = eRow.nextElement();
				
				String[] serverIPsplit;
				String[] serverPortsplit;
				String[] urlSplit;
				
				if (serverIP.contains(",")) {
					serverIPsplit = serverIP.split(",");
					serverIP = serverIPsplit[0];
				}
				
				if (serverPort.contains(",")) {
					serverPortsplit = serverPort.split(",");
					serverPort = serverPortsplit[0];
				}
				
				if (url.contains(",")) {
					urlSplit = url.split(",");
					url = urlSplit[0];
				}

				System.out.println("movieName = " + movieName);
				System.out.println("serverIP = " + serverIP);
				System.out.println("serverPort = " + serverPort);
				System.out.println("category = " + category);
				System.out.println("url = " + url);
				System.out.println("---------------------");
			}
		}
	}
	
	/**
	 * @param args
	 * 
	 * Used to test this class
	 * 
	 */
	public static void main(String[] args) {
		DBControllerUser userDB = new DBControllerUser();

		System.out.println("\n\n\nQUERYALL");
		userDB.testParser(userDB.queryAll());
		
		System.out.println("\n\n\nQUERY - No CRITERIA");
		userDB.testParser(userDB.queryCriteria(null, null, null));
		
		System.out.println("\n\n\nQUERY - ALL CRITERIA");
		userDB.testParser(userDB.queryCriteria("Hor", "Scr", "127.0.0.2"));
		
		System.out.println("\n\n\nQUERY - Category CRITERIA");
		userDB.testParser(userDB.queryCriteria("ci ", null, null));
		
		System.out.println("\n\n\nQUERY - Movie CRITERIA");
		userDB.testParser(userDB.queryCriteria(null, "Met", null));
		
		System.out.println("\n\n\nQUERY - Server CRITERIA");
		userDB.testParser(userDB.queryCriteria(null, null, "127.0.0.2"));
		
		System.out.println("\n\n\nQUERY - ALL CRITERIA - NO MATCH");
		userDB.testParser(userDB.queryCriteria("ABCD", "Scr", "127.0.0.2"));
	}

}
