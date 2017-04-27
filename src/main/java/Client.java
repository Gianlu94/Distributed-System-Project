//package src.main.java;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.net.*;
import java.io.IOException;




import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

public class Client {
	static private String remotePath = null; // Akka path of the bootstrapping peer
	static private int myId; // ID of the local node
    static private ActorRef clientActor;


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
								try{
									keyItem = Integer.valueOf(tokensInput[5]);
								}catch(Exception e){
									System.out.println("ERROR: Key not correct");
									break;
								}
								sendWrite(tokensInput[2],tokensInput[3],keyItem,tokensInput[6]);
								break;
							case "leave":
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


	//tell to the client actor to do a leave
    public static void sendLeave (String ip, String port){
		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		clientActor.tell(new Message.DoLeave(), null);
	}


	//tell to the (current) client actor to start a read
    public static void sendRead (String ip, String port, Integer itemKey){
		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		clientActor.tell(new Message.DoRead(itemKey), null);

	}

	//tell to the (current) client actor to start a write
	public static void sendWrite (String ip, String port, Integer itemKey, String value){
		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		clientActor.tell(new Message.DoWrite(itemKey,value), null);

	}

    
    public static class Node extends UntypedActor {
    	
		public void preStart() {
		}
	    
        public void onReceive(Object message) {
        	if(message instanceof Message.DoLeave){
        		getContext().actorSelection(remotePath).tell(new Message.Leave(), getSelf());
        	}
        	else if(message instanceof Message.DoRead){
        		getContext().actorSelection(remotePath).tell(new Message.ClientToCoordReadRequest(
        				((Message.DoRead) message).itemKey), getSelf());
        	}
        	else if(message instanceof Message.DoWrite){
		        Message.DoWrite msg = (Message.DoWrite) message;
		        getContext().actorSelection(remotePath).tell(new Message.ClientToCoordWriteRequest(msg.itemKey,msg.value),getSelf());
	        }
        	else if(message instanceof Message.ReadReplyToClient){ //reesponse from the coordinator
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

        		//Client exits after receiving response
		        System.exit(0);
        	}
        	else if (message instanceof Message.WriteReplyToClient){ //reesponse from the coordinator
		        Message.WriteReplyToClient msg = (Message.WriteReplyToClient)message;
		        if (!msg.failed) {
			        if (msg.isExisting) {
				        System.out.println("Updating success-> Item " + msg.item.toString());
			        } else {
				        System.out.println("Creation success-> Item " + msg.item.toString());
			        }
		        }
		        else{
			        System.out.println("System did not manage to compute the write request for the item: " +
					        msg.item.getKey() + " "+msg.item.getValue() +" because Timeout was hit");
		        }

		        //Client exits after receiving response
		        System.exit(0);
	        }
	        else if (message instanceof Message.rejectWrite){
		        Message.rejectWrite msg = (Message.rejectWrite)message;
		        System.out.println("Write rejected: network with less than of "+msg.N +" nodes" );

		        //Client exits after receiving response
		        System.exit(0);
	        }
	        else {
		        unhandled(message);
	        }
        }
    }


    public static void main(String[] args) {
	    File clientFile = new File("client.conf");
	    Config clientConfig = ConfigFactory.parseFile(clientFile);
	    final ActorSystem system;

	    system = ActorSystem.create("client_system", clientConfig);

	    clientActor = system.actorOf(Props.create(Node.class),"client");

	    //call client terminal
	    terminal();
		
    }
}
