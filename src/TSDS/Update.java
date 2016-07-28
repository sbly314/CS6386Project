import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
public class Update {

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
	String update="";
	String media="";

    if (args.length >= 2) {
      update = args[0];
      media = args[1];
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


	System.out.println("\nSending UPDATE");
	update=update.concat(",");
	update=update.concat(media);
	os.println(update);
    while(true){
	String line="";
	line=is.readLine();
	String temp[]=line.split(",");  
	
	if(temp[0].equals("FIN")){
		if(temp[1].equals("ERR")){
		System.out.println("\nError. Media Could not be removed. Try again");
		}
		else{
		System.out.println("\nUpdate Successful");	
		}
		break;
	}
	}
        

        os.close();
        is.close();
        clientSocket.close();
  }
}
