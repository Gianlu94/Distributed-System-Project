//package main.java;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gianluke on 14/03/17.
 */
public class Message {

	//end this msg in order to require to our remoteActor to do start a Join
	public static class RequestJoin implements Serializable {}

	public static class Join implements Serializable {
		int id;
		public Join(int id) {
			this.id = id;
		}
	}
	//send this msg in order to ask for the list of items one actor is responsible for
	public static class RequestItems implements Serializable {}

	//msg containing the list of items that sender requested
	public static class ItemsList implements Serializable{
		private Map<Integer, Item> items;

		public ItemsList(Map<Integer,Item> items){
			this.items = items;
		}

		public Map<Integer, Item>getItemsList(){
			return items;
		}
	}

	//reply to RequestNodeList from the sender
	public static class Nodelist implements Serializable {
		private Map<Integer, ActorRef> nodes;
		private char typeOfRequest;
		public Nodelist(Map<Integer, ActorRef> nodes, char typeOfRequest) {
			this.nodes = Collections.unmodifiableMap(new HashMap<Integer, ActorRef>(nodes));
			this.typeOfRequest = typeOfRequest;
		}

		public Map<Integer, ActorRef> getNodeList(){
			return this.nodes;
		}

		public char getTypeOfRequest(){
			return this.typeOfRequest;
		}
	}

	public static class RequestNodelist implements Serializable {
		char typeOfRequest;

		public RequestNodelist (char typeOfRequest){
			this.typeOfRequest = typeOfRequest;
		}
	}

	//message sent by the client to node
	public static class LeaveMessage implements Serializable {}

	public static class MessageRead implements Serializable{
		Integer itemKey = null;
		Item item = null;


		public MessageRead(Integer itemKey){
			super();
			this.itemKey = itemKey;
		}

		public MessageRead(Item item){
			super();
			this.item = item;
		}
	}
	//message sent by the client to node
	public static class ClientToCoordReadRequest extends MessageRead {
		
		public ClientToCoordReadRequest(Integer itemKey) {
			super(itemKey);
		}
	}
	
	// message sent from coordinator to responsible nodes
	public static class CoordToNodeReadRequest extends MessageRead{
		
		public CoordToNodeReadRequest(Integer itemKey) {
			super(itemKey);
		}
	}
	
	public static class ReadReplyToCoord extends MessageRead{
		
		public ReadReplyToCoord(Item item) {
			super(item);
		}
	}

	public static class ReadReplyToClient extends MessageRead{
		Boolean isExisting = false;
		
		public ReadReplyToClient(Item item) {
			super(item);
			this.isExisting = true;
		}
		public ReadReplyToClient(Integer itemKey, Boolean isExisting) {
			super(itemKey);
			this.isExisting = isExisting;
		}
	}
	
	//send this msg in order to tell the other nodes that the node is leaving
	public static class LeavingAnnouncement implements Serializable {
		private int id;

		public LeavingAnnouncement(int id) {
			this.id = id;
		}
		public int getId() {
			return id;
		}
	}

	//msg sent in order to communicate the list of the items to the new responsible node
	public static class UpdateAfterLeaving implements Serializable{
		private List<Item> itemList;

		public UpdateAfterLeaving(List<Item> itemList){
			this.itemList = itemList;
		}

		public List<Item> getItemsList(){
			return this.itemList;
		}
	}
    public static class ReadTimeout implements Serializable {
    	public ReadTimeout(Integer itemKey) {
			super();
			this.itemKey = itemKey;
		}

		Integer itemKey;
    }
}
