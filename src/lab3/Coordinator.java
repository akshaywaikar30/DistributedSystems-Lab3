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
import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent; 			
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Coordinator extends JFrame implements ActionListener 
{
	final static int ServerPort = 1238;		//port number for communication
	static Timer timer = new Timer();		//declaring variable for timer
	static int count = 0;				
	static int commitcount=0;	
	static Socket sock;
	static String msg =  "";
	static int once=0;
	static int counter=0;
	static  DataInputStream dis;
    static DataOutputStream dos;
    //stores the date format for http message 
    static String date=java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.systemDefault())).toString();

	static JTextArea responseArea;			//declaring variables for displaying messages
	static JTextArea displayResponse;
	JButton sendBtn;							//declares a send button				
	JScrollPane scroll;
	
	public void initializeGUI() 
    { 	
    	this.setTitle("Coordinator");																								
		this.setSize(900, 800);																								
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);																			
		getContentPane().setLayout(null);																						

		displayResponse = new JTextArea();							// creates TextArea to display messages broadcasted by server
		displayResponse.setBounds(10, 0, 880, 400);					//This determines size of the TextArea
		displayResponse.setEditable(false);							//cannot edit this
		add(displayResponse);										//adds the TextArea to the container
		
		scroll = new JScrollPane (displayResponse, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 0, 880, 400);				//to add scroll
		add(scroll);
		//setVisible (true);
		
		responseArea = new JTextArea();							// creates a TextArea for client to type messages
		responseArea.setBounds(10, 450, 880, 100);				//This determines size of the TextArea
		add(responseArea);											//adds the TextArea to the container

		sendBtn = new JButton("SEND");						// creates button for client to send the text which is txtFromClient
		sendBtn.setBounds(400, 600, 100, 25);				//This determines size of the TextArea
		sendBtn.addActionListener(this);						//passes button object to send data after clicking it
		add(sendBtn);										//adds button to the container

		this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) 			//method gets called on click of send buttons
    {
    	try 
        {
    		if (e.getSource().equals(sendBtn)) 		//It checks which button to be called
    		{
    			sendVoteRequest();					//calls the sendVoteRequest() function
            } 
    	}
        catch (Exception e1) {		//Handles exception if any occurred
            e1.printStackTrace();
        }
    }
	
	void sendVoteRequest() throws IOException
	{
		
		displayResponse.append(responseArea.getText() + ": VOTE_REQUEST\n");
		int len = responseArea.getText().length() + 14;
		String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
				"Content-Length: " + len + "\nUser-Agent: Chat App\n";						//http format
		dos.writeUTF(httpMsg + " " + responseArea.getText() + ": VOTE_REQUEST\n");		//sends data across the network and InputStream can accept it on other end
		String writetofile=httpMsg+responseArea.getText()+" : VOTE_REQUEST\n";
		Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt"), writetofile.getBytes(), StandardOpenOption.APPEND);
		responseArea.setText("");			//displays the message from client on to the server with http post method
		
		int timerVal = 40;

		timer.scheduleAtFixedRate(new TimerTask()
		{
			int i = timerVal;
			public void run() // method is executed to start timer 
			{
				System.out.println(i--);		//reduces timer by 1 
				try 
				{		
					if(msg.contains("PRECOMMIT"))			//checks if message contains precommit
					{
						msg = "";
						count++;						//increments it if message contains precommit
					}
					
					else if ( msg.contains("ABORT") ||i < 0 && commitcount ==0 )  //checks if message contains abort or timer is less than 0, if true sends global abort
					{	
						displayResponse.append("GLOBAL ABORT\n");
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 12\nUser-Agent: Chat App\n";
						dos.writeUTF(httpMsg + " GLOBAL ABORT");
						Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt"), "Global ABORT\n".getBytes(), StandardOpenOption.APPEND);

						timer.cancel();		//cancels the timer
					}		
					
					if(count==3) 	//loop to check if it has got 3 precommits, enter if the condition is true and sends ack to participants
					{
						
						if(once==0) {
						displayResponse.append("ACK_Cordinator\n");
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 13\nUser-Agent: Chat App\n";
						Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt"), "ACK_Cordinator\n".getBytes(), StandardOpenOption.APPEND);
						dos.writeUTF(httpMsg + " ACK_Cordinator");
						once++;
						}
						
					}
					
					if(msg.contains("ACK_Participant")) {		//increases counter if participants sends ACK
						msg="";
						
						commitcount++;
					}
					
					if(msg.contains("NEED DECISION")&&count==3) {			//runs this loop if has recieved need decision and has recieved 3 precommits
						if(counter<=2) {
						
						displayResponse.append("GLOBAL COMMIT\n");		//sends global commit and writes to its textarea and also writes to file
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 13\nUser-Agent: Chat App\n";
						Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt"), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
						dos.writeUTF(httpMsg + " GLOBAL COMMIT\n");
					
						counter++;
						timer.cancel();					//cancels the timer
						}
					}
					
					if(commitcount==3) {					//exceutes if it recieves 3 ack from 3 participants  and sends Global Commit
						displayResponse.append("GLOBAL COMMIT");
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 13\nUser-Agent: Chat App\n";
						//writes to file
						Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt"), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
						dos.writeUTF(httpMsg + " GLOBAL COMMIT");
						timer.cancel();	//cancels the timer
					}
					else if(commitcount>=1 && i <= 0) {					//exceutes if it recieves 3 ack from 3 participants  and sends Global Commit
						displayResponse.append("GLOBAL COMMIT");
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 13\nUser-Agent: Chat App\n";
						//writes to file
						Files.write(Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt"), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
						dos.writeUTF(httpMsg + " GLOBAL COMMIT");
						timer.cancel();	//cancels the timer
					}
					
				}
				catch (IOException e) 		//catches exception if any
				{										
					e.printStackTrace();
				}
				}       
		}, 0, 1000);						

	}
	public static void main(String[] args) throws IOException 
	{	
		
		new Coordinator().initializeGUI();		//calls the function to exceute gui properties

		// TODO Auto-generated method stub
		InetAddress ip = InetAddress.getByName("localhost");
		sock = new Socket(ip, ServerPort);
		dis = new DataInputStream(sock.getInputStream());	//reads the message from InputStream
		dos = new DataOutputStream(sock.getOutputStream());	//accepts the data from the OutputStream
		File yourFile = new File("C:\\Users\\aksha\\eclipse-workspace\\lab3\\Coordinator.txt");		//path to create a local file
		yourFile.createNewFile();
		DataInputStream dis = new DataInputStream(sock.getInputStream());									//reads incoming messages
		Path display = Paths.get("C:\\Users\\aksha\\eclipse-workspace\\lab3", "Coordinator.txt");
		try {
			byte[] dispArray = Files.readAllBytes(display);												//reads data from local file upon coordinator starting

			String displayString = new String(dispArray, "ISO-8859-1");
			displayResponse.append(displayString);																//displays data on the text area
		} catch (IOException e) {
			System.out.println(e);																			//catches exception if any
		}
		while(true)
	    {
	       	msg = dis.readUTF();				//keeps reading the message it recieves
	       	displayResponse.append(msg+"\n");
	    }
	}
}

