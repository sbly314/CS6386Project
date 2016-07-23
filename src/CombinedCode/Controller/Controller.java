import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/*
 * A chat server that delivers public and private messages.
 */
public class Controller {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;

  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 1000;
  private static final clientThread[] threads = new clientThread[maxClientsCount];
  public static String[] mediaList = new String[20];
  public static String[][] mediaServerDB = new String[20][3];
  public static int serverCount=0;
  public static int size=0;
  public static String receivedIP="";
  public static String receivedPort="";
  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
    if (args.length >= 1) {
      portNumber = Integer.valueOf(args[0]).intValue();
    }
	System.out.println("-----------This is the controller-----------");
    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {

  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;

  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
	DBControllerMS dbController = new DBControllerMS();
	DBControllerUser dbUser = new DBControllerUser();
	InetAddress IP = null;
    try {

      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());

	  while(true)
	  {
	  String line="";
      line = is.readLine();
      String temp[]=line.split(" ");
      String message=temp[0];
	  
	  if(message.equals("SYN")){
	  System.out.println("\nReceived "+message);
	  Controller.receivedIP=temp[1];
	  Controller.receivedPort=temp[2];
	  IP=InetAddress.getByName(Controller.receivedIP);
	  dbController.newMSConnection(IP, Integer.parseInt(Controller.receivedPort));
	  Controller.mediaServerDB[Controller.serverCount][0]=Integer.toString(Controller.serverCount+1);
	  Controller.mediaServerDB[Controller.serverCount][1]=Controller.receivedIP;
	  Controller.mediaServerDB[Controller.serverCount][2]=Controller.receivedPort;
	  Controller.serverCount++;
	  System.out.println("\nDatabase maintaining Server Information\n");
	  System.out.println("Server	 IP address	  Port");
	  
	  for(int i=0;i<Controller.serverCount;i++)
	  {
		  System.out.print("    ");
		  for(int j=0;j<3;j++)
		  {
			  System.out.print(Controller.mediaServerDB[i][j]+"    ");
		  }
		  System.out.println();
	  }
	  
	  os.println("ACK");
	  } 
	  
	  if(message.equals("SYNC")){
	  System.out.println("\nReceived "+message);
	  os.println("ACK");
	  } 
	  
	  if(message.equals("media")){
	  //Controller.size = Controller.size+temp.length;
	  System.out.println("\nMedia received:\n");
	  for(int i=1;i<temp.length;i++){
		Controller.mediaList[Controller.size]=temp[i];
		String temp2[]=temp[i].split("-");
		dbController.insertQuery(IP, temp2[0], temp2[1]);
		System.out.println(Controller.mediaList[Controller.size]);
		Controller.size++;
	  }
	  System.out.println("\nMedia list:\n");
	  for(int i=0;i<Controller.size;i++){
		System.out.println(Controller.mediaList[i]);
	  }  
	  
	  System.out.println("\nSending FIN");
	  os.println("FIN");
	  break;
	  } 
	  int found=0;
	  if(message.equals("search")){ //search moviename-Drama
	  System.out.println("\nSearching "+temp[1]+" in media list\n");
	  String temp2[]=temp[1].split("-");
	  if(temp2[1].equals("ALL")){
		  dbUser.dbParser(dbUser.queryAll()); // Query All
	  }
	  else {
		  if(temp2[0].equals("null")){
				dbUser.dbParser(dbUser.queryCriteria(temp2[1], null)); // Category-only
		  }
		  else{
			    dbUser.dbParser(dbUser.queryCriteria(temp2[0], temp2[1])); // Category and Media name  
		  }
	  }
	  
	  for(int i=0;i<Controller.size;i++){
		if(temp[1].equals(Controller.mediaList[i])){
		System.out.println("Match found. Sending Media");
		os.println("FIN "+Controller.mediaList[i]);
		found=1;
		break;
		}
	  }
	  if(found==0){
		System.out.println("No match Found. Sending Error code ERR");
		os.println("FIN ERR");
		}
	  break;
	  } 
	  
	  if(message.equals("ADD")){
	  System.out.println("\nAdding "+temp[1]+" to media list\n");
	  Controller.mediaList[Controller.size]=temp[1];
	  Controller.size=Controller.size+1;
	  System.out.println("Updated Media list:\n");
	  for(int i=0;i<Controller.size;i++){
		System.out.println(Controller.mediaList[i]);
	  }
	  String temp2[]=temp[1].split("-");
	  dbController.insertQuery(IP, temp2[0], temp2[1]);
	  System.out.println("\nSending FIN");
	  os.println("FIN "+temp[1]);
	  break;
	  }
	  int remove=0;
	  if(message.equals("REMOVE")){
	  System.out.println("\nRemoving "+temp[1]+" from media list\n");
		for(int ii=0;ii<Controller.size;ii++){
		if(temp[1].equals(Controller.mediaList[ii])){
		System.out.println("Match found. Removing Media");
		String temp2[]=temp[1].split("-");
		dbController.deleteQuery(IP, temp2[0]);
		for(int jj=ii;jj<Controller.size-1;jj++){
			Controller.mediaList[jj]=Controller.mediaList[jj+1];
		}
		Controller.size=Controller.size-1;
		System.out.println("Updated Media list:\n");
		for(int i=0;i<Controller.size;i++){
			System.out.println(Controller.mediaList[i]);
		}
		os.println("FIN "+temp[1]);
		remove=1;
		break;
		}
	  }
	  if(remove==0){
		System.out.println("Media not found. Enter a valid Media name.");
		os.println("FIN ERR");
		}
	  System.out.println("\nSending FIN");
	  os.println("FIN");
	  break;
	  }
	  }
	  


      

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}

/**
 * @author Stephen
 *
 */
class DBControllerMS {
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
	int insertQuery(InetAddress addr, String movieName, String category) {
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
				insertAvailableMovieServer = "INSERT INTO AVAILABLE_TV_MOVIES VALUES(" + tvMovieID + ", " + serverID + ");";
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
}

class DBControllerUser {
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
		
		String queryAllMovies = "SELECT TV.TV_Movie_name, GROUP_CONCAT(INET_NTOA(MS.IP)), GROUP_CONCAT(MS.Port), Cat.Category_name "
				+ "FROM MEDIA_SERVERS MS, TV_MOVIES TV, CATEGORIES Cat, "
				+ "AVAILABLE_TV_MOVIES Avail, TV_MOVIE_CATEGORY TV_cat WHERE MS.Server_id=Avail.Server_id "
				+ "AND TV.TV_Movie_id=Avail.TV_Movie_id AND TV.TV_Movie_id=TV_cat.TV_Movie_id AND "
				+ "Cat.Category_id=TV_cat.Category_id GROUP BY TV_Movie_name, Category_name ORDER BY TV_Movie_name;";
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				ResultSet rs = stat.executeQuery(queryAllMovies);
				
				results = new Vector<Vector<String>>();
				
				while(rs.next()) {
					Vector<String> row = new Vector<String>(4);

					row.add(rs.getString(1));
					row.add(rs.getString(2));
					row.add(rs.getString(3));
					row.add(rs.getString(4));
					
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
	 * 
	 * @return Vector<Vector<String>> results - contains results of query; need to validate not null on return
	 * 
	 * This method queries for specific criteria
	 */
	public Vector<Vector<String>> queryCriteria(String categoryCrit, String movieNameCrit) {
		Vector<Vector<String>> results = null;
		
		// base query
		String queryCriteria = "SELECT TV.TV_Movie_name, GROUP_CONCAT(INET_NTOA(MS.IP)), GROUP_CONCAT(MS.Port), Cat.Category_name "
				+ "FROM MEDIA_SERVERS MS, TV_MOVIES TV, CATEGORIES Cat, "
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
		
		// add ending to query
		queryCriteria +=  " GROUP BY TV_Movie_name, Category_name ORDER BY TV_Movie_name;";
		
		try {
			try (Connection conn = dbConn.getConnection()) {
				Statement stat = conn.createStatement();
				
				ResultSet rs = stat.executeQuery(queryCriteria);
				
				results = new Vector<Vector<String>>();
				
				while(rs.next()) {
					Vector<String> row = new Vector<String>(4);

					row.add(rs.getString(1));
					row.add(rs.getString(2));
					row.add(rs.getString(3));
					row.add(rs.getString(4));
					
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
	public void dbParser(Vector<Vector<String>> rs) {
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
				
				String[] serverIPsplit;
				String[] serverPortsplit;
				
				if (serverIP.contains(",")) {
					serverIPsplit = serverIP.split(",");
					serverIP = serverIPsplit[0];
				}
				
				if (serverPort.contains(",")) {
					serverPortsplit = serverPort.split(",");
					serverPort = serverPortsplit[0];
				}
				
				
				
				// IMPLEMENT JSON CODE HERE
				

				System.out.println("movieName = " + movieName);
				System.out.println("serverIP = " + serverIP);
				System.out.println("serverPort = " + serverPort);
				System.out.println("category = " + category);
				System.out.println("---------------------");
			}
		}
	}
}


class DBConnection {
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
        
        try (InputStream in = Files.newInputStream(Paths.get(databasePropertiesName))) {
           
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


