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
	DBController dbController = new DBController();
	InetAddress IP = null;
    try {

      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());

	  while(true)
	  {
	  String line="";
      line = is.readLine();
      String temp[]=line.split(",");
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
		dbController.insertQuery(temp2[0], temp2[1]);
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
	  //System.out.println("\nSearching "+temp[1]+" in media list\n");
	  String temp2[]=temp[1].split("/");
	  Vector<String> response = new Vector<String>();
	  if(temp2[1].equals("All")){
	  	  System.out.println("\nSearching for All files in media list\n");
		  response=dbController.dbParser(dbController.queryAll()); // Query All
	  } else {
		 if(temp2[0].equals("")){
		 	System.out.println("\nSearching for all files in category "+temp2[1]+"\n");
			response=dbController.dbParser(dbController.queryCriteria(null, temp2[1])); // Category-only
		 }
		 else{
		 	System.out.println("\nSearching for "+temp2[0]+" in category "+temp2[1]+"\n");
		    response=dbController.dbParser(dbController.queryCriteria(temp2[0], temp2[1])); // Category and Media name  
		 }
	  }
	 
	 String [] str = response.toArray(new String[response.size()]); 
	 //System.out.println("\n\n-------- TESTING-------- ");
	 //for(int ii=0;ii<response.size();ii++){
	//		 System.out.println(str[ii]); 
	 //}
	  if(str[0].equals("1")){
		System.out.println("No match Found. Sending Error code ERR");
		os.println("FIN,ERR");
		}
	  else{
		  System.out.println("Match Found. Sending response in json Format");
		  os.println("FIN,"+(response.size()-1));
		  for(int jj=1;jj<response.size();jj++){
			 //System.out.println(str[jj]);
			 os.println(str[jj]);
		  }
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
	  dbController.insertQuery(temp2[0], temp2[1]);
	  System.out.println("\nSending FIN");
	  os.println("FIN,"+temp[1]);
	  break;
	  }
	  int remove=0;
	  if(message.equals("REMOVE")){
	  System.out.println("\nRemoving "+temp[1]+" from media list\n");
		for(int ii=0;ii<Controller.size;ii++){
		if(temp[1].equals(Controller.mediaList[ii])){
		System.out.println("Match found. Removing Media");
		String temp2[]=temp[1].split("-");
		dbController.deleteQuery(temp2[0],temp2[1]);
		for(int jj=ii;jj<Controller.size-1;jj++){
			Controller.mediaList[jj]=Controller.mediaList[jj+1];
		}
		Controller.size=Controller.size-1;
		System.out.println("Updated Media list:\n");
		for(int i=0;i<Controller.size;i++){
			System.out.println(Controller.mediaList[i]);
		}
		os.println("FIN,"+temp[1]);
		remove=1;
		break;
		}
	  }
	  if(remove==0){
		System.out.println("Media not found. Enter a valid Media name.");
		os.println("FIN,ERR");
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
class DBController {
	// variables to hold Media Server IP and Port
	private String serverIP;
	private int serverPort;
	
	// vectors to hold list of media files in each category
	private Vector<String> ComedyFiles;
	private Vector<String> DramaFiles;
	private Vector<String> HorrorFiles;
	private Vector<String> RomanceFiles;
	private Vector<String> FictionFiles;
	
	// Strings used for category names
	static private final String COMEDY = "Comedy";
	static private final String DRAMA = "Drama";
	static private final String HORROR = "Horror";
	static private final String ROMANCE = "Romance";
	static private final String FICTION = "Fiction";
	
	
	public DBController() {
		ComedyFiles = new Vector<String>();
		DramaFiles = new Vector<String>();
		HorrorFiles = new Vector<String>();
		RomanceFiles = new Vector<String>();
		FictionFiles = new Vector<String>();
	}
	
	public void setServerIP (InetAddress addr) {
		serverIP = addr.getHostAddress();
	}
	
	public String getServerIP () {
		return serverIP;
	}
	
	public void setServerPort (int port) {
		serverPort = port;
	}
	
	public int getServerPort () {
		return serverPort;
	}
	
	/**
	 * @author Stephen
	 * 
	 * @return Vector<Vector<String>> results - contains results of query; need to validate not null on return
	 * 
	 * This method queries the whole database
	 */
	public Vector<Vector<String>> queryAll() {
		
		Vector<Vector<String>> results = new Vector<Vector<String>>();
		
		Vector<Vector<String>> temp = new Vector<Vector<String>>();
		
		if(!ComedyFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = ComedyFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(COMEDY);
				results.add(row);
			}
		}
		
		if(!DramaFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = DramaFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(DRAMA);
				results.add(row);
			}
		}
		
		if(!HorrorFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = HorrorFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(HORROR);
				results.add(row);
			}
		}
		
		if(!RomanceFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = RomanceFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(ROMANCE);
				results.add(row);
			}
		}
		
		if(!FictionFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = FictionFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(FICTION);
				results.add(row);
			}
		}
		
		// check if all of the files are empty
		if (results.isEmpty()) {
			results = null;
		}
		
		return results;
	}
	
	public Vector<Vector<String>> queryAllComedy() {
		Vector<Vector<String>> results = new Vector<Vector<String>>();

		if(!ComedyFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = ComedyFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(COMEDY);
				results.add(row);
			}
		} else {
			results = null;
		}
		
		return results;
	}
	
	public Vector<Vector<String>> queryAllDrama() {
		Vector<Vector<String>> results = new Vector<Vector<String>>();

		if(!DramaFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = DramaFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(DRAMA);
				results.add(row);
			}
		} else {
			results = null;
		}
		
		return results;
	}
	
	public Vector<Vector<String>> queryAllHorror() {
		Vector<Vector<String>> results = new Vector<Vector<String>>();

		if(!HorrorFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = HorrorFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(HORROR);
				results.add(row);
			}
		} else {
			results = null;
		}
		
		return results;
	}
	
	public Vector<Vector<String>> queryAllRomance() {
		Vector<Vector<String>> results = new Vector<Vector<String>>();

		if(!RomanceFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = RomanceFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(ROMANCE);
				results.add(row);
			}
		} else {
			results = null;
		}
		
		return results;
	}
	
	public Vector<Vector<String>> queryAllFiction() {
		Vector<Vector<String>> results = new Vector<Vector<String>>();
		
		if(!FictionFiles.isEmpty()) {
			// Files exist
			Vector<String> row;
			
			Iterator iter = FictionFiles.iterator();
			while (iter.hasNext()) {
				row = new Vector<String>();
				String tmp = (String) iter.next();
				row.add(tmp);
				row.add(getServerIP());
				row.add(Integer.toString(getServerPort()));
				row.add(FICTION);
				results.add(row);
			}
		} else {
			results = null;
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
	public Vector<Vector<String>> queryCriteria(String movieNameCrit, String categoryCrit) {
		Vector<Vector<String>> results = new Vector<Vector<String>>();
		
		if(categoryCrit.compareTo(COMEDY) == 0) {
			results = queryFile(movieNameCrit, categoryCrit, ComedyFiles);
		} else if (categoryCrit.compareTo(DRAMA) == 0) {
			results = queryFile(movieNameCrit, categoryCrit, DramaFiles);
		} else if (categoryCrit.compareTo(HORROR) == 0) {
			results = queryFile(movieNameCrit, categoryCrit, HorrorFiles);
		} else if (categoryCrit.compareTo(ROMANCE) == 0) {
			results = queryFile(movieNameCrit, categoryCrit, RomanceFiles);
		} else if (categoryCrit.compareTo(FICTION) == 0) {
			results = queryFile(movieNameCrit, categoryCrit, FictionFiles);
		} else {
			System.out.println("ERROR: Unknown categoryCrit");
			results = null;
		}
		
		return results;
	}
	
	/**
	 * @author Stephen
	 * 
	 * @param String movieNameCrit - contains movie name (null if not used)
	 * @param String categoryCrit - contains search criteria for category (null if not used)
	 * @param Vector<String> fileName - contains fileName to search in
	 * 
	 * @return Vector<Vector<String>> results - contains results of query; need to validate not null on return
	 * 
	 * This method queries individual file
	 */
	public Vector<Vector<String>> queryFile(String movieNameCrit, String categoryCrit, Vector<String> fileName) {
		Vector<Vector<String>> results = new Vector<Vector<String>>();
		
		if(fileName.isEmpty()) {
			// Nothing to search
			return null;
		}
		
		if (movieNameCrit == null) {
			if(categoryCrit.compareTo(COMEDY) == 0) {
				results = queryAllComedy();
			} else if (categoryCrit.compareTo(DRAMA) == 0) {
				results = queryAllDrama();
			} else if (categoryCrit.compareTo(HORROR) == 0) {
				results = queryAllHorror();
			} else if (categoryCrit.compareTo(ROMANCE) == 0) {
				results = queryAllRomance();
			} else if (categoryCrit.compareTo(FICTION) == 0) {
				results = queryAllFiction();
			} else {
				System.out.println("ERROR: Unknown categoryCrit");
				results = null;
			}
		} else {
			CharSequence cs = (CharSequence) movieNameCrit;
			
			Iterator iter = fileName.iterator();
			while (iter.hasNext()) {
				String tmp = (String) iter.next();
				
				if(tmp.contains(cs)) {
					Vector<String>row = new Vector<String>();
					row.add(tmp);
					row.add(getServerIP());
					row.add(Integer.toString(getServerPort()));
					row.add(categoryCrit);
					results.add(row);
				}
			}
		}
		
		// check if no results found
		if (results.isEmpty()) {
			results = null;
		}
		
		return results;
	}
	
	boolean existsInFile(String mediaName, Vector<String> fileName) {
		boolean exists = false;
		
		if(!fileName.isEmpty()) {
			Iterator iter = fileName.iterator();
			while (iter.hasNext()) {
				String tmp = (String) iter.next();
				
				if(tmp.compareTo(mediaName) == 0) {
					exists = true;
					return exists;
				}
			}
		}
		
		return exists;
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
		setServerIP(addr);
		setServerPort(port);
		
		successFlag = 1;		
		return successFlag;
	}
	
	/**
	 * @author Stephen
	 * @param InetAddress addr - IP of server
	 * @param String movieName - name of movie to insert
	 * @param String category - category name of movie
	 * @return integer show success or failure
	 * 
	 * This method is called on initial connection when MS syncs with Controller
	 * This method is also called on update of a new movie
	 */
	int insertQuery(String movieName, String category) {
		int successFlag = 0; // 0 = False (Fail); 1 = True (Success)
		
		if(category.compareTo(COMEDY) == 0) {
			if(existsInFile(movieName, ComedyFiles)) {
				successFlag = 0;
				return successFlag;
			} else {
				ComedyFiles.add(movieName);
			}
		} else if (category.compareTo(DRAMA) == 0) {
			if(existsInFile(movieName, DramaFiles)) {
				successFlag = 0;
				return successFlag;
			} else {
				DramaFiles.add(movieName);
			}
		} else if (category.compareTo(HORROR) == 0) {
			if(existsInFile(movieName, HorrorFiles)) {
				successFlag = 0;
				return successFlag;
			} else {
				HorrorFiles.add(movieName);
			}
		} else if (category.compareTo(ROMANCE) == 0) {
			if(existsInFile(movieName, RomanceFiles)) {
				successFlag = 0;
				return successFlag;
			} else {
				RomanceFiles.add(movieName);
			}
		} else if (category.compareTo(FICTION) == 0) {
			if(existsInFile(movieName, FictionFiles)) {
				successFlag = 0;
				return successFlag;
			} else {
				FictionFiles.add(movieName);
			}
		} else {
			System.out.println("ERROR: Unknown categoryCrit");
			successFlag = 0;
			return successFlag;
		}
		
		successFlag = 1;		
		return successFlag;
	}
	
	/**
	 * @author Stephen
	 * @param String movieName - name of movie to remove
	 * @param String category - category name of movie
	 * @return integer show success or failure
	 * 
	 * This method is called on removal of a movie
	 */
	int deleteQuery(String movieName, String category) {
		int successFlag = 0; // 0 = False (Fail); 1 = True (Success)
		
		if(category.compareTo(COMEDY) == 0) {
			if(existsInFile(movieName, ComedyFiles)) {
				Iterator iter = ComedyFiles.iterator();
				while (iter.hasNext()) {
					String tmp = (String) iter.next();
					
					if(tmp.compareTo(movieName) == 0) {
						// Delete
						iter.remove();
					}
				}
			} else {
				successFlag = 0;
				return successFlag;
			}
		} else if (category.compareTo(DRAMA) == 0) {
			if(existsInFile(movieName, DramaFiles)) {
				Iterator iter = DramaFiles.iterator();
				while (iter.hasNext()) {
					String tmp = (String) iter.next();
					
					if(tmp.compareTo(movieName) == 0) {
						// Delete
						iter.remove();
					}
				}
			} else {
				successFlag = 0;
				return successFlag;
			}
		} else if (category.compareTo(HORROR) == 0) {
			if(existsInFile(movieName, HorrorFiles)) {
				Iterator iter = HorrorFiles.iterator();
				while (iter.hasNext()) {
					String tmp = (String) iter.next();
					
					if(tmp.compareTo(movieName) == 0) {
						// Delete
						iter.remove();
					}
				}
			} else {
				successFlag = 0;
				return successFlag;
			}
		} else if (category.compareTo(ROMANCE) == 0) {
			if(existsInFile(movieName, RomanceFiles)) {
				Iterator iter = RomanceFiles.iterator();
				while (iter.hasNext()) {
					String tmp = (String) iter.next();
					
					if(tmp.compareTo(movieName) == 0) {
						// Delete
						iter.remove();
					}
				}
			} else {
				successFlag = 0;
				return successFlag;
			}
		} else if (category.compareTo(FICTION) == 0) {
			if(existsInFile(movieName, FictionFiles)) {
				Iterator iter = FictionFiles.iterator();
				while (iter.hasNext()) {
					String tmp = (String) iter.next();
					
					if(tmp.compareTo(movieName) == 0) {
						// Delete
						iter.remove();
					}
				}
			} else {
				successFlag = 0;
				return successFlag;
			}
		} else {
			System.out.println("ERROR: Unknown categoryCrit");
			successFlag = 0;
			return successFlag;
		}
		
		successFlag = 1;		
		return successFlag;
	}
	
	/**
	 * @author Stephen
	 * @param InetAddress addr - IP of server
	 * 
	 * Delete Server from MEDIA_SERVERS
	 * 
	 * This method is called on removal of a server
	 */
	void deleteServer(InetAddress addr) {
		// do nothing; stub
	}
	
	
	/**
	 * @author Stephen
	 * 
	 * @param ResultSet rs - contains results of query; need to validate not null
	 * @return String - formatted in JSON output
	 * 
	 * 
	 * Need to handle case where ResultSet may be null (connection failed)
	 * Need to handle case where ResultSet may be empty (No matches found)
	 * Need to also extract the first value from the comma-separated results
	 */
	public Vector<String> dbParser(Vector<Vector<String>> rs) {
		Vector<String> returnVector = new Vector<String>();
		
		String errMessage = "1";
		
		if ( rs == null) {
			System.out.println("No movies matched your criteria");
			returnVector.add(errMessage);
			return returnVector;
		} else if (rs.isEmpty()) {
			System.out.println("No movies matched your criteria");
			returnVector.add(errMessage);
			return returnVector;
		} else {
			// used to add the header
			int firstRunIP = 0;
			
			Enumeration<Vector<String>> eRS = rs.elements();
			
			while(eRS.hasMoreElements()) {
				Enumeration<String> eRow = eRS.nextElement().elements();
				
				String movieName = eRow.nextElement();
				String serverIP = eRow.nextElement();
				String serverPort = eRow.nextElement();
				String category = eRow.nextElement();
				
				if(firstRunIP == 0) {
					// Put a 0 at the front for parsing in the calling function
					String header = "0,{\"ip\":\"" + serverIP + "\",\"port\":\"" + serverPort + "\",\"medialist\":[";
					
					returnVector.add(header);
					
					++firstRunIP;
				}
				
				
				String entry = "{\"name\":\"" + movieName + "\",\"category\":\"" + category + "\"}";
				
				//System.out.println("DEBUG: Before entry is " + entry);
				
				if(eRS.hasMoreElements()) {
					entry = entry.concat(",");
				}
				
				//System.out.println("DEBUG: After entry is " + entry);
				
				returnVector.add(entry);
				
				
				System.out.println("movieName = " + movieName);
				System.out.println("serverIP = " + serverIP);
				System.out.println("serverPort = " + serverPort);
				System.out.println("category = " + category);
				System.out.println("---------------------");
			}
			
			// Add trailer to returnVector
			returnVector.add("]}");
			
			
			// combine into string
			String combinedReturn = "";
			Iterator iter = returnVector.iterator();
			while (iter.hasNext()) {
				combinedReturn = combinedReturn + iter.next();
			}
			System.out.println("DEBUG: combinedReturn is " + combinedReturn);
			
			// clear the vector
			returnVector.clear();
			// split and place into vector so that each element is followed by a comma
			// calling function will add commas back
			returnVector.addAll(Arrays.asList(combinedReturn.split(",")));
			
			/*
			Iterator iter2 = returnVector.iterator();
			while (iter2.hasNext()) {
				System.out.println("DEBUG: returnVector is " + iter2.next());
			}
			*/
			
			return returnVector;
		}
	}
	
	public void printParserOutput(Vector<String> output) {
		Iterator iter = output.iterator();
		while (iter.hasNext()) {
			System.out.println("DEBUG: returnVector is " + iter.next());
		}
	}
}
