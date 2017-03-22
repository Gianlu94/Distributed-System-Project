import akka.actor.ActorRef;

/**
 * Created by gianluke on 22/03/17.
 */
public class PendingOperation {
	private Integer counter;
	private ActorRef client;



	public PendingOperation(Integer counter, ActorRef client){
		this.counter = counter;
		this.client = client;
	}

	public ActorRef getClient() {
		return client;
	}

	public Integer getCounter() {
		return counter;
	}

	public void incrementCounter() {
		counter++;
	}
}
