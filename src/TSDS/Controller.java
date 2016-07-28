import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

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
	  if(message.equals("search")){
	  System.out.println("\nSearching "+temp[1]+" in media list\n");
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
