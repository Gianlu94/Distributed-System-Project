//package main.java;

import akka.actor.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NodeApp {
	static private String remotePath = null; // Akka path of the bootstrapping peer
	static private int myId; // ID of the local node
	static int N, R, W, T; // parameters: replication number, read quorum, write quorum and timeout
	static private ActorRef receiver;
	static private Boolean crashed;



	/*
		  Terminal to receive node commands
	*/
	private static void terminal(){
		Scanner input; //to receive keyboard input stream
		String inputCommand;
		String [] tokensInput; //split command in tokens
		Integer tokensNumber;
		String firstCommand;
		crashed = false;

		input = new Scanner(System.in);
		while (true) {
			System.out.print(">> ");
			inputCommand = input.nextLine();
			tokensInput = inputCommand.split(" ");

			firstCommand = tokensInput[0].toLowerCase();
			if (firstCommand.equals("crash")){
				crashed = true; //crashed state setted
			} else if ((tokensInput.length != 5) && (tokensInput.length != 1)){
				System.err.println("ERROR: Wrong number of parameters");
			}
			else{
				if ((firstCommand.equals("java"))&&
				(tokensInput[1].toLowerCase().equals("node"))){

					switch (tokensInput[2].toLowerCase()){
						case "join":
							if (crashed != true) { // if node is not crashed
								doJoin(tokensInput[3].toLowerCase(), tokensInput[4].toLowerCase());
							}
							break;
						case "recover":
								doRecovery(tokensInput[3].toLowerCase(), tokensInput[4].toLowerCase());
							break;
						default:
							System.err.println("ERROR: unknown command");
							break;
					}

				}else{
					System.err.println("ERROR: unknown command");
				}
			}
		}
	}

	//tell to the (current) node actor to start a join
	public static void doJoin (String ip, String port){

		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		receiver.tell(new Message.RequestJoin()  ,null);


	}

	//tell to the (current) node actor to start a recovery
	public static void doRecovery (String ip, String port){

		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		receiver.tell(new Message.RequestRecovery()  ,null);


	}
	
	// returns the node which is immediately after this node on the ring of partitioning
	public static ActorRef identifyNextNode(Map <Integer, ActorRef> nodes){
		ArrayList<Integer> keyArray = new ArrayList<Integer>(nodes.keySet());
		Collections.sort(keyArray);
		
		int nextNodeKey;
		int myIndex = keyArray.indexOf(myId);
		
		if(myIndex!=-1){
			nextNodeKey = keyArray.get((myIndex + 1) % keyArray.size());
			return nodes.get(nextNodeKey);
		}else{
			return null; // "null" would mean that in "nodes" there is not this actor, but there always should be
		}
	}
	
	//returns the list of nodes that are responsible for the item given as parameter
	public static List <Integer> getResponsibleNodes(Map<Integer,ActorRef> nodes, Integer itemKey){
		
		int replicationN = N; //a temporary value for N
		int keyNode;
		
		List <Integer> responsibleNodes = new ArrayList<Integer>();
		
		ArrayList<Integer> keyNodes = new ArrayList<Integer>(nodes.keySet());
		Collections.sort(keyNodes);

		for(int i = 0; i < keyNodes.size() && replicationN > 0; i++){
			keyNode = keyNodes.get(i);
			if (itemKey <= keyNode){ // I find all the elements that have to contain that item
				replicationN--;
				responsibleNodes.add(keyNode);
			}
		}

		//it means that the remaining elements that contains that item
		//are located at the beginning of the ring
		if (replicationN != 0){
			for(int i = 0; i < keyNodes.size() && replicationN > 0; i++){
				keyNode = keyNodes.get(i);
				replicationN--;
				responsibleNodes.add(keyNode);
			}
		}
		return responsibleNodes;
	}
	
	// returns a map containing the nodes mapped with the list of items the nodes become responsible for after the leaving
	private static Map<Integer, List<Item>> getNewResponsibleNodes (Map<Integer,ActorRef> nodes, Map<Integer,Item> items){

		Map <Integer, List<Item>> newResponsibleNodes = new HashMap <Integer, List<Item>>();
		
		ArrayList<Integer> keyItems = new ArrayList<Integer>(items.keySet());

		for (Integer keyItem:keyItems){
			List <Integer> nodesCompetent = getResponsibleNodes(nodes, keyItem);

			int responsibleNode = nodesCompetent.get(nodesCompetent.size()-1);
			List <Item> listOfResponsibleNode = newResponsibleNodes.get(responsibleNode);
			if (listOfResponsibleNode == null){
				List<Item> itemList = new ArrayList<Item>();
				itemList.add(items.get(keyItem));
				newResponsibleNodes.put(responsibleNode, itemList);
			} else {
				listOfResponsibleNode.add(items.get(keyItem));
			}
		}
		return newResponsibleNodes;
	}

	// removes items for which the node is not responsible anymore (e.g. upon recovery of the node)
	private static void notResposibleItemRemove(Map<Integer,ActorRef> nodes, Map<Integer,Item> items){

		boolean doRewrite = false;
		ArrayList<Integer> keyItems = new ArrayList<Integer>(items.keySet());

		for (Integer keyItem:keyItems){
			
			List <Integer> nodesCompetent = getResponsibleNodes(nodes, keyItem);
			
			//if among the nodeCompetent there is not the current node
			//then delete that item from its list of items
			if (!nodesCompetent.contains(myId)){
				items.remove(keyItem);
				doRewrite = true;
			}
		}

		if (doRewrite) {
			Utilities.updateLocalStorage(myId,items);
		}
	}

	
    public static class Node extends UntypedActor {
		
		// The table of all nodes in the system id->ref
		private Map<Integer, ActorRef> nodes = new HashMap<Integer,ActorRef>();
	    private Map<Integer, Item> items = new HashMap<Integer,Item>();
	    
	    // only one read at a time is meant to be handled
	    private PendingRead pendingReadRequest = null;
	    private PendingWrite pendingWriteRequest = null;

	    //item sent from the client
	    private Item itemToWrite = null;
	    private List<Integer> responsibleNodesForWrite = null;
	    
	    private char typeOfRequest;

		public void preStart() {
			if (remotePath != null) {
    			getContext().actorSelection(remotePath).tell(new Message.RequestNodelist('j'), getSelf());
			}
			nodes.put(myId, getSelf());
			Utilities.loadItems(myId,items);

		}

		
		private void initializeItemList(Map<Integer, Item> items){
			Utilities.initializeStorageFile(myId,items);
			this.items = items;
		}
		
        void setReadTimeout(Integer itemKey, int time) {
    		getContext().system().scheduler().scheduleOnce(
    				Duration.create(time, TimeUnit.SECONDS),	
    				getSelf(),
    				new Message.ReadTimeout(itemKey),
    				getContext().system().dispatcher(), getSelf()
    				);
    	}

	    void setWriteTimeout(Integer itemKey, int time) {
		    getContext().system().scheduler().scheduleOnce(
				    Duration.create(time, TimeUnit.SECONDS),
				    getSelf(),
				    new Message.WriteTimeout(itemKey),
				    getContext().system().dispatcher(), getSelf()
		    );
	    }
		
        public void onReceive(Object message) {
			if (crashed && !(message instanceof Message.Nodelist) &&
					!(message instanceof Message.RequestRecovery) && !(message instanceof ReceiveTimeout)){
				System.out.println("Dropped msg");
				Utilities.goBackToTerminal();
				return;
			}
			if (message instanceof Message.RequestNodelist) {
				typeOfRequest = ((Message.RequestNodelist) message).typeOfRequest;
				if(typeOfRequest == 'j'){
					System.out.println("[JOIN] Received request nodes list from node "+ getSender().toString() + "\n");
				}
				else if (typeOfRequest == 'r'){
					System.out.println("[RECOVERY] Received request nodes list from node "+ getSender().toString() + "\n");
				}
				Utilities.goBackToTerminal();
				getSender().tell(new Message.Nodelist(nodes,typeOfRequest), getSelf());
			}
			else if (message instanceof Message.Nodelist) {
				Message.Nodelist msg = (Message.Nodelist) message;
				typeOfRequest = ((Message.Nodelist) message).getTypeOfRequest();
				getContext().setReceiveTimeout(Duration.Undefined());
				if(typeOfRequest == 'j') {
					nodes.putAll(msg.getNodeList());
					
					ActorRef nextNode = identifyNextNode(nodes);
					nextNode.tell(new Message.RequestItems(), getSelf());

				}
				else if (typeOfRequest == 'r'){
					nodes = new HashMap<Integer, ActorRef>();
					items = new HashMap<Integer, Item>();

					nodes.putAll(msg.getNodeList());
					Utilities.loadItems(myId, items);

					notResposibleItemRemove(nodes,items);
					crashed = false;

				}
			}
			else if (message instanceof Message.Join) {
				int id = ((Message.Join)message).id;
				System.out.println("Node " + id + " joined" + "\n");
				Utilities.goBackToTerminal();
				nodes.put(id, getSender());
				notResposibleItemRemove(nodes,items);

			}
			else if (message instanceof Message.RequestJoin){
				getContext().actorSelection(remotePath).tell(new Message.RequestNodelist('j'), getSelf());
				getContext().setReceiveTimeout(Duration.create(T+"second"));
			}
			else if (message instanceof Message.RequestItems){ // I have to send my items to the sender of the message
				getSender().tell(new Message.ItemsList(items), getSelf());

			}

			else if (message instanceof Message.ItemsList){ // received items i'm responsible for. initialize items and announce my presence
				initializeItemList(((Message.ItemsList)message).getItemsList());
				notResposibleItemRemove(nodes, items);
				
				//announce to other nodes my presence
				for (ActorRef n : nodes.values()) {
					if(!n.equals(getSelf())){
						n.tell(new Message.Join(myId), getSelf());
					}
				}

				
			}
			else if (message instanceof Message.Leave){ // a client just told me to leave the network
				System.out.println("Received leave msg");
				for (ActorRef n : nodes.values()) {
					if(!n.equals(getSelf())){
						n.tell(new Message.LeavingAnnouncement(myId), getSelf());
					}
				}

				if (nodes.size() == 1){
					System.out.println(">> Forbidden: Only myself in the network" + "\n");
					getSender().tell(new Message.AckLeave(myId, false), null);
				}
				else {
					nodes.remove(myId);
					Map<Integer, List<Item>> newResponsibleNodes;
					newResponsibleNodes = getNewResponsibleNodes(nodes, items);

					for (int i : newResponsibleNodes.keySet()) {
						ActorRef toBeNotified = nodes.get(i);
						toBeNotified.tell(new Message.UpdateAfterLeaving(newResponsibleNodes.get(i)), getSelf());
					}

					// re-initializing the list of nodes after leaving the network, for a possible future join
					nodes.clear();
					nodes.put(myId, getSelf());

					System.out.println(">> Node has been removed from the network" + "\n");


					//send ack to the client
					getSender().tell((new Message.AckLeave(myId,true)), null);
				}
				Utilities.goBackToTerminal();
			}
			else if (message instanceof Message.LeavingAnnouncement){ // a node just told me that it is about to leave
				nodes.remove(((Message.LeavingAnnouncement)message).getId());
				System.out.println("Node " + ((Message.LeavingAnnouncement)message).getId() + " left\n");
				Utilities.goBackToTerminal();

			}
			else if (message instanceof Message.UpdateAfterLeaving){ 					//a node just sent me the list of items it
				List<Item> newItems = ((Message.UpdateAfterLeaving)message).getItemsList();	//had, which now I am responsible for
				for (Item item : newItems){
					Utilities.appendItemToStorageFile(myId,item);
					items.put(item.getKey(), item);
				}
			}
			
			
			else if (message instanceof Message.ClientToCoordReadRequest){ 	// client is requesting a read
				Integer itemKey = ((Message.ClientToCoordReadRequest) message).itemKey;
				pendingReadRequest = new PendingRead(itemKey, getSender());
				setReadTimeout(itemKey, T);
				List <Integer> responsibleNodes = getResponsibleNodes(nodes, itemKey);
				for (int i : responsibleNodes){
					ActorRef a = nodes.get(i);
					a.tell(new Message.CoordToNodeReadRequest(itemKey), getSelf());
				}
			}			
			else if (message instanceof Message.CoordToNodeReadRequest){ 	// coordinator is requesting a read
				Integer itemKey = ((Message.CoordToNodeReadRequest) message).itemKey;
				Item item = items.get(itemKey);
				getSender().tell(new Message.ReadReplyToCoord(item), getSelf());
				
				if (item != null){
					System.out.println("Read sent to Coordinator with value: " + item.toString() + "\n");
					Utilities.goBackToTerminal();
				} else {
					System.out.println("Item to be read " + itemKey + " does not exist\n");
					Utilities.goBackToTerminal();
				}
			}
			else if (message instanceof Message.ReadReplyToCoord){ 	// node is replying to one of my read requests
				Item itemRead = ((Message.ReadReplyToCoord) message).item;
				if(pendingReadRequest != null){ // if null -> i have already finished servicing the request and i can ignore further replies
					
					pendingReadRequest.setLatestItem(itemRead); //and increment counter
					if (pendingReadRequest.getCounter() == R){

						if (pendingReadRequest.getItem() != null){
							pendingReadRequest.getClient().tell(new Message.ReadReplyToClient(pendingReadRequest.getItem()), getSelf());
						} else { // this means that the item does not exist in the system
							pendingReadRequest.getClient().tell(new Message.ReadReplyToClient(pendingReadRequest.getItemKey(), false), getSelf());
						}


						if (pendingReadRequest.getItem() != null){
							System.out.println("Read serviced to Client with value: " + itemRead.toString() + "\n");
							Utilities.goBackToTerminal();
						} else {
							System.out.println("Read unsuccessful, item: " + pendingReadRequest.getItemKey() + " does not exist" + "\n");
							Utilities.goBackToTerminal();
						}
						
						pendingReadRequest = null;
					}
					
				}
			}
			else if (message instanceof Message.ReadTimeout){ 	// timeout for read has been hit
				if (pendingReadRequest != null){
					pendingReadRequest.getClient().tell(new Message.ReadReplyToClient (pendingReadRequest.getItemKey(), true), getSelf());
					pendingReadRequest = null;
					
					System.out.println("Read unsuccessful, Timeout has been hit" + "\n");
					Utilities.goBackToTerminal();

				}
			}
			else if (message instanceof Message.ClientToCoordWriteRequest){ //client sent me a write request
				Message.ClientToCoordWriteRequest msgReqWriteCord = (Message.ClientToCoordWriteRequest)message;

				if (nodes.size() < N){
					getSender().tell(new Message.rejectWrite(N), getSelf());
					System.out.println("Write operation rejected: less than "+N+"\n");
				}
				else {
					Integer itemKey = msgReqWriteCord.itemKey;

					itemToWrite = new Item(itemKey, msgReqWriteCord.value, 0); //no problem with version 0

					System.out.println("Received write request : ITEM -> key " + msgReqWriteCord.itemKey + " value " +
							msgReqWriteCord.value);

					pendingWriteRequest = new PendingWrite(msgReqWriteCord.itemKey, msgReqWriteCord.value, getSender());
					setWriteTimeout(itemKey, T);
					responsibleNodesForWrite = getResponsibleNodes(nodes, itemKey);
					for (int i : responsibleNodesForWrite) {
						ActorRef a = nodes.get(i);
						a.tell(new Message.CoordToNodeWriteRequest(itemKey), getSelf());
					}
				}
				Utilities.goBackToTerminal();
			}
			else if (message instanceof  Message.CoordToNodeWriteRequest){
				Message.CoordToNodeWriteRequest msg = (Message.CoordToNodeWriteRequest)message;
				Integer itemKey = msg.itemKey;
				Item item = items.get(itemKey);

				getSender().tell(new Message.WriteReplyToCoord(item), getSelf());

				if (item == null){
					System.out.println("Items is not present: creation......");
					Utilities.goBackToTerminal();
				} else {
					System.out.println("Item " + itemKey + ": updating..... ");
					Utilities.goBackToTerminal();
				}
			}
			else if (message instanceof  Message.WriteReplyToCoord){
				Message.WriteReplyToCoord msg = (Message.WriteReplyToCoord)message;
				Item item = msg.item;
				Item latestItem;

				if (pendingWriteRequest != null){
					pendingWriteRequest.setLatestItem(item);
					if (pendingWriteRequest.getCounter() == Math.max(R,W)){
						
						latestItem = pendingWriteRequest.getItem();
						if (latestItem != null){
							itemToWrite.setVersion(latestItem.getVersion()+1);
							pendingWriteRequest.getClient().tell(new Message.WriteReplyToClient(itemToWrite,true,false), getSelf());



							System.out.println("Item : " + itemToWrite.toString() + " updated" + "\n");
							for (int i : responsibleNodesForWrite){
								ActorRef a = nodes.get(i);
								a.tell(new Message.CoordToNodeDoWrite(itemToWrite,true), getSelf());
							}

							Utilities.goBackToTerminal();
							
						}else{
							pendingWriteRequest.getClient().tell(new Message.WriteReplyToClient(itemToWrite, false, false),
									getSelf());
							System.out.println("Item : " + itemToWrite.toString() + " created" + "\n");

							for (int i : responsibleNodesForWrite){
								ActorRef a = nodes.get(i);
								a.tell(new Message.CoordToNodeDoWrite(itemToWrite,false), getSelf());
							}

							Utilities.goBackToTerminal();

						}

						//For the next request
						pendingWriteRequest = null;
					}
									
				}
			}
			else if (message instanceof  Message.CoordToNodeDoWrite){
				Message.CoordToNodeDoWrite msg = (Message.CoordToNodeDoWrite)message;
				Item receivedItem = msg.item;

				if (msg.isExisting){
					items.remove(receivedItem.getKey());
					System.out.println("Updating completed -> Item "+receivedItem.toString()+ "\n");
				}
				else{
					System.out.println("Creation completed -> Item "+receivedItem.toString() + "\n");
				}

				items.put(receivedItem.getKey(),receivedItem);

				Utilities.updateLocalStorage(myId,items);

				Utilities.goBackToTerminal();
			}
			else if (message instanceof Message.RequestRecovery){
				getContext().actorSelection(remotePath).tell(new Message.RequestNodelist('r'), getSelf());
				getContext().setReceiveTimeout(Duration.create(T+"second"));
			}
			else if (message instanceof Message.WriteTimeout){ 	// timeout for read has been hit
				if (pendingWriteRequest != null){
					//set null we are not interested in knowing if node is present or not
					pendingWriteRequest.getClient().tell(new Message.WriteReplyToClient (itemToWrite , true, true),
							getSelf());
					pendingWriteRequest = null;

					System.out.println("Write unsuccessful, Timeout has been hit" + "\n");
					Utilities.goBackToTerminal();

				}
			}
			else if (message instanceof ReceiveTimeout){
				getContext().setReceiveTimeout(Duration.Undefined());
				System.out.println("ERROR: Failed to contact node "+remotePath + "\n");
				Utilities.goBackToTerminal();

			}
			else
            	unhandled(message);		// this actor does not handle any incoming messages
        }

    }
    


	public static void main(String[] args) {
		
		if (args.length != 0 && args.length !=2 ) {
			System.out.println("Wrong number of arguments: [remote_ip remote_port]");
			return;
		}
		
		// Load parameters from parameters configuration file
	    File parameterFile = new File("./../../resources/parameters.conf");
		Config parameters = ConfigFactory.parseFile(parameterFile);
	    try {
		    N = parameters.getInt("N.value");
		    R = parameters.getInt("R.value");
		    W = parameters.getInt("W.value");
		    T = parameters.getInt("T.value");
	    }catch(Exception ex){
		    System.exit(0);
	    }	    
	    if(R + W <= N){
	    	System.out.println("Error: the condition 'R + W > N' must hold");
		    System.exit(0);
	    }
		
		// Load the "application.conf"
		Config config = ConfigFactory.load("application");
		myId = config.getInt("nodeapp.id");
		if (args.length == 2) {
			// Starting with a bootstrapping node
			String ip = args[0];
			String port = args[1];
    		// The Akka path to the bootstrapping peer
			remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node"+myId;
			System.out.println("Starting node " + myId + "; bootstrapping node: " + ip + ":"+ port);
		}
		else 
			System.out.println("Starting disconnected node " + myId + "\n");
		
		// Create the actor system
		final ActorSystem system = ActorSystem.create("mysystem", config);

		// Create a single node actor
		receiver = system.actorOf(
				Props.create(Node.class),	// actor class 
				"node"					// actor name
				);

	    terminal();

	    System.exit(0);
    }
}
