//package main.java;

import akka.actor.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;//Todo:Inspection
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NodeApp {
	static private String remotePath = null; // Akka path of the bootstrapping peer
	static private int myId; // ID of the local node
	static int N, R, W, T; // parameters replication number, read quorum, write quorum and timeout
	static private ActorRef receiver;



	private static void goBackToTerminal(){
		System.out.print(">> ");
	}


	/*
		  Terminal to receive node commands
	   */
	private static void terminal(){
		Scanner input; //to receive keyboard input stream
		String inputCommand;
		String [] tokensInput; //split command in tokens
		Integer tokensNumber;


		input = new Scanner(System.in);
		while (true) {
			System.out.print(">> ");
			inputCommand = input.nextLine();
			tokensInput = inputCommand.split(" ");

			if (tokensInput[0].toLowerCase().equals("e")){
				System.exit(0); // for us remember to remove it
			}
			if (tokensInput.length != 5){
				System.err.println("ERROR: Wrong number of parameters");
			}
			else{
				if ((tokensInput[0].toLowerCase().equals("java"))&&
				(tokensInput[1].toLowerCase().equals("node"))){

					switch (tokensInput[2].toLowerCase()){
						case "join":
							//System.out.println("ERROR: Not implemented yet");
							doJoin(tokensInput[3].toLowerCase(),tokensInput[4].toLowerCase());
							break;
						case "recover":
							System.out.println("ERROR: Not implemented yet");
							break;
						default:
							System.err.println("ERROR: unknown command");
							break;
					}

				}
			}
		}
	}

	/*
		This method is responsible to load local storage of the node
		TODO: replace e.printStackTrace with something else (e.g. Log.e)
	 */
	private static void loadItems(Map<Integer,Item> items){
		String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file
		File localStorage = new File(storagePath);
		FileReader reader = null;
		BufferedReader buffer;
		String item;
		String tokensItem[]; //when reading an item from the file you get a string
		Integer itemKey;

		if (!localStorage.exists()){ //check if file exists
			try {
				localStorage.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			//start reading operation
			try {
				reader = new FileReader(storagePath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			buffer = new BufferedReader(reader);
			try {
				item = buffer.readLine();
				while (item!= null){
					tokensItem = item.split("\\s+"); //split string according space

					itemKey = Integer.parseInt(tokensItem[0]);
					//put the item in items hashmap
					items.put(itemKey,new Item(itemKey,
														tokensItem[1],
														Integer.parseInt(tokensItem[2])));

					//read next item in the file
					item = buffer.readLine();


				}

				/*for (int i=0; i < items.size(); i++){
					System.out.println("KEY = "+items.get(i).key+" VALUE = "+items.get(i).value+
							" VERSION = " +items.get(i).version);
				}
				*/

			} catch (IOException e) {
				e.printStackTrace();
			}


		}

	}

	// initialize storage file and add items
	private static void initializeStorageFile (Map<Integer, Item> items){

		String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file
		Path file = Paths.get(storagePath);

		List<String> lines = new ArrayList<String>();
		for (Integer i : items.keySet()){
			lines.add(items.get(i).toString());
		}		

		try{
			Files.write(file, lines,  Charset.forName("UTF-8"));
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private static void appendItemToStorageFile(Item item){
	
		String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file
		Path file = Paths.get(storagePath);

		List<String> lines = new ArrayList<String>();
		lines.add(item.toString());		

		try{
			Files.write(file, lines,  Charset.forName("UTF-8"), StandardOpenOption.APPEND);
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private static void updateLocalStorage(Map<Integer,Item> items){
		initializeStorageFile(items);
	}

	public static void doJoin (String ip, String port){

		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		receiver.tell(new Message.RequestJoin()  ,null);


	}

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


	//TODO: insert a method to return an arraylist of keys to avoid duplicate code
	//TODO: test it
	private static void itemsAfterJoin (Map<Integer,ActorRef> nodes, Map<Integer,Item> items){

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
		//TODO: how to update local storage
		if (doRewrite) {
			updateLocalStorage(items);
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
			loadItems(items);

		}

		
		private void initializeItemList(Map<Integer, Item> items){
			initializeStorageFile(items);
			this.items = items;
		}
		
        void setReadTimeout(int time, Integer itemKey) {
    		getContext().system().scheduler().scheduleOnce(
    				Duration.create(time, TimeUnit.SECONDS),	
    				getSelf(),
    				new Message.ReadTimeout(itemKey),
    				getContext().system().dispatcher(), getSelf()
    				);
    	}

	    void setWriteTimeout(int time, Integer itemKey) {
		    getContext().system().scheduler().scheduleOnce(
				    Duration.create(time, TimeUnit.SECONDS),
				    getSelf(),
				    new Message.WriteTimeout(itemKey),
				    getContext().system().dispatcher(), getSelf()
		    );
	    }
		
        public void onReceive(Object message) {
			if (message instanceof Message.RequestNodelist) {
				typeOfRequest = ((Message.RequestNodelist) message).typeOfRequest;
				if(typeOfRequest == 'j'){
					getSender().tell(new Message.Nodelist(nodes,typeOfRequest), getSelf());
				}
			}
			else if (message instanceof Message.Nodelist) {
				typeOfRequest = ((Message.Nodelist) message).getTypeOfRequest();
				if(typeOfRequest == 'j') {
					getContext().setReceiveTimeout(Duration.Undefined());
					nodes.putAll(((Message.Nodelist) message).getNodeList());
					
					ActorRef nextNode = identifyNextNode(nodes);
					nextNode.tell(new Message.RequestItems(), getSelf());

				}
			}
			else if (message instanceof Message.Join) {
				int id = ((Message.Join)message).id;
				System.out.println("Node " + id + " joined");
				goBackToTerminal();
				nodes.put(id, getSender());
				itemsAfterJoin(nodes,items);

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
				itemsAfterJoin(nodes, items);
				
				//announce to other nodes my presence
				for (ActorRef n : nodes.values()) {
					if(!n.equals(getSelf())){
						n.tell(new Message.Join(myId), getSelf());
					}
				}

				
			}
			else if (message instanceof Message.LeaveMessage){ // a client just told me to leave the network
				System.out.println("Received leave msg");
				for (ActorRef n : nodes.values()) {
					if(!n.equals(getSelf())){
						n.tell(new Message.LeavingAnnouncement(myId), getSelf());
					}
				}
				
				nodes.remove(myId);
				Map <Integer, List<Item>> newResponsibleNodes;
				newResponsibleNodes = getNewResponsibleNodes(nodes, items);
				
				for (int i : newResponsibleNodes.keySet()){
					ActorRef toBeNotified = nodes.get(i);
					toBeNotified.tell(new Message.UpdateAfterLeaving(newResponsibleNodes.get(i)), getSelf());
				}
				
				// re-initializing the list of nodes after leaving the network, for a possible future join
				nodes.clear();
				nodes.put(myId, getSelf());
				
				System.out.println("Node has been removed from the network");
				goBackToTerminal();
			}
			else if (message instanceof Message.LeavingAnnouncement){ // a node just told me that it is about to leave
				nodes.remove(((Message.LeavingAnnouncement)message).getId());
				System.out.println("Node " + ((Message.LeavingAnnouncement)message).getId() + " left");
				goBackToTerminal();

			}
			else if (message instanceof Message.UpdateAfterLeaving){ 					//a node just sent me the list of items it
				List<Item> newItems = ((Message.UpdateAfterLeaving)message).getItemsList();	//had, which now I am responsible for
				for (Item item : newItems){
					appendItemToStorageFile(item);
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
					System.out.println("Read sent to Coordinator with value: " + item.toString());
					goBackToTerminal();
				} else {
					System.out.println("Item to be read " + itemKey + " does not exist");
					goBackToTerminal();
				}
			}
			else if (message instanceof Message.ReadReplyToCoord){ 	// node is replying to one of my read requests
				Item itemRead = ((Message.ReadReplyToCoord) message).item;
				if(pendingReadRequest != null){ // if null -> i have already finished servicing the request and i can ignore further replies
					if (itemRead != null){
						pendingReadRequest.setLatestItem(itemRead); //and increment counter
						if (pendingReadRequest.getCounter() == R){
							pendingReadRequest.getClient().tell(new Message.ReadReplyToClient(pendingReadRequest.getItem()), getSelf());
							pendingReadRequest = null;
							
							System.out.println("Read serviced to Client with value: " + itemRead.toString());
							goBackToTerminal();

						}
					} else{  // this means that the item does not exist in the system
						if (pendingReadRequest.getCounter() == 0){
							pendingReadRequest.getClient().tell(new Message.ReadReplyToClient(pendingReadRequest.getItemKey(), false), getSelf());
						}
			
						System.out.println("Read unsuccessful, item: " + pendingReadRequest.getItemKey() + " does not exist");
						goBackToTerminal();

						pendingReadRequest = null;						
					}
				}
			}
			else if (message instanceof Message.ReadTimeout){ 	// timeout for read has been hit
				if (pendingReadRequest != null){
					pendingReadRequest.getClient().tell(new Message.ReadReplyToClient (pendingReadRequest.getItemKey(), true), getSelf());
					pendingReadRequest = null;
					
					System.out.println("Read unsuccessful, Timeout has been hit");
					goBackToTerminal();

				}
			}
			else if (message instanceof Message.ClientToCoordWriteRequest){ //client sent me a write request
				Message.ClientToCoordWriteRequest msgReqWriteCord = (Message.ClientToCoordWriteRequest)message;
				Integer itemKey = msgReqWriteCord.itemKey;

				itemToWrite = new Item(itemKey, msgReqWriteCord.value, 0); //no problem with version 0

				System.out.println("Received write request : ITEM -> key "+msgReqWriteCord.itemKey+" value " +
						msgReqWriteCord.value);

				pendingWriteRequest = new PendingWrite(msgReqWriteCord.itemKey, msgReqWriteCord.value, getSender());
				setWriteTimeout(itemKey, T);
				responsibleNodesForWrite = getResponsibleNodes(nodes, itemKey);
				for (int i : responsibleNodesForWrite){
					ActorRef a = nodes.get(i);
					a.tell(new Message.CoordToNodeWriteRequest(itemKey), getSelf());
				}
				goBackToTerminal();
			}
			else if (message instanceof  Message.CoordToNodeWriteRequest){
				Message.CoordToNodeWriteRequest msg = (Message.CoordToNodeWriteRequest)message;
				Integer itemKey = msg.itemKey;
				Item item = items.get(itemKey);

				getSender().tell(new Message.WriteReplyToCoord(item), getSelf());

				if (item == null){
					System.out.println("Items is not present: creation......");
					goBackToTerminal();
				} else {
					System.out.println("Item " + itemKey + ": updating..... ");
					goBackToTerminal();
				}
			}
			else if (message instanceof  Message.WriteReplyToCoord){
				Message.WriteReplyToCoord msg = (Message.WriteReplyToCoord)message;
				Item item = msg.item;
				Item latestItem;

				if (pendingWriteRequest != null){
					if (item != null){
						pendingWriteRequest.setLatestItem(item);
						if (pendingWriteRequest.getCounter() == Math.max(R,W)){
							latestItem = pendingWriteRequest.getItem();
							//itemToWrite.setValue(latestItem.getValue());
							itemToWrite.setVersion(latestItem.getVersion()+1);
							pendingWriteRequest.getClient().tell(new Message.WriteReplyToClient(itemToWrite,true,false), getSelf());



							System.out.println("Item : " + itemToWrite.toString() + " updated");
							for (int i : responsibleNodesForWrite){
								ActorRef a = nodes.get(i);
								a.tell(new Message.CoordToNodeDoWrite(itemToWrite,true), getSelf());
							}

							goBackToTerminal();

							//For the next request
							pendingWriteRequest = null;
						}
					}
					else{
						pendingWriteRequest.getClient().tell(new Message.WriteReplyToClient(itemToWrite, false, false),
								getSelf());
						System.out.println("Item : " + itemToWrite.toString() + " created");

						for (int i : responsibleNodesForWrite){
							ActorRef a = nodes.get(i);
							a.tell(new Message.CoordToNodeDoWrite(itemToWrite,false), getSelf());
						}

						goBackToTerminal();

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
					System.out.println("Updating completed -> Item "+receivedItem.toString());
				}
				else{
					System.out.println("Creation completed -> Item "+receivedItem.toString());
				}

				items.put(receivedItem.getKey(),receivedItem);

				updateLocalStorage(items);

				goBackToTerminal();
			}
			else if (message instanceof Message.WriteTimeout){ 	// timeout for read has been hit
				if (pendingWriteRequest != null){
					//set null we are not interested in knowing if node is present or not
					pendingWriteRequest.getClient().tell(new Message.WriteReplyToClient (itemToWrite, true, true),
							getSelf());
					pendingWriteRequest = null;

					System.out.println("Write unsuccessful, Timeout has been hit");
					goBackToTerminal();

				}
			}
			else if (message instanceof ReceiveTimeout){
				getContext().setReceiveTimeout(Duration.Undefined());
				System.out.println("\nERROR: Failed to contact node "+remotePath+"\n");
				goBackToTerminal();

			}
			else
            	unhandled(message);		// this actor does not handle any incoming messages
        }

    }
    


    //TO LAUNCH NODE APP FROM NODE CONFIGURATION FOLDER TYPE:
	//java -cp $AKKA_CLASSPATH:.:../../../ main.java.NodeApp
	public static void main(String[] args) {
		
		if (args.length != 0 && args.length !=2 ) {
			System.out.println("Wrong number of arguments: [remote_ip remote_port]");
			return;
		}
		
		// Load the "application.conf"
		Config config = ConfigFactory.load("application");
		myId = config.getInt("nodeapp.id");
		if (args.length == 2) {
			// Starting with a bootstrapping node
			String ip = args[0];
			String port = args[1];
    		// The Akka path to the bootstrapping peer
			remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
			System.out.println("Starting node " + myId + "; bootstrapping node: " + ip + ":"+ port);
		}
		else 
			System.out.println("Starting disconnected node " + myId);
		
		// Create the actor system
		final ActorSystem system = ActorSystem.create("mysystem", config);

		// Create a single node actor
		receiver = system.actorOf(
				Props.create(Node.class),	// actor class 
				"node"						// actor name
				);

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

	    terminal();

	    System.exit(0);
    }
}
