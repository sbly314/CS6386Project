/**
 * 
 */
package sysdproject;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * @author Stephen
 *
 */
public class DBControllerMS {
	private static boolean DEBUG = true;
	
	/* Semaphore variables */
	private static final int MAX_CONCURRENT_THREADS = 1;
	private final Semaphore lock = new Semaphore(MAX_CONCURRENT_THREADS, true);
	
	private static DBConnection dbConn;
	
	
	public DBControllerMS() {
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
            dbConn = new DBConnection(0); // 0 indicates MS
            dbConn.readDatabaseProperties();
        } catch (IOException e) {
            System.err.println("ERROR: Unable to read Database Properties: " + e);
            System.exit(1);
        }
	}
	
	/**
	 * @author Stephen
	 * @param InetAddress addr - IP of server
	 * @param int port - port server wants to receive media requests on
	 * @return integer show success or failure
	 * 
	 * This method checks if the IP address already exists and if so returns error.
	 * If does not exist, then inserts IP and port into MEDIA_SERVERS table
	 */
	int newMSConnection(InetAddress addr, int port) {
		int successFlag = 0; // 0 = False (Fail); 1 = True (Success)
		
		// All possible queries this method needs
		String checkIfIPexists = "SELECT * FROM MEDIA_SERVERS WHERE IP=INET_ATON('" + addr.getHostAddress() + "')";
		String getMaxServerID = "SELECT MAX(Server_id) FROM MEDIA_SERVERS";
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				ResultSet result = stat.executeQuery(checkIfIPexists);
				
				if(result.next()) { //IP already found
					successFlag = 0;
					return successFlag;
				} else { // New IP
					//Acquire lock before finding MAX(Server_id) and inserting based on that
					try {
						lock.acquire();
					} catch (InterruptedException e) {
						System.err.println("ERROR: acquiring lock");
						
						successFlag = 0;
						return successFlag;
					}
					
					try {
						ResultSet resCount = stat.executeQuery(getMaxServerID);
						
						int maxServerID;
						
						if (resCount.next()) {
							maxServerID = resCount.getInt(1);
						} else {
							// Assume no servers added yet
							maxServerID = 1;
						}
						
						// increment to next available Server_id
						++maxServerID;
						
						// Insert ServerID and Server
						String insertServer = "INSERT INTO MEDIA_SERVERS VALUES(" + maxServerID + ", INET_ATON('" + addr.getHostAddress() + "'), " + port + ")";
						stat.executeUpdate(insertServer);
						
						successFlag = 1; // set successFlag since made it to the end of this section
					} catch (Exception e) {
						System.err.println("ERROR: " + e.getMessage());
					} finally {
						lock.release();
					}
				}
			}
		} catch (SQLException e) {
			for (Throwable t : e)
				System.err.println("ERROR: " + t.getMessage());
		}
		
		return successFlag;
	}
	
	/**
	 * @author Stephen
	 * @param InetAddress addr - IP of server
	 * @param String movieName - name of movie to insert
	 * @param String category - category name of movie
	 * @param String url - url to stream movie
	 * @return integer show success or failure
	 * 
	 * This method inserts a new category (unless category already exists, in which case it gets Category_id).
	 * If movie name does not exist in TV_MOVIES, it adds it there (otherwise gets TV_Movie_id).
	 * If movie name and category already linked in TV_MOVIE_CATEGORY do nothing, otherwise insert.
	 * Insert TV_Movie_id, Server_id, URL into AVAILABLE_TV_MOVIES
	 * 
	 * This method is called on initial connection when MS syncs with Controller
	 * This method is also called on update of a new movie
	 */
	int insertQuery(InetAddress addr, String movieName, String category, String url) {
		int successFlag = 0; // 0 = False (Fail); 1 = True (Success)
		int tvMovieID;
		int categoryID;
		int serverID;
		
		// All possible queries this method calls (some defined later on)
		String checkForCategory = "SELECT Category_id FROM CATEGORIES WHERE Category_name='" + category + "'";
			String getMaxCategoryID = "SELECT MAX(Category_id) FROM CATEGORIES";
			String insertNewCategory;
		String checkForMovieName = "SELECT TV_Movie_id FROM TV_MOVIES WHERE TV_Movie_name='" + movieName + "'";
			String getMaxMovieID = "SELECT MAX(TV_Movie_id) FROM TV_MOVIES";
			String insertNewMovie;
		String checkForMovieCategory;
			String insertMovieCategory;
		String getMediaServerID = "SELECT Server_id FROM MEDIA_SERVERS WHERE IP=INET_ATON('" + addr.getHostAddress() + "')";
		String insertAvailableMovieServer;
		
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				//Look to see if Category Exists in DB
				ResultSet result = stat.executeQuery(checkForCategory);
				
				if(result.next()) {
					categoryID = result.getInt(1);
				} else { // Category does not exist
					//Acquire lock before finding MAX(categoryID) and inserting based on that
					try {
						lock.acquire();
					} catch (InterruptedException e) {
						System.err.println("ERROR: acquiring lock");
						
						successFlag = 0;
						return successFlag;
					}
					

					try {
						result = stat.executeQuery(getMaxCategoryID);
						
						if (result.next()) { // at least 1 category there
							categoryID = result.getInt(1) + 1;
						} else { // this is the first category
							categoryID = 1;
						}
						
						// insert new category
						insertNewCategory = "INSERT INTO CATEGORIES VALUES(" + categoryID + ", '" + category + "')";
						stat.executeUpdate(insertNewCategory);
					} catch (Exception e) {
						System.err.println("ERROR: " + e.getMessage());
						
						//error occurred which means new category should not have been added; safe to return here
						successFlag = 0;
						return successFlag;
					} finally {
						lock.release();
					}
				}
				
				//Look to see if Movie Exists in DB
				result = stat.executeQuery(checkForMovieName);
				
				if(result.next()) {
					tvMovieID = result.getInt(1);
				} else { // Movie does not exist
					//Acquire lock before finding MAX(tvMovieID) and inserting based on that
					try {
						lock.acquire();
					} catch (InterruptedException e) {
						System.err.println("ERROR: acquiring lock");
						
						successFlag = 0;
						return successFlag;
					}
					
					try {
						result = stat.executeQuery(getMaxMovieID);
						
						if (result.next()) { // at least 1 movie there
							tvMovieID = result.getInt(1) + 1;
						} else { // this is the first movie
							tvMovieID = 1;
						}
						
						//insert new movie
						insertNewMovie = "INSERT INTO TV_MOVIES VALUES(" + tvMovieID + ", '" + movieName + "')";
						stat.executeUpdate(insertNewMovie);
					} catch (Exception e) {
						System.err.println("ERROR: " + e.getMessage());
						
						//error occurred which means new movie should not have been added; safe to return here
						successFlag = 0;
						return successFlag;
					} finally {
						lock.release();
					}
				}
				
				//Look to see if Movie/Category relationship exists
				checkForMovieCategory = "SELECT * FROM TV_MOVIE_CATEGORY WHERE TV_Movie_id='" + tvMovieID + "' AND Category_id='" + categoryID + "'";
				result = stat.executeQuery(checkForMovieCategory);
				
				if(result.next()) {
					// Movie/Category relationship already exists; don't do anything
				} else { //INSERT into table
					insertMovieCategory = "INSERT INTO TV_MOVIE_CATEGORY VALUES(" + tvMovieID + "," + categoryID + ")";
					stat.executeUpdate(insertMovieCategory);
				}
				
				// Get Server_id for this server
				result = stat.executeQuery(getMediaServerID);
				
				if(result.next()) {
					serverID = result.getInt(1);
				} else {
					// should not be here; server should have been added during newMSConnection call
					successFlag = 0;
					return successFlag;
				}
				
				// Insert that this movie is available on this server into AVAILABLE_TV_MOVIES
				insertAvailableMovieServer = "INSERT INTO AVAILABLE_TV_MOVIES VALUES(" + tvMovieID + ", " + serverID + ", '" + url + "');";
				stat.executeUpdate(insertAvailableMovieServer);
			}
		} catch (SQLException e) {
			for (Throwable t : e)
				System.err.println("ERROR: " + t.getMessage());
		}
		
		
		successFlag = 1; // set successFlag since made it to the end of this section
		
		return successFlag;
	}
	
	/**
	 * @author Stephen
	 * @param InetAddress addr - IP of server
	 * @param String movieName - name of movie to insert
	 * @return integer show success or failure
	 * 
	 * Get Server_id for Server sending update
	 * If movie name does not exist in TV_MOVIES throw error, otherwise get TV_Movie_id.
	 * If last instance of movie in AVAILABLE_TV_MOVIES, delete association in TV_MOVIE_CATEGORY
	 * Delete row from AVAILABLE_TV_MOVIES
	 * Can leave category for future movies to use (May change this later)
	 * 
	 * This method is called on removal of a movie
	 */
	int deleteQuery(InetAddress addr, String movieName) {
		int successFlag = 0; // 0 = False (Fail); 1 = True (Success)
		
		int tvMovieID;
		int serverID;
		
		// All possible queries this method calls (some defined later on)
		String checkForMovieName = "SELECT TV_Movie_id FROM TV_MOVIES WHERE TV_Movie_name='" + movieName + "'";
		String getNumberAvailableMovie;
		/* If last instance of movie */
			String deleteMovieCategory;
			String deleteMovie;
		/* If not last instance of movie */
			String getMediaServerID = "SELECT Server_id FROM MEDIA_SERVERS WHERE IP=INET_ATON('" + addr.getHostAddress() + "')";
			String deleteAvailableMovieServer;
			
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				// Get TV_Movie_id
				ResultSet result = stat.executeQuery(checkForMovieName);
				
				if(result.next()) { 
					tvMovieID = result.getInt(1);
				} else { // Failure; couldn't find Movie
					successFlag = 0;
					return successFlag;
				}
				
				// Get count of this movie to determine if last instance
				getNumberAvailableMovie = "SELECT COUNT(*) FROM AVAILABLE_TV_MOVIES WHERE TV_Movie_id=" + tvMovieID;
				
				result = stat.executeQuery(getNumberAvailableMovie);
				
				if (result.next()) {
					int movieCount = result.getInt(1);
					
					if(movieCount == 1) { // last instance
						// Delete from TV_MOVIE_CATEGORY
						deleteMovieCategory = "DELETE FROM TV_MOVIE_CATEGORY WHERE TV_Movie_id=" + tvMovieID;
						stat.executeUpdate(deleteMovieCategory);
						
						// Delete from AVAILABLE_TV_MOVIES
						deleteAvailableMovieServer = "DELETE FROM AVAILABLE_TV_MOVIES WHERE TV_Movie_id=" + tvMovieID;
						stat.executeUpdate(deleteAvailableMovieServer);
						
						// Delete from TV_MOVIES
						deleteMovie = "DELETE FROM TV_MOVIES WHERE TV_Movie_id=" + tvMovieID;
						stat.executeUpdate(deleteMovie);
					} else { // another instance of movie exists
						// Get Media Server IP
						result = stat.executeQuery(getMediaServerID);
						
						if (result.next()) {
							serverID = result.getInt(1);
							
							deleteAvailableMovieServer = "DELETE FROM AVAILABLE_TV_MOVIES WHERE TV_Movie_id=" + tvMovieID + " AND Server_id=" + serverID;
							stat.executeUpdate(deleteAvailableMovieServer);
						} else {
							// Error; couldn't find server id
							successFlag = 0;
							return successFlag;
						}
					}
				}
			}
		} catch (SQLException e) {
			for (Throwable t : e)
				System.err.println("ERROR: " + t.getMessage());
		}
		
		
		successFlag = 1; // set successFlag since made it to the end of this section
		
		return successFlag;
	}
	
	
	/**
	 * @author Stephen
	 * @param InetAddress addr - IP of server
	 * 
	 * Call deleteQuery() for every movie on this server
	 * Delete Server from MEDIA_SERVERS
	 * 
	 * This method is called on removal of a server
	 */
	void deleteServer(InetAddress addr) {
		int serverID;
		
		if(DEBUG) {
			System.out.println("deleteServer");
		}
		
		// All possible queries this method calls (some defined later on)
		String getMediaServerID = "SELECT Server_id FROM MEDIA_SERVERS WHERE IP=INET_ATON('" + addr.getHostAddress() + "')";
		String getTVMovies;
		String checkForMovieName;
		String deleteServer;
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				
				// Get Server_id
				ResultSet result = stat.executeQuery(getMediaServerID);
				
				if(result.next()) { 
					serverID = result.getInt(1);
				} else {
					if(DEBUG) {
						System.out.println("ServerID already deleted?");
					}
					return; // already deleted?
				}
				
				if(DEBUG) {
					System.out.println("ServerID is " + serverID);
				}
				
				// Get List of Movies
				getTVMovies = "SELECT TV_Movie_id FROM AVAILABLE_TV_MOVIES WHERE Server_id=" + serverID;
				result = stat.executeQuery(getTVMovies);
				
				Vector<Integer> movieIDVector = new Vector<Integer>();
				
				// Loop through movies and use deleteQuery to delete each
				while(result.next()) {
					int tvMovieID = result.getInt(1);
					
					movieIDVector.add(tvMovieID);
				}
				
				Enumeration<Integer> eMovieID = movieIDVector.elements();
				
				while(eMovieID.hasMoreElements()) {
					int tvMovieID = eMovieID.nextElement();
					
					checkForMovieName = "SELECT TV_Movie_name FROM TV_MOVIES WHERE TV_Movie_id='" + tvMovieID + "'";
					
					result = stat.executeQuery(checkForMovieName);
					
					if(result.next()) {
						String movieName = result.getString(1);
						
						if(DEBUG) {
							System.out.println("DELETE: tvMovieID and name are " + tvMovieID + "\t" + movieName);
						}
						
						deleteQuery(addr, movieName);
					}
				}
				
				
				// Delete server
				deleteServer = "DELETE FROM MEDIA_SERVERS WHERE Server_id=" + serverID;
				stat.executeUpdate(deleteServer);
			}
		} catch (SQLException e) {
			for (Throwable t : e)
				System.err.println("ERROR: " + t.getMessage());
		}
	}
	
	/**
	 * @param args
	 * 
	 * Used to test this class
	 */
	public static void main(String[] args) {
		DBControllerMS msDB = new DBControllerMS();
		

		InetAddress testAddr1;
		InetAddress testAddr2;
		int port1;
		int port2;
		try {
			testAddr1 = InetAddress.getByName("127.0.0.1");
			port1 = 4092;
			msDB.newMSConnection(testAddr1, port1);
			
			msDB.insertQuery(testAddr1, "Star Wars", "Sci Fi", "Server1 URL For Movie 1");
			msDB.insertQuery(testAddr1, "Scream", "Horror", "Server1 URL For Movie 2");
			
			testAddr2 = InetAddress.getByName("127.0.0.2");
			port2 = 52000;
			msDB.newMSConnection(testAddr2, port2);
			
			msDB.insertQuery(testAddr2, "Scream", "Horror", "Server2 URL For Movie 2");
			msDB.insertQuery(testAddr2, "Friends", "Drama", "Server2 URL For Movie 3");
			msDB.insertQuery(testAddr2, "The Notebook", "Romance", "Server2 URL For Movie 4");
			msDB.insertQuery(testAddr1, "How I Met Your Mother", "Comedy", "Server1 URL For Movie 5");
			msDB.insertQuery(testAddr2, "How I Met Your Mother", "Comedy", "Server2 URL For Movie 5");
			
			msDB.deleteServer(testAddr1);
			
			if(DEBUG) {
				System.out.println("Server should be deleted now");
			}
			
			try {
				try (Connection conn = dbConn.getConnection()) {
					Statement stat = conn.createStatement();
					
					String query = "SELECT TV.TV_Movie_name, GROUP_CONCAT(INET_NTOA(MS.IP)), Cat.Category_name, "
							+ "GROUP_CONCAT(Avail.URL) FROM MEDIA_SERVERS MS, TV_MOVIES TV, CATEGORIES Cat, "
							+ "AVAILABLE_TV_MOVIES Avail, TV_MOVIE_CATEGORY TV_cat WHERE MS.Server_id=Avail.Server_id "
							+ "AND TV.TV_Movie_id=Avail.TV_Movie_id AND TV.TV_Movie_id=TV_cat.TV_Movie_id "
							+ "AND Cat.Category_id=TV_cat.Category_id GROUP BY TV_Movie_name, Category_name ORDER BY TV_Movie_name";
					
					Vector<String> columns = new Vector<String>(4);
					columns.add("TV_Movie_name");
					columns.add("MS_IP(s)");
					columns.add("Category_name");
					columns.add("URL(s)");
					
					Vector<Vector<String>> data = new Vector<Vector<String>>();
					Vector<String> row;
					
					ResultSet result = stat.executeQuery(query);
					
					while (result.next()) {
						row = new Vector<String>(4);
						
						for (int i = 1; i <=4; i++) {
							row.add(result.getString(i));
						}
						
						data.add(row);
					}
					
					Enumeration<String> eColumns = columns.elements();
					
					System.out.println(eColumns.nextElement());
					
					while(eColumns.hasMoreElements()) {
						System.out.println("\t" + eColumns.nextElement());
					}
					
					System.out.println("\n");
					
					Enumeration<Vector<String>> eData = data.elements();
					
					while(eData.hasMoreElements()) {
						Enumeration<String> eRow = eData.nextElement().elements();
						
						System.out.println(eRow.nextElement());
						
						while(eRow.hasMoreElements()) {
							System.out.println("\t" + eRow.nextElement());
						}
					}
				}
			} catch (SQLException e) {
				for (Throwable t : e)
					System.err.println("ERROR: " + t.getMessage());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
