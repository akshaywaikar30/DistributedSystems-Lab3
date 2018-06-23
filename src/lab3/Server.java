//Name: AkshayMaheshWaikar
//ID: 1001373973
//Mahrsee, Rishabh. “Multi-Threaded Chat Application.” GeeksforGeeks, 17 June 2017, www.geeksforgeeks.org/multi-threaded-chat-application-set-1/.
//Mahrsee, Rishabh. "Multi-Threaded Chat Application." GeeksforGeeks, 17 June 2017, www.geeksforgeeks.org/multi-threaded-chat-application-set-2/.
//https://stackoverflow.com/questions/15247752/gui-client-server-in-java
//http://www.jmarshall.com/easy/http/ HTTP Made Really Easy. 
//Pseudo-code from Chapter 8 of Textbook
//https://docs.oracle.com/javase/8/docs/technotes/guides/lang/Countdown.java
//http://www.java2s.com/Tutorials/Java/java.nio.file/Files/Java_Files_readAllBytes_Path_path_.htm
//Referred Textbook for 3PC protocol
 package lab3;
import java.io.*;    //All Header files
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
 
// Server class
public class Server extends JFrame 
{
 
    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();
     
    // counter for clients
    static int i = 0;
    
    static JTextArea displayResponse;   // creating text field to show client response on server
	JScrollPane scroll;					//creates the scroll
    
    public void initialize()
    {
    	this.setTitle("Server");						//Set all the GUI properties																		
		this.setSize(900, 800);								//This determines size 													
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);																			
		getContentPane().setLayout(null);																						

		displayResponse = new JTextArea();								
		displayResponse.setBounds(10, 0, 880, 600);			//This determines size of the TextArea			
		displayResponse.setEditable(false);							
		add(displayResponse);						//adds the TextArea to the container
		
		scroll = new JScrollPane (displayResponse, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 0, 880, 600);				//to create vertical and horizontal scroll
		add(scroll);
		this.setVisible(true);
    }
 
    public static void main(String[] args) throws IOException 
    {
    	Server ser = new Server();							 // Calling Constructor
    	ser.initialize();										//calls the method for GUI implementation
    	Path display = Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3", "server.txt");  //57-65 code for reading the file and displaying
    																							//on the textarea of sever
		try {
			byte[] dispArray = Files.readAllBytes(display);

			String displayString = new String(dispArray, "ISO-8859-1");
			displayResponse.append(displayString);
		} catch (IOException e) {
			System.out.println(e);
		}
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1238);				// Declaring the port number for communication
         
        Socket s;													// Creating socket for transferring messages
        
        // running infinite loop for getting
        // client request
        while (true) 
        {
            // Accept the incoming request
            s = ss.accept();
 
            //System.out.println("New client request received : " + s);
            displayResponse.append("New client request received : " + s + "\n");
             
            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
             
            //System.out.println("Creating a new handler for this client...");
            displayResponse.append("Creating a new handler for this client...\n");
            
            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);
 
            // Create a new Thread with this object.
            Thread t = new Thread(mtch);
             
            //System.out.println("Adding this client to active client list");
            displayResponse.append("Adding this client to active client list\n");
            
            // add this client to active clients list
            ar.add(mtch);
 
            // start the thread.
            t.start();
 
            // increment i for new client.
            // i is used for naming only, and can be replaced
            // by any naming scheme
            i++;
 
        }
    }
}
    class ClientHandler implements Runnable 			// ClientHandler class
    {
        Scanner scn = new Scanner(System.in);
        private String name;
        final DataInputStream dis;
        final DataOutputStream dos;
        Socket s;
        boolean isloggedin;
            
        // constructor
        public ClientHandler(Socket s, String name,
                                DataInputStream dis, DataOutputStream dos) {
            this.dis = dis;
            this.dos = dos;
            this.name = name;				//taking all the values needed by clienthandler constructor
            this.s = s;
            this.isloggedin=true;
        }
     
        @Override
        public void run() {
     
            String received;
            while (true) 
            {
                try
                {
                    // receive the string
                    received = dis.readUTF();
                    
                    Server.displayResponse.append(this.name + " : " + received + "\n");						//appends the response recieved from client on the textarea
                    File yourFile = new File("C:\\Users\\aksha\\eclipse-workspace\\lab3\\server.txt");		//system path to create a file
    				yourFile.createNewFile();																		//creates new file if it doesn't exist
    				Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\server.txt"), received.getBytes(),	//write the content to file
    						StandardOpenOption.APPEND);
                    if(received.equals("logout")){ 			//if client types logout it discontinues that client thread
                        this.isloggedin=false;
                        this.s.close();     			 //closes the socket with client which has logged out
                        Server.ar.remove(this.name);
                        break;
                    }
                     
                    // break the string into message and recipient part
                    StringTokenizer st = new StringTokenizer(received, "#");
                    String MsgToSend = st.nextToken();
                    //String recipient = st.nextToken();
     
                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : Server.ar) 
                    {
                        // if the recipient is found, write on its
                        // output stream
                        //if (mc.name.equals(recipient) && mc.isloggedin==true)
                    	if (!this.name.equals(mc.name) && mc.isloggedin==true)
                        {
                    		String copyMsg = MsgToSend;
                    		String[] writeTofile = copyMsg.split("\n");				//Separates actual message from http fromat
                    		int len = writeTofile.length;
                            mc.dos.writeUTF(this.name+" : "+writeTofile[len-1]);
                        }
                    }
                    
                } catch (IOException e) {
                    
                    e.printStackTrace();
                }
            }
            try
            {
                // closing resources
                this.dis.close();
                this.dos.close();
                 
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }