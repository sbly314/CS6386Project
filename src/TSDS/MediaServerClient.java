import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class MediaServerClient implements ActionListener {
	// Client components
	// client socket
	private static Socket clientSocket = null;
	// output stream
	private static PrintStream os = null;
	// input stream
	private static DataInputStream is = null;
	// default port number
	private int portNumber = 2223;
	// default host
	private String host = "129.110.92.15"; // cs1.utdallas.edu
	
	private String delimiter = ",";
	
	
	// GUI Components
	private JPanel mainPanel = new JPanel(); // this is added to contentPane
	
	private String[] updateOptions = {"ADD", "REMOVE", "VIEW", "SUBSCRIBE"};
	private final JComboBox<String> updateCombo = new JComboBox<String>(updateOptions);
	
	private final JTextArea statusArea = new JTextArea("Status: Please enter information into above fields and press Submit");
	private final JScrollPane statusScrollPane = new JScrollPane(statusArea);
	
	private final JTextField mediaNameText = new JTextField();
	
	private String[] categories = {"--Select--", "Drama", "Fiction", "Horror", "Romance"};
	private final JComboBox<String> categoryCombo = new JComboBox<String>(categories);
	
	private JButton submitButton = new JButton("Submit");
	
	public MediaServerClient() {
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Select Function: ", SwingConstants.RIGHT), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 5;
		mainPanel.add(updateCombo, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5,0,0,0);
		mainPanel.add(new JLabel("Media Filename: ", SwingConstants.RIGHT), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 5;
		mainPanel.add(mediaNameText, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Category: ", SwingConstants.RIGHT), c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 5;
		mainPanel.add(categoryCombo, c);

		// Button
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 6;
		c.insets = new Insets(10,0,0,0); // top padding
		submitButton.addActionListener(this);
		mainPanel.add(submitButton, c);
		
		// Build lower Status Panel
		statusArea.setLineWrap(true);
		statusArea.setWrapStyleWord(true);
		statusScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		statusScrollPane.setPreferredSize(new Dimension(250, 250));
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 6;
/*		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
*/
		mainPanel.add(statusScrollPane, c);
		
	}
	
	public void setStatusMessage(String message) {
		statusArea.setText(message);
	}
	
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		String updateOption = (String)updateCombo.getSelectedItem();
		String mediaName = mediaNameText.getText();
		String category = (String)categoryCombo.getSelectedItem();
		
		if ((!updateOption.equals("VIEW") && !updateOption.equals("SUBSCRIBE")) && mediaName.isEmpty()) {
			setStatusMessage("ERROR: filename must be more than 0 characters in length!");
		} else if ((!updateOption.equals("VIEW") && !updateOption.equals("SUBSCRIBE")) && category.equals("--Select--")) {
			setStatusMessage("ERROR: You must select a category!");
		} else {
			String transmitToServer = "";
			
			if (updateOption.equals("ADD") || updateOption.equals("REMOVE")) {
				setStatusMessage(updateOption + " " + mediaName + " and category: " + category);
				transmitToServer = updateOption + delimiter + mediaName + delimiter + category;
			} else {
				transmitToServer = updateOption;
			}
			
			try {
				// initialize
				clientSocket = new Socket(host, portNumber);
				os = new PrintStream(clientSocket.getOutputStream());
				is = new DataInputStream(clientSocket.getInputStream());
				
				os.println(transmitToServer);
				
				int printFlag = 0;
				Vector<String> viewOutput = new Vector<String>(); 
				
				while(true) {
					String line = "";
					line = is.readLine();
					
					if (line == null) {
						setStatusMessage("ERROR: null message received");
						break;
					} else {
						String temp[] = line.split(delimiter);
						
						if (temp[0].equals("PRINT")) {
							printFlag = 1;
							System.out.println("PRINT: " + temp[1]);
							viewOutput.addElement(temp[1]);
						}
						
						// Check for success
						if (temp[0].equals("FIN")) {
							
							if (printFlag == 1) {
								String output = "";
								Iterator<String> iter = viewOutput.iterator();
								while (iter.hasNext()) {
									output = output + "\n" + iter.next();
								}
								setStatusMessage(output);
							} else {
								setStatusMessage("Success!");
							}
							break;
						}
						
						// Check for error
						if (temp[0].equals("ERR")) {
							setStatusMessage("ERROR: " + temp[1]);
							break;
						}
					}
				}
				
				// close
				os.close();
				is.close();
				clientSocket.close();
				
			} catch (UnknownHostException e1) {
				System.err.println("Unknown host: " + host);
				setStatusMessage("Unknown host: " + host);
			} catch (IOException e2) {
				System.err.println("Couldn't get I/O for connection to host: " + host);
				setStatusMessage("Couldn't get I/O for connection to host: " + host);
			}
		}
	}
	
	public JComponent getMainComponent() {
		return mainPanel;
	}
	
	private static void createAndShowGui() {
		MediaServerClient client = new MediaServerClient();
		
		// creating JFrame
		JFrame frame = new JFrame("Update Media Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(client.getMainComponent());
		frame.setLocationByPlatform(true);
		frame.setSize(600, 200);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				createAndShowGui();
			}
		});
	}
}
