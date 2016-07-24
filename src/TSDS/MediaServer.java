import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
public class MediaServer {

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

	String[] mediaList = new String[20];
	mediaList[0]="ABC-SciFi";
	mediaList[1]="DEF-Horror";
	mediaList[2]="XYZ-Drama";
	String media="media";
	System.out.println("\nMedia List:\n");
	for(int i=0;i<3;i++){
		media=media.concat(",");
		media=media.concat(mediaList[i]);
		System.out.println(mediaList[i]);
	}
	
	System.out.println("\nSending SYN");
	os.println("SYN,10.111.152.13,2223");
    while(true){
	String line="";
	line=is.readLine();
	String temp[]=line.split(",");  
	if(temp[0].equals("ACK")){
		System.out.println("\nReceived "+temp[0]);
		System.out.println("\nSending Media List");
		os.println(media);
	}
	
	if(temp[0].equals("FIN")){
		System.out.println("\nReceived "+temp[0]);
		break;
	}
	}
        

        os.close();
        is.close();
        clientSocket.close();
  }
}
