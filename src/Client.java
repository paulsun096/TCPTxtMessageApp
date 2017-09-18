import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client extends JFrame{

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input; //from the server going into this computer
    private String message = "";
    private String serverIP; //ip address of person you are talking to
    private Socket connection;
    //constructor, only able to connect to server
    public Client(String host) {
        super("Client");
        serverIP = host;
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

        Font font = new Font("Times New Roman", Font.PLAIN, 19);
        userText.setFont(font);

        chatWindow = new JTextArea();
        chatWindow.setFont(font);
        chatWindow.setEditable(false);
        chatWindow.setLineWrap(true);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(450, 500);
        setVisible(true);
    }

    //connect to server
    public void startRunning() {
        try{
            connectToServer();//connecting to one specific computer
            setupStream();
            whileChatting();
        }catch(EOFException eofException){
            showMessage("\n Client terminated connection");
        }catch(IOException ioException){
            ioException.printStackTrace();
        }finally{
            closeApp();
        }
    }

    //connect to server
    private void connectToServer() throws IOException{
        showMessage("Attempting connection... \n");
        connection = new Socket(InetAddress.getByName(serverIP), 10404);
        showMessage("Connected: " + connection.getInetAddress().getHostName());

    }

    //set up steams to send and receive messages
    private void setupStream() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nstreams are setup \n");
    }
    //while chatting with server
    private void whileChatting() throws IOException{
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

    private void closeApp(){
        showMessage("\n closing crap down...");
        ableToType(false);

        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
    //sends messages to server, passes to message parameter. displays on gui
    private void sendMessage(String message){
        try{
            output.writeObject("Client - " + message);
            output.flush();
            showMessage("\nYou - " + message + "\n");
        }catch(IOException ioException){
            chatWindow.append("\n something messed up sending message!");
        }
    }
    //change/update chat window
    private void showMessage(final String m){
        SwingUtilities.invokeLater(
                new Runnable(){
                    public void run(){
                        chatWindow.append(m);
                    }
                }
        );
    }

    //gives user permission to type into the text body
    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run(){
                        userText.setEditable(tof);
                    }
                }
        );
    }
}