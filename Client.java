import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
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

	
    public static class Node extends UntypedActor {
		public void preStart() {
		}

        public void onReceive(Object message) {
			
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
		
    }
}
