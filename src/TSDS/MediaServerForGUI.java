import java.io.*;
import java.net.*;

public class MediaServerForGUI {
	
	//server socket
	private static ServerSocket serverSocket = null;
	//client socket
	private static Socket clientSocket = null;
	
	// accept up to maxClientsCount concurrent connections
	private static final int maxClientsCount = 1000;
	private static final clientThread[] threads = new clientThread[maxClientsCount];
	// default port number
	private static int portNumber = 2223;
	private static String delimiter = ",";
	
	public static void main(String[] args) {
		/*
		 * Open server socket on portNumber
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.err.println(e);
		}
		
		/*
		 * Create client socket for each connection and pass to new client thread
		 */
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i=0; i < maxClientsCount; i++) {
					if(threads[i] == null) {
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}
				}
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("ERR" + delimiter + "Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}

class clientThread extends Thread {
	/* Parameters for receiving client */
	private DataInputStream gui_is = null;
	private PrintStream gui_os = null;
	private Socket gui_clientSocket = null;
	private final clientThread[] threads;
	private int maxClientsCount;
	
	
	/* Parameters to update Controller (client to Controller server) */
	private Socket update_clientSocket = null;
	private PrintStream update_os = null;
	private DataInputStream update_is = null;
	private int update_portNumber = 2222;
	private String update_host = "129.110.92.16";
	

	private String delimiter = ",";
	
	public static int cou=0;
	  
	public static String[] fileUpdate(String fileName, String[]arr, String category) throws IOException{
		String line="";
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader=new BufferedReader(fileReader);
		
		while((line = bufferedReader.readLine()) != null){
			line=line.trim();
			line=line.concat("-");
			line=line.concat(category);
			arr[cou]=line;
			cou++;
		}
		
		bufferedReader.close();
		
		return arr;
	}
	
	public clientThread(Socket clientSocket, clientThread[] threads) {
		this.gui_clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		int maxClientsCount = this.maxClientsCount;
		clientThread[] threads = this.threads;
		
		try {
			gui_is = new DataInputStream(gui_clientSocket.getInputStream());
			gui_os = new PrintStream(gui_clientSocket.getOutputStream());
			
			String s;
			Process p;
			BufferedReader br;
			
			String dir = System.getProperty("user.dir");
			dir += "/live/mediaServer/";
			
			
			
			String gui_line = "";
			gui_line = gui_is.readLine();
			
			System.out.println("Received string is " + gui_line);
			
			if (gui_line.startsWith("ADD") || gui_line.startsWith("REMOVE")) {
				String gui_temp[] = gui_line.split(delimiter);
				
				String updateCommand = gui_temp[0];
				String mediaName = gui_temp[1];
				String category = gui_temp[2];
				
	
				System.out.println("updateCommand is " + updateCommand);
				System.out.println("mediaName is " + mediaName);
				System.out.println("category is " + category);
				
				String update = updateCommand;
				String media = mediaName + "-" + category;
				
				
				/*
				 * Open a socket on given host and port; open input/output streams
				 */
				try {
					update_clientSocket = new Socket(update_host, update_portNumber);
					update_os = new PrintStream(update_clientSocket.getOutputStream());
					update_is = new DataInputStream(update_clientSocket.getInputStream());
					
					System.out.println("UPDATE: Sending Update");
					update = update.concat(",");
					update = update.concat(media);
					
					update_os.println(update);
					
					while(true) {
						String update_line = "";
						update_line=update_is.readLine();
						String update_temp[] = update_line.split(",");
						
						if(update_temp[0].equals("FIN")) {
							if(update_temp[1].equals("ERR")) {
								System.err.println("UPDATE: Error.  Media could not be removed.  Try again");
								// Send data back to desktop GUI
								gui_os.println("ERR" + delimiter + "Media Could Not Be Removed. Try again");
							} else {
								System.out.println("UPDATE: Update successful");
								// Send data back to desktop GUI
								gui_os.println("FIN");
								
								if (gui_line.startsWith("ADD")) {
									// move into category
									p = Runtime.getRuntime().exec("mv " + dir + "/" + mediaName + " " + dir + category + "/" + mediaName);
									p.waitFor();
									p.destroy();
									
									// Write to output file
									ProcessBuilder builder = new ProcessBuilder("ls", dir + category, "-1");
									builder.redirectOutput(new File(category + ".txt"));
									p.waitFor();
									p.destroy();
								} else if (gui_line.startsWith("REMOVE")) {
									// move out of category
									p = Runtime.getRuntime().exec("mv " + dir + category + "/" + mediaName + " " + dir + mediaName);
									p.waitFor();
									p.destroy();
									
									// Write to output file
									ProcessBuilder builder = new ProcessBuilder("ls", dir + category, "-1");
									builder.redirectOutput(new File(category + ".txt"));
									p.waitFor();
									p.destroy();
								}
							}
							break;
						}
					}
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host " + update_host);
					gui_os.println("ERR" + delimiter + "Don't know about host " + update_host);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for connection to host" + update_host);
					gui_os.println("ERR" + delimiter + "Couldn't get I/O for connection to host" + update_host);
				} catch (InterruptedException e) {
					
				}
				
				/*
				 * Close Streams and socket for updating controller
				 */
				update_is.close();
				update_os.close();
				update_clientSocket.close();
				
			} else if (gui_line.startsWith("SUBSCRIBE")) {
				try {
					update_clientSocket = new Socket(update_host, update_portNumber);
					update_os = new PrintStream(update_clientSocket.getOutputStream());
					update_is = new DataInputStream(update_clientSocket.getInputStream());
					
					System.out.println("UPDATE: Subscribing");
					
					String[] mediaList = new String[20];
					mediaList=fileUpdate("Drama.txt", mediaList,"Drama");
					mediaList=fileUpdate("Romance.txt", mediaList,"Romance");
					mediaList=fileUpdate("Fiction.txt", mediaList,"Fiction");
					mediaList=fileUpdate("Horror.txt", mediaList,"Horror");

					String media="media";
					System.out.println("\nMedia List:\n");
					for(int i=0;i<cou;i++){
						media=media.concat(",");
						media=media.concat(mediaList[i]);
						System.out.println(mediaList[i]);
					}
					
					System.out.println("\nSending SYN");
					update_os.println("SYN,129.110.92.15,8554"); // Media Server
				    while(true){
						String line="";
						line=update_is.readLine();
						String temp[]=line.split(",");
						
						if(temp[0].equals("ACK")){
							System.out.println("\nReceived "+temp[0]);
							System.out.println("\nSending Media List");
							update_os.println(media);
						}
						
						if(temp[0].equals("FIN")){
							System.out.println("\nReceived "+temp[0]);
							// Send data back to desktop GUI
							gui_os.println("FIN");
							break;
						}
					}
					
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host " + update_host);
					gui_os.println("ERR" + delimiter + "Don't know about host " + update_host);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for connection to host " + update_host);
					gui_os.println("ERR" + delimiter + "Couldn't get I/O for connection to host " + update_host);
					e.printStackTrace();
					System.out.println(e.getMessage());
				}
				
				/*
				 * Close Streams and socket for updating controller
				 */
				update_is.close();
				update_os.close();
				update_clientSocket.close();
			} else if (gui_line.startsWith("VIEW")) {
				System.out.println("UPDATE: View");
				
				int errorSentinel = 0;
				
				gui_os.println("PRINT" + delimiter + "-----Available Media Files-----");

				gui_os.println("PRINT" + delimiter + " ");
				gui_os.println("PRINT" + delimiter + "Category: Drama");
				try {
					p = Runtime.getRuntime().exec("ls " + dir + "Drama -1");
					br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					while ((s = br.readLine()) != null) {
						System.out.println("line: " + s);
						gui_os.println("PRINT" + delimiter + s);
					}
					p.waitFor();
					p.destroy();
					
					// Write to output file
					ProcessBuilder builder = new ProcessBuilder("ls", dir + "Drama", "-1");
					builder.redirectOutput(new File("Drama.txt"));
					p = builder.start();
					p.waitFor();
					p.destroy();
				} catch (Exception e) {
					errorSentinel = 1;
				}
				
				if (errorSentinel == 0) {
					gui_os.println("PRINT" + delimiter + " ");
					gui_os.println("PRINT" + delimiter + "Category: Fiction");
					try {
						p = Runtime.getRuntime().exec("ls " + dir + "Fiction -1");
						br = new BufferedReader(new InputStreamReader(p.getInputStream()));
						while ((s = br.readLine()) != null) {
							System.out.println("line: " + s);
							gui_os.println("PRINT" + delimiter + s);
						}
						p.waitFor();
						p.destroy();
						
						// Write to output file
						ProcessBuilder builder = new ProcessBuilder("ls", dir + "Fiction", "-1");
						builder.redirectOutput(new File("Fiction.txt"));
						p = builder.start();
						p.waitFor();
						p.destroy();
					} catch (Exception e) {
						errorSentinel = 1;
					}
				}
				
				if (errorSentinel == 0) {
					gui_os.println("PRINT" + delimiter + " ");
					gui_os.println("PRINT" + delimiter + "Category: Romance");
					try {
						p = Runtime.getRuntime().exec("ls " + dir + "Romance -1");
						br = new BufferedReader(new InputStreamReader(p.getInputStream()));
						while ((s = br.readLine()) != null) {
							System.out.println("line: " + s);
							gui_os.println("PRINT" + delimiter + s);
						}
						p.waitFor();
						p.destroy();
						
						// Write to output file
						ProcessBuilder builder = new ProcessBuilder("ls", dir + "Romance", "-1");
						builder.redirectOutput(new File("Romance.txt"));
						p = builder.start();
						p.waitFor();
						p.destroy();
					} catch (Exception e) {
						errorSentinel = 1;
					}
				}
				
				if (errorSentinel == 0) {
					gui_os.println("PRINT" + delimiter + " ");
					gui_os.println("PRINT" + delimiter + "Category: Horror");
					try {
						p = Runtime.getRuntime().exec("ls " + dir + "Horror -1");
						br = new BufferedReader(new InputStreamReader(p.getInputStream()));
						while ((s = br.readLine()) != null) {
							System.out.println("line: " + s);
							gui_os.println("PRINT" + delimiter + s);
						}
						p.waitFor();
						p.destroy();
						
						// Write to output file
						ProcessBuilder builder = new ProcessBuilder("ls", dir + "Horror", "-1");
						builder.redirectOutput(new File("Horror.txt"));
						p.waitFor();
						p.destroy();
					} catch (Exception e) {
						errorSentinel = 1;
					}
					
					gui_os.println("PRINT" + delimiter + " ");
					gui_os.println("PRINT" + delimiter + "-------------------------------");
					gui_os.println("FIN");
				}
				
				if (errorSentinel == 1) {
					gui_os.println("ERR" + delimiter + "VIEW Error");
				}
			}
			
			
			
			gui_is.close();
			gui_os.close();
			gui_clientSocket.close();
			
			/*
			 * Clean up GUI Threads.  Set current thread variable to null so that a new client
			 * could be accepted by the server
			 */
			synchronized(this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if(threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			
			/*
			 * Close output stream, input stream, and socket for GUI
			 */
		} catch (IOException e) {
			
		}
	}
}
