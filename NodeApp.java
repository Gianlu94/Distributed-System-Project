import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;


import akka.actor.*;
import scala.collection.generic.BitOperations;
import scala.collection.mutable.StringBuilder;
import scala.concurrent.duration.Duration;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

public class NodeApp {
	static private String remotePath = null; // Akka path of the bootstrapping peer
	static private int myId; // ID of the local node
	static int N, R, W, T; // parameters replication number, read quorum, write quorum and timeout
	static private ActorRef receiver;

	public static class Nodelist implements Serializable {
		Map<Integer, ActorRef> nodes;
		char typeOfRequest;
		public Nodelist(Map<Integer, ActorRef> nodes, char typeOfRequest) {
			this.nodes = Collections.unmodifiableMap(new HashMap<Integer, ActorRef>(nodes));
			this.typeOfRequest = typeOfRequest;
		}
	}

    public static class RequestNodelist implements Serializable {
	    char typeOfRequest;

	    public RequestNodelist (char typeOfRequest){
		    this.typeOfRequest = typeOfRequest;
	    }
    }

	//send this msg in order to require to our remoteActor to do start a Join
	public static class RequestJoin implements Serializable {}

	//send this msg in order to ask for the list of items one actor is resonsible for
	public static class RequestItems implements Serializable {}

	/*
		This is the class that identify an item
	 */
	public static class Item implements Serializable {
		private Integer key;
		private String value;
		private Integer version;

		public Item(Integer key, String value, Integer version ){
			this.key = key;
			this.value = value;
			this.version = version;
		}
		
		public String toString(){
			String res = key + " " + value + " " + version;
			return res;
		}
	}


	public static class ItemsList implements Serializable{
		Map<Integer, Item> items;

		public ItemsList(Map<Integer,Item> items){
			this.items = items;
		}
	}


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
		Integer itemIdentifier = 0; //Identifier (key) in the hashmap

		Integer keyItem;

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

					//put the item in items hashmap
					items.put(itemIdentifier,new Item(Integer.parseInt(tokensItem[0]),
														tokensItem[1],
														Integer.parseInt(tokensItem[2])));
					//increment local id (hashMap)
					itemIdentifier++;

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

	public static void doJoin (String ip, String port){

		remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
		receiver.tell(new RequestJoin()  ,null);


	}

	//naive method maybe we can think something better --works on the ring
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
		
		/*
		int marginG = 10000; //TODO:Replace it
		int marginL = 0;
		ActorRef nodeToContactG = null ;
		ActorRef nodeToContactL = null ;
		for (int key:nodes.keySet()){
			if(key > myId){
				if (key - myId < marginG){
					marginG = key - myId;
					nodeToContactG = nodes.get(key);
				}
			}
			//if successor has a smaller Id
			else{
				if (myId - key > marginL) {
					marginL = myId - key;
					nodeToContactL = nodes.get(key);
				}
			}
		}
		if ((marginG > 0)&&(marginL==0)){
			return nodeToContactG; //Todo:if i have only one node and node performs request to itsselfe
									//Todo: (NO SENSE) it should return null (MANAGE)
		}
		else{
			return nodeToContactL;
		}*/
	}


	public static class Join implements Serializable {
		int id;
		public Join(int id) {
			this.id = id;
		}
	}
	
    public static class Node extends UntypedActor {
		
		// The table of all nodes in the system id->ref
		private Map<Integer, ActorRef> nodes = new HashMap<>();
	    private Map<Integer, Item> items = new HashMap<>();
	    private char typeOfRequest;

		public void preStart() {
			if (remotePath != null) {
    			getContext().actorSelection(remotePath).tell(new RequestNodelist('j'), getSelf());
			}
			nodes.put(myId, getSelf());
			loadItems(items);

		}

		// initialize storage file and add items
		private void initializeStorageFile (Map<Integer, Item> items){
			String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file			
			
			//String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file
// ** il file local storage deve essere chiamato "myid"mylocalStorage, oppure deve andare nella cartella "myid" e chiamarsi myLocalStorage? **
			
			List<String> lines = new ArrayList<String>();
			for (Integer i : items.keySet()){
				lines.add(items.get(i).toString());
			}
			Path file = Paths.get(storagePath);
			
			try{
			Files.write(file, lines, Charset.forName("UTF-8"));
			}catch (IOException e){
				e.printStackTrace();
			}
		}
		
		private void initializeItemList(Map<Integer, Item> items){
			initializeStorageFile(items);
			this.items = items;
		}
		
        public void onReceive(Object message) {
			if (message instanceof RequestNodelist) {
				typeOfRequest = ((RequestNodelist) message).typeOfRequest;
				if(typeOfRequest == 'j'){
					getSender().tell(new Nodelist(nodes,typeOfRequest), getSelf());
				}
			}
			else if (message instanceof Nodelist) {
				typeOfRequest = ((Nodelist) message).typeOfRequest;
				if(typeOfRequest == 'j') {
					getContext().setReceiveTimeout(Duration.Undefined());
					nodes.putAll(((Nodelist) message).nodes);
					
					ActorRef nextNode = identifyNextNode(nodes);
					nextNode.tell(new RequestItems(), getSelf());


				}
			}
			else if (message instanceof Join) {
				int id = ((Join)message).id;
				System.out.println("Node " + id + " joined");
				nodes.put(id, getSender());
			}
			else if (message instanceof RequestJoin){
				getContext().actorSelection(remotePath).tell(new RequestNodelist('j'), getSelf());
				getContext().setReceiveTimeout(Duration.create(T+"second"));
			}
			else if (message instanceof RequestItems){ // I have to send my items to the sender of the message
				getSender().tell(new ItemsList(items), getSelf());
			}

			else if (message instanceof ItemsList){ // received items i'm responsible for. initialize items and announce my presence
				initializeItemList(((ItemsList)message).items);
				
				//announce to other nodes my presence
				for (ActorRef n : nodes.values()) {
						n.tell(new Join(myId), getSelf());
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
		Config parameters = ConfigFactory.load("parameters");
		N = parameters.getInt("N.value");
		R = parameters.getInt("R.value");
		W = parameters.getInt("W.value");
		T = parameters.getInt("T.value");

	    terminal();

	    System.exit(0);
    }
}
