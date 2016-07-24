import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
public class MediaPlayer {

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) throws IOException, InterruptedException {

    // The default port.
    int portNumber = 2222;
    // The default host.
    String host = "localhost";

    if (args.length >= 2) {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }
	
	System.out.println("-----------This is the Media Server-----------");
    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }


	String search="search";
	Scanner input = new Scanner(System.in);











	
	System.out.println("\nSending SYNC");
	os.println("SYNC");
    while(true){
	String line="";
	line=is.readLine();

	String temp[]=line.split(",");  
	if(temp[0].equals("ACK")){
		System.out.println("\nReceived "+temp[0]);
		System.out.println("\nEnter String to Search:");
		String media="";
		media = input.nextLine();
		search=search.concat(",");
		search=search.concat(media);
		os.println(search);
	}
	

	if(temp[0].equals("FIN")){
		if(temp[1].equals("ERR")){
		System.out.println("\nError. No Match found");
		}
		else{
		System.out.println("\nMedia "+temp[1]+" found");	
		}
		break;
	}
	}
        

        os.close();
        is.close();
        clientSocket.close();
  }
}
