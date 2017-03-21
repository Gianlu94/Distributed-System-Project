//package src.main.java;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.io.IOException;




import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Cancellable;
import scala.concurrent.duration.Duration;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

public class Client {
	static private String remotePath = null; // Akka path of the bootstrapping peer
	static private int myId; // ID of the local node
    static private ActorRef clientActor;

    
    public static void sendLeave (String ip, String port){

		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		clientActor.tell(new DoLeave(), null);

	}

    public static void sendRead (String ip, String port, Integer itemKey){

		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		clientActor.tell(new DoRead(itemKey), null);

	}
    
    public static class Node extends UntypedActor {
    	
		public void preStart() {
		}
	    
        public void onReceive(Object message) {
        	if(message instanceof DoLeave){
        		getContext().actorSelection(remotePath).tell(new Message.LeaveMessage(), getSelf());
        	} 
        	else if(message instanceof DoRead){
        		getContext().actorSelection(remotePath).tell(new Message.ClientToCoordReadRequest(((DoRead) message).itemKey), getSelf());
        	}
        	else if(message instanceof Message.ReadReplyToClient){
        		Item itemRead = ((Message.ReadReplyToClient) message).item;
        		Integer itemKey = ((Message.ReadReplyToClient) message).itemKey;
        		Boolean isExisting = ((Message.ReadReplyToClient) message).isExisting;

        		if (!isExisting){
        			System.out.println("The item " + itemKey + " requested does not exist in the system");
        		} else {
        			if(itemRead != null){
        				System.out.println("System replied to the read request: " + itemRead.toString());
        			} else { 
        				System.out.println("System did not manage to compute the read request for the item: " + itemKey + " because Timeout was hit");

        			}
        		}
    			goBackToTerminal();
        	}
        }
    }

	
	public static class DoLeave implements Serializable {}
	
	public static class DoRead implements Serializable {
		Integer itemKey;
		
		public DoRead(Integer itemKey) {
			super();
			this.itemKey = itemKey;
		}
		
	}


	private static void goBackToTerminal(){
		System.out.print(">> ");
	}
	
    /*
        Terminal to receive client commands
     */
    private static void terminal(){

	    Scanner input; //to receive keyboard input stream
	    String inputCommand;
	    String [] tokensInput; //split command in tokens
	    Integer tokensNumber;
	    Integer keyItem;

	    input = new Scanner(System.in);
	    while (true){
		    System.out.print(">> ");
		    inputCommand = input.nextLine();
		    tokensInput = inputCommand.split(" ");
			tokensNumber = tokensInput.length;

		    switch (tokensInput[0].toLowerCase()){
			    case "e":
			    case "exit":
			    	System.exit(0);
			    case "java":
				    if ((tokensInput.length < 5) || (tokensNumber > 7)){
					    System.out.println("ERROR: Number of parameters wrong");
				    }
				    else if(tokensInput[1].toLowerCase().equals("client")){
					    System.out.println("*****"+tokensInput[4].toLowerCase());
					    switch(tokensInput[4].toLowerCase()){
						    case "read":
						    	    try{
								        keyItem = Integer.valueOf(tokensInput[5]);
							        }catch(Exception e){
								        System.out.println("ERROR: Key not correct");
								        break;
							        }
							        sendRead(tokensInput[2],tokensInput[3], keyItem);
						    	    break;
						    case "write":
							    System.out.println("NOT IMPLEMENTED YET");
						    	break;
						    case "leave":
							    //System.out.println("****"+tokensInput[2].toLowerCase());
						    	sendLeave(tokensInput[2],tokensInput[3]);
						    	break;
						    default:
						    	System.out.println("ERROR: command unknown");
							    break;

					    }

				    }
				    else{
					    System.out.println("ERROR: command unknown");
				    }
				    break;
			    default:
			    	System.out.println("ERROR: No commands or command unknown");
				    break;


		    }


	    }
    }
	
	private static boolean ping (String address, String port, int timeout){
		SocketAddress sockaddr = new InetSocketAddress(address, Integer.parseInt(port));
		Socket socket = new Socket();
		boolean online = true;
		try {
			socket.connect(sockaddr, timeout);
		} catch (SocketTimeoutException stex) {
			// treating timeout errors separately from other io exceptions
			// may make sense
			online=false;
		} catch (IOException iOException) {
			online = false;    
		} finally {
			try {
				socket.close();
			} catch (IOException ex) {
			}

		}
		return online;
	}


	//TO LAUNCH NODE APP FROM NODE CONFIGURATION FOLDER USE:
	//java -cp $AKKA_CLASSPATH:.:../../../ main.java.Client
    public static void main(String[] args) {
	    //Config config = ConfigFactory.load("client");
	    File clientFile = new File("./main/resources/client.conf");
	    Config clientConfig = ConfigFactory.parseFile(clientFile);
	    final ActorSystem system;

	    /*
		if (!(args.length >= 3 && args.length <= 5)) {
			System.out.println("Wrong number of arguments: [remote_ip remote_port]");
			return;
		}
		
		String ip = args[0];
		String port = args[1];
		boolean online = ping(ip, port, 10000);
		if (!online){
			System.out.println("Node with address: " + ip + ":" + port + " not reachable");
		} else {			
		}
		*/

	    system = ActorSystem.create("client_system", clientConfig);

	    clientActor = system.actorOf(Props.create(Node.class),"client");

	    terminal();





		
    }
}
