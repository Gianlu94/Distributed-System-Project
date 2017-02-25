import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;





import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Cancellable;
import scala.collection.generic.BitOperations;
import scala.collection.mutable.StringBuilder;
import scala.concurrent.duration.Duration;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

public class NodeApp {
	static private String remotePath = null; // Akka path of the bootstrapping peer
	static private int myId; // ID of the local node
	static int N, R, W, T; // parameters replication number, read quorum, write quorum and timeout

    public static class Join implements Serializable {
		int id;
		public Join(int id) {
			this.id = id;
		}
	}
    public static class RequestNodelist implements Serializable {}

    public static class Nodelist implements Serializable {
		Map<Integer, ActorRef> nodes;
		public Nodelist(Map<Integer, ActorRef> nodes) {
			this.nodes = Collections.unmodifiableMap(new HashMap<Integer, ActorRef>(nodes)); 
		}
	}

	public static class Item implements Serializable {
		private Integer key;
		private String value;
		private Integer version;

		public Item(Integer key, String value, Integer version ){
			this.key = key;
			this.value = value;
			this.version = version;
		}
	}

	public static class ItemsList implements Serializable{
		Map<Integer, Item> items;

		public ItemsList(Map<Integer,Item> items){
			this.items = items;
		}
	}

	/*
		This method is responsible to load local storage of the node
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
					tokensItem = item.split("\\s+");
					System.out.println(tokensItem.length);

					keyItem = Integer.parseInt(tokensItem[0]);
					items.put(itemIdentifier,new Item(keyItem, tokensItem[1],
														Integer.parseInt(tokensItem[2])));
					itemIdentifier++;
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
	
    public static class Node extends UntypedActor {
		
		// The table of all nodes in the system id->ref
		private Map<Integer, ActorRef> nodes = new HashMap<>();
	    private Map<Integer, Item> items = new HashMap<>();

		public void preStart() {
			if (remotePath != null) {
    			getContext().actorSelection(remotePath).tell(new RequestNodelist(), getSelf());
			}
			nodes.put(myId, getSelf());
			loadItems(items);

			for (int i=0; i< items.size(); i++){

			}

		}

        public void onReceive(Object message) {
			if (message instanceof RequestNodelist) {
				getSender().tell(new Nodelist(nodes), getSelf());
			}
			else if (message instanceof Nodelist) {
				nodes.putAll(((Nodelist)message).nodes);
				for (ActorRef n: nodes.values()) {
					n.tell(new Join(myId), getSelf());
				}
			}
			else if (message instanceof Join) {
				int id = ((Join)message).id;
				System.out.println("Node " + id + " joined");
				nodes.put(id, getSender());
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
		final ActorRef receiver = system.actorOf(
				Props.create(Node.class),	// actor class 
				"node"						// actor name
				);

		// Load parameters from parameters configuration file
		Config parameters = ConfigFactory.load("parameters");
		N = parameters.getInt("N.value");
		R = parameters.getInt("R.value");
		W = parameters.getInt("W.value");
		T = parameters.getInt("T.value");
    }
}
