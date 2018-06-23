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
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
 
public class Client2 extends JFrame implements ActionListener 
{
    final static int ServerPort = 1238;//Port to connect to server
    static Socket s;//Socket for communicating messages
    static  DataInputStream dis;//reads the messages from inputstream
    static DataOutputStream dos;//sends message across the network
    static Scanner scn;
    static String msg =  "";//To recieve message
    static Timer timer = new Timer();//object for timer
    static boolean coordinatorResp = false;
    static int count = 0;
    static boolean needDecisionSent = false;//flags
    static boolean decisionRecv = false;
    static int crashcount=0;
    static int abrtcount=0;
    static boolean coack=false;
    static File logfile;
    static String cordinatorString;
    //date format for http
    static String date=java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)).toString();
    static String abc="ACK_Cordinator";
    static JTextArea responseArea;
	static JTextArea displayResponse;							
	JButton sendBtn;//Send button	
	JButton precommitbtn;//precommit button
	JButton abortBtn;					//abort button							
	JButton commitBtn;//Ack buttons
	JScrollPane scroll;
    
    public void initializeGUI() //it runs the constructor and enables all gui properties	
    { 	
    	this.setTitle("Client2");																								
		this.setSize(900, 800);																								
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				//Creating a Frame for client																				
		getContentPane().setLayout(null);																						

		displayResponse = new JTextArea();								
		displayResponse.setBounds(10, 0, 880, 400);						// creates TextArea to display messages broadcasted by server
		displayResponse.setEditable(false);							
		add(displayResponse);
		
		scroll = new JScrollPane (displayResponse, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 0, 880, 400);
		add(scroll);//creates a scroll bar
		//setVisible (true);
		
		responseArea = new JTextArea();							
		responseArea.setBounds(10, 450, 880, 100);				// creates a TextArea for client to type messages
		add(responseArea);											//This determines size of the TextArea

		sendBtn = new JButton("SEND");						
		sendBtn.setBounds(100, 600, 110, 25);				//This determines size of the TextArea
		sendBtn.addActionListener(this);					
		add(sendBtn);											//adds the TextArea to the container		
		
		precommitbtn= new JButton("PRECOMMIT");
		precommitbtn.setBounds(250, 600, 110, 25);				//This determines size of the TextArea
		precommitbtn.addActionListener(this);					
		add(precommitbtn);	//adds the TextArea to the container		
		
		commitBtn=new JButton("ACK_Participant");					    	
		commitBtn.setBounds(400, 600, 130, 25);			//This determines size of the TextArea
		commitBtn.addActionListener(this);					
		add(commitBtn);											//adds the TextArea to the container		
		commitBtn.setVisible(false);
		abortBtn=new JButton("ABORT");						  
		abortBtn.setBounds(550, 600, 110, 25);				//This determines size of the TextArea
		abortBtn.addActionListener(this);					
		add(abortBtn);											//adds the TextArea to the container		

		this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) 
    {
    	try 
        {
    		if (e.getSource().equals(sendBtn)) 
    		{
            	sendResponse();//method gets called on click of send button
            }
    		else if(e.getSource().equals(precommitbtn)) {
    			
    			sendPreCommitResponse();//method gets called on click of send button
    		}
        	else if(e.getSource().equals(commitBtn))
        	{
        		sendCommitResponse();//method gets called on click of send button
        	}
        	else if(e.getSource().equals(abortBtn))
        	{
        		sendAbortResponse();//method gets called on click of send button
        	}
    	}
        catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
 void sendPreCommitResponse() throws IOException{//exceutes if the precommit button is pressed
    	
    	if(!s.isClosed())//checks of socket is not closed
    	{
			String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
					"Content-Length: 6\nUser-Agent: Chat App\n";
			dos.writeUTF(httpMsg+" PRECOMMIT\n");//checks of socket is not closed
    		//dos.writeUTF("COMMIT");
    	}
    	Files.write(Paths.get(logfile.getName()), cordinatorString.getBytes(), StandardOpenOption.APPEND);
    	displayResponse.append("PRECOMMIT\n");
    	
    	Files.write(Paths.get(logfile.getName()), "PRECOMMIT\n".getBytes(), StandardOpenOption.APPEND);
    	abortBtn.setVisible(false);//hides abort button
    	commitBtn.setVisible(true);//displays commit button
    	
    	int timerVal = 30;	//timer
		timer.scheduleAtFixedRate(new TimerTask()
		{
			int countdownVal = timerVal;
			public void run() 
			{
				System.out.println(countdownVal--);//decreases the timer by 1
				if(msg.contains("ACK_Cordinator")) {
					coack=true;
				}
				if(msg.contains("GLOBAL COMMIT")) 	//checks if it has global commit
				{
					decisionRecv = true;
					try {
						Files.write(Paths.get(logfile.getName()), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND); //writes global commit to file
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timer.cancel();
				}
				else if( msg.contains("GLOBAL ABORT"))   //checks if message has global abort and writes it to file and cancels the timer
				{
					decisionRecv = true;
					try {
						Files.write(Paths.get(logfile.getName()), "GLOBAL ABORT\n".getBytes(), StandardOpenOption.APPEND);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timer.cancel();
				}
				else if(countdownVal < 0 && coack==true) {//checks if timer is less than 0 and if message contains coordinator ack
    				if(crashcount==0) {
    				displayResponse.append("GLOBAL COMMIT\n");
    				String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    						"Content-Length: 13\nUser-Agent: Chat App\n";
    				try {
    					Files.write(Paths.get(logfile.getName()), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
    					dos.writeUTF(httpMsg + " GLOBAL COMMIT\n");
    					timer.cancel();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				crashcount++;
    				}
    			}
				else if(countdownVal < 0 && !msg.equals(abc)) {
    				displayResponse.append("GLOBAL ABORT\n");
    				String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    						"Content-Length: 13\nUser-Agent: Chat App\n";
    				try {
    					Files.write(Paths.get(logfile.getName()), "GLOBAL ABORT\n".getBytes(), StandardOpenOption.APPEND);
    					dos.writeUTF(httpMsg + " GLOBAL ABORT\n");
    					timer.cancel();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
				else if(countdownVal < 0 && decisionRecv == false)//checks if timer is less than 0and if message contains coordinator ack
				{
					if(!msg.contains("NEED DECISION"))
					{
						if(!needDecisionSent)
						{
							count = 0;
							displayResponse.append("\n");
							needDecisionSent = true;
							String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
									"Content-Length: 13\nUser-Agent: Chat App\n";
							//dos.writeUTF(httpMsg + " NEED DECISION");
						}
						
						while(true)
						{
							try
							{
								if(msg.contains("ACK_Participant")) 
								{
									msg = "";
									count++;
								}
								else if(msg.contains("ABORT"))  
								{
									msg = "";
									displayResponse.append("GLOBAL ABORT\n");
									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
											"Content-Length: 12\nUser-Agent: Chat App\n";
									dos.writeUTF(httpMsg +" GLOBAL ABORT");
									break;
								}	
								else if(countdownVal < 0 && msg.contains("ACK_Cordinator")) {
									if(crashcount==0) {
									displayResponse.append("GLOBAL COMMIT\n");
									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
											"Content-Length: 13\nUser-Agent: Chat App\n";
									try {
										Files.write(Paths.get(logfile.getName()), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
										dos.writeUTF(httpMsg + " GLOBAL COMMIT\n");
										timer.cancel();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									crashcount++;	
								}
								}
								else if(countdownVal < 0 &&  !msg.equals(abc)) {
									if(abrtcount==0) {
									displayResponse.append("GLOBAL ABORT\n");
									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
											"Content-Length: 13\nUser-Agent: Chat App\n";
									try {
										Files.write(Paths.get(logfile.getName()), "GLOBAL ABORT\n".getBytes(), StandardOpenOption.APPEND);
										dos.writeUTF(httpMsg + " GLOBAL ABORT\n");
										timer.cancel();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									abrtcount++;
								}
									}
								if(count==2) 	
								{	
									displayResponse.append("GLOBAL COMMIT\n");
									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
											"Content-Length: 13\nUser-Agent: Chat App\n";
									dos.writeUTF(httpMsg +" GLOBAL COMMIT");
									Files.write(Paths.get(logfile.getName()), cordinatorString.getBytes(), StandardOpenOption.APPEND);
									break;
								}
							}
							catch(IOException e) 
							{										
								e.printStackTrace();
							}
						}
						timer.cancel();
					}
				}
				else if(countdownVal < 0 && decisionRecv)
					timer.cancel();
			}       
		}, 0, 1000);
    } 
    
    void sendCommitResponse() throws IOException
    {
    	displayResponse.append("ACK_Participant\n");
    	
    	try 
    	{
            // write on the output stream
        	if(!s.isClosed())
        	{
        		String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
						"Content-Length: 6\nUser-Agent: Chat App\n";
        		dos.writeUTF(httpMsg + " ACK_Participant");
        	}
        	
        	Files.write(Paths.get(logfile.getName()), "ACK_Participant\n".getBytes(), StandardOpenOption.APPEND);
        	int timerVal = 30;
    		//timer = new Timer();			
    		timer.scheduleAtFixedRate(new TimerTask()
    		{
    			int countdownVal = timerVal;
    			public void run() 
    			{
    				System.out.println(countdownVal--);
    				
    				if(msg.contains("GLOBAL COMMIT")) 
    				{
    					decisionRecv = true;
    					try {
							Files.write(Paths.get(logfile.getName()), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					timer.cancel();
    				}
    				else if( msg.contains("GLOBAL ABORT"))  
    				{
    					decisionRecv = true;
    					try {
							Files.write(Paths.get(logfile.getName()), "GLOBAL ABORT\n".getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					timer.cancel();
    				}
    				else if(countdownVal < 0 && decisionRecv == false)
    				{
    					if(!msg.contains("NEED DECISION"))
    					{
    						if(!needDecisionSent)
    						{
    							count = 0;
    							displayResponse.append("\n");
    							needDecisionSent = true;
								String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
										"Content-Length: 13\nUser-Agent: Chat App\n";
								//dos.writeUTF(httpMsg + " NEED DECISION");
								timer.cancel();
    						}
    						
    						while(true)
    						{
    							try
    							{
    								if(msg.contains("ACK_Participant")) 
    								{
    									msg = "";
    									count++;
    								}
    								else if(msg.contains("ABORT"))  
    								{
    									msg = "";
    									displayResponse.append("GLOBAL ABORT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 12\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg +" GLOBAL ABORT");
    									break;
    								}	
    								
    								else if(countdownVal<0 && msg.contains(abc)){
    									if(crashcount==0) {
    										displayResponse.append("GLOBAL COMMIT\n");
    										String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    												"Content-Length: 13\nUser-Agent: Chat App\n";
    										try {
    											Files.write(Paths.get(logfile.getName()), "GLOBAL COMMIT\n".getBytes(), StandardOpenOption.APPEND);
    											dos.writeUTF(httpMsg + " GLOBAL COMMIT\n");
    											timer.cancel();
    										} catch (IOException e) {
    											// TODO Auto-generated catch block
    											e.printStackTrace();
    										}
    										crashcount++;	
    									}
    									
    			
    		}
							
    								if(count==2) 	
    								{	
    									displayResponse.append("GLOBAL COMMIT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 13\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg +" GLOBAL COMMIT");
    									Files.write(Paths.get(logfile.getName()), cordinatorString.getBytes(), StandardOpenOption.APPEND);
    									break;
    								}
    							}
    							catch(IOException e) 
								{										
									e.printStackTrace();
								}
    						}
    						timer.cancel();
    					}
    				}
    				else if(countdownVal < 0 && decisionRecv)
    					timer.cancel();
    			}       
    		}, 0, 1000);
        } 
    	catch (IOException e) 
    	{
            e.printStackTrace();
        }
    }
    
    void sendAbortResponse()
    {
    	displayResponse.append("ABORT\n");
    	try 
    	{
            // write on the output stream
        	if(!s.isClosed())
        	{
        		String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
						"Content-Length: 5\nUser-Agent: Chat App\n";
        		dos.writeUTF(httpMsg +" ABORT");
        	}
        	
        	int timerVal = 15;
    		//timer = new Timer();			
    		timer.scheduleAtFixedRate(new TimerTask()
    		{
    			int countdownVal = timerVal;
    			public void run() 
    			{
    				System.out.println(countdownVal--);
    				
    				if(msg.contains("GLOBAL COMMIT")) 
    				{
    					decisionRecv = true;
    					timer.cancel();
    				}
    				else if( msg.contains("GLOBAL ABORT"))  
    				{
    					decisionRecv = true;
    					timer.cancel();
    				}
    				else if(countdownVal < 0 && decisionRecv == false)
    				{
    					if(!msg.contains("NEED DECISION"))
    					{
    						if(!needDecisionSent)
    						{
    							count = 0;
    							displayResponse.append("\n");
    							needDecisionSent = true;
								String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
										"Content-Length: 13\nUser-Agent: Chat App\n";
								//dos.writeUTF(httpMsg + " NEED DECISION");
    						}
    						
    						while(true)
    						{
    							try
    							{
    								if(msg.contains("ACK_Participant")) 
    								{
    									msg = "";
    									count++;
    								}
    								else if(msg.contains("ABORT"))  
    								{
    									msg = "";
    									displayResponse.append("GLOBAL ABORT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 12\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg +" GLOBAL ABORT");
    									break;
    								}	
							
    								if(count==2) 	
    								{	
    									displayResponse.append("GLOBAL COMMIT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 13\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg + " GLOBAL COMMIT");
    									break;
    								}
    							}
    							catch(IOException e) 
								{										
									e.printStackTrace();
								}
    						}
    						timer.cancel();
    					}
    				}
    				else if(countdownVal < 0 && decisionRecv)
    					timer.cancel();
    			}       
    		}, 0, 1000);
        }
    	catch (IOException e) 
    	{
            e.printStackTrace();
        }    	
    }
    
    void sendResponse() throws IOException
    {
    	String msg = responseArea.getText();
    	responseArea.setText("");
    	displayResponse.append(msg+"\n");
    	
        try 
        {
            // write on the output stream
        	if(!s.isClosed())
        		dos.writeUTF(msg);
        	
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }		
    }
    
    public static void fileCreation() throws IOException
    {
    	if(!new File("client2.txt").exists())
    	{
    		logfile = new File("client2.txt");
    		logfile.createNewFile();
    	}
    	else if(new File("client2.txt").exists())
    	{
    		logfile = new File("client2.txt");
    	}
    	   	
    }
 
    public static void main(String args[]) throws UnknownHostException, IOException 
    {
    	Client2 cl = new Client2(); 
    	cl.initializeGUI();

    	fileCreation();
    	
    	if(logfile.exists())
    	{
    		BufferedReader br = null;
    		FileReader fr = null;
		
    		fr = new FileReader(logfile.getName());
    		br = new BufferedReader(fr);

    		String sCurrentLine;
    		while ((sCurrentLine = br.readLine()) != null) 
    		{
    			displayResponse.append(sCurrentLine + "\n");
    		}
    		displayResponse.append("\n");
    	}
    	
        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");
         
        // establish the connection
        s = new Socket(ip, ServerPort);
         
        // obtaining input and out streams
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());
        
        while(true)
        {
        	msg = dis.readUTF();
        	displayResponse.append(msg+"\n");
        	String copyMsg = msg;
        	String[] writeTofile = copyMsg.split(":");
        	if(writeTofile.length > 2)
        		cordinatorString = writeTofile[1]+"\n";
        	//Files.write(Paths.get(logfile.getName()), writeTofile[1].getBytes(), StandardOpenOption.APPEND);
        }
    }
}