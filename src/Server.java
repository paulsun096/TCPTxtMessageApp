import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
public class Server extends JFrame {

	private static final long serialVersionUID = 1L;
	//area where message is typed before it is sent
	private JTextField userText;
	private String message = "";
	//displays the text of the conversation
	private JTextArea chatWindow;
	//steams methods of communications
	//1: output stream (your computer to FRIENDS computer) send stuff away
	private ObjectOutputStream output;

	//2: input stream (friends types message, gets sent to YOUR computer) stuff comes to you
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	private String clientIP; //ip address of person you are talking to
	//constructor set up GUI 
	public Server(String client){
		super("Server");
		clientIP = client;
		userText = new JTextField();
		userText.setEditable(false);//cannot type when not connected
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());
						userText.setText("");
					}
				}
		);
		add(userText, BorderLayout.SOUTH);

		Font font = new Font("Helvetica", Font.PLAIN, 19);
		userText.setFont(font);

		chatWindow = new JTextArea();
		chatWindow.setFont(font);
		chatWindow.setEditable(false);
		chatWindow.setLineWrap(true);
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(450, 500);
		setVisible(true);
	}

	//set up and run server
	public void startRunning() {
		try {
			server = new ServerSocket(10404, 100); //IMPORTANT! Know port number for client class

			while(true){
				try {
					waitForConnections();//1: start up, wait to connect
					setupStreams(); 	//2: setup connection stream between computers
					whileChatting();	//3: once connections set up, send messages between one another

					//connect to have conversation	
				} catch(EOFException eofException) {
					//connection has ended
					showMessage("\n Server ended the connection.");
				} finally {
					closeApp();
				}
			}
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}


	//wait for connection, then display information
	private void waitForConnections() throws IOException {
		showMessage(" Waiting for someone to connect... \n");
		//connection = new Socket(clientIP, 49670);

		connection = server.accept();
		showMessage("Now connected to " + connection.getInetAddress().getHostName());
	}

	//get stream to send and receive data
	private void setupStreams() throws IOException {
		//setup pathway to contact someone else
		output = new ObjectOutputStream(connection.getOutputStream());
		//instant messengers deal with buffers & bytes, sometimes bytes get lost in buffer
		output.flush(); //refresh: sometimes data gets lost. Push or flush rest of data to person
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup! \n");

	}

	private void whileChatting() throws IOException{
		//while chatting with client
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n I don't know that object type");
			}
		}while(!message.equals("Server - END"));
	}


	//close streams and sockets after done chatting
	private void closeApp(){
		showMessage("\n Closing connectionn... \n");
		ableToType(false);
		try{
			output.close(); //closes streams to them
			input.close(); //closes stream from them
			connection.close(); //close main connection (socket)
		} catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//connect to client
	private void connectToClient() throws IOException{
		showMessage("Attempting connection... \n");

		connection = new Socket(InetAddress.getByName(clientIP), 10404);
		showMessage("Connected: " + connection.getInetAddress().getHostName());

	}

	//send a message to client
	private void sendMessage(String message){
		try{
			output.writeObject("Server -  " + message); //server would be where username is 
			output.flush();
			showMessage("\n SERVER - " + message);
		}catch(IOException ioException){
			//if message was unable to send
			chatWindow.append("\n ERROR: I can not send that message :O !! ");
		}
	}

	//UPDATE CHAT WINDOW
	private void showMessage(String message){
		SwingUtilities.invokeLater( //since dealing with threads... 
				new Runnable(){
					public void run(){ //method gets called when GUI is updated
						chatWindow.append(message);
					}
				}
		);
	}

	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater( //since dealing with threads... 
				new Runnable(){
					public void run(){ //method gets called when GUI is updated
						userText.setEditable(tof);
					}
				}
		);

	}

}
