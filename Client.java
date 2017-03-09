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

	
    public static class Node extends UntypedActor {
    	
		public void preStart() {
		}
	    
        public void onReceive(Object message) {
			if(message instanceof DoLeave){
				getContext().actorSelection(remotePath).tell(new LeaveMessage(), getSelf());
			}
		}
    }
    
	public static class LeaveMessage implements Serializable {}
	
	public static class DoLeave implements Serializable {}


    
    /*
        Terminal to receive client commands
     */
    private static void terminal(){

	    Scanner input; //to receive keyboard input stream
	    String inputCommand;
	    String [] tokensInput; //split command in tokens
	    Integer tokensNumber;

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

					    switch(tokensInput[2].toLowerCase()){
						    case "read":
							    System.out.println("NOT IMPLEMENTED YET");
						    	break;
						    case "write":
							    System.out.println("NOT IMPLEMENTED YET");
						    	break;
						    case "leave":
						    	sendLeave(tokensInput[3],tokensInput[4]);
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



    public static void main(String[] args) {
	    Config config = ConfigFactory.load("client");
	    final ActorSystem system;
	    final ActorRef clientActor;

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

	    system = ActorSystem.create("client_system", config);

	    clientActor = system.actorOf(Props.create(Node.class),"client");

	    terminal();





		
    }
}
