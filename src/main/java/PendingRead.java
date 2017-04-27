import akka.actor.ActorRef;

/**
 * Created by gianluke on 22/03/17.
 * Class extends PendingOperation. It is related to read.
 */

public class PendingRead extends PendingOperation {
	private Integer itemKey;
	private Item item;

	public PendingRead(Integer itemKey, ActorRef client) {
		super(0,client);
		this.itemKey = itemKey;
		this.item = null;
	}

	public void setLatestItem(Item item) {
		if (item!=null){
			if (this.item == null){
				this.item = item;
			} else {
				if( this.item.getVersion() < item.getVersion()){
					this.item = item;
				}
			}
		}
		this.incrementCounter();
	}


	public Integer getItemKey() {
		return itemKey;
	}

	public Item getItem() {
		return item;
	}
}
