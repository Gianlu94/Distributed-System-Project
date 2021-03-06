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

	/*
		---- HERE ARE DEFINED THE MSGs RELATED TO JOIN---
	*/

	//send this msg in order to require to our remoteActor to do start a Join
	public static class RequestJoin implements Serializable {}

	public static class Join implements Serializable {
		int id;
		public Join(int id) {
			this.id = id;
		}
	}

	/*
		----------------------------------------------------
	*/


	/*
		---- HERE ARE DEFINED THE MSGs RELATED TO LEAVE---
	*/

	//message sent from the terminal to the  current client to start leaving operation
	public static class DoLeave implements Serializable {}

	//message sent by the client to node
	public static class Leave implements Serializable {}

	//ack message sent by the leaving node to the client
	public static class AckLeave implements Serializable{
		Integer keyNode;
		boolean successful;

		public AckLeave (Integer keyNode, boolean successful){
			this.keyNode = keyNode;
			this.successful = successful;
		}
	}

	/*
		----------------------------------------------------
	*/



	/*
		---- HERE ARE DEFINED THE MSGs RELATED TO READ---
	*/

	//message sent from terminal to the current client to start reading operation
	public static class DoRead implements Serializable {
		Integer itemKey;

		public DoRead(Integer itemKey) {
			super();
			this.itemKey = itemKey;
		}

	}

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

	public static class ReadTimeout extends MessageRead {
		public ReadTimeout(Integer itemKey) {
			super(itemKey);
			//this.itemKey = itemKey;
		}

	}

	/*
		----------------------------------------------------
	*/

	/*
		---- HERE ARE DEFINED THE MSGs RELATED TO WRITE---
	*/

	//message sent to the current client to start write operation
	public static class DoWrite implements Serializable {
		Integer itemKey;
		String value;

		public DoWrite(Integer itemKey, String value) {
			super();
			this.itemKey = itemKey;
			this.value = value;
		}

	}

	public static class MessageWrite implements Serializable{
		Integer itemKey;
		String value;
		Item item;
		boolean isExisting;

		public MessageWrite (){

		}

		public MessageWrite (Integer itemKey){
			this.itemKey = itemKey;
		}

		public MessageWrite (Integer itemKey, String value){
			this.itemKey = itemKey;
			this.value = value;
		}

		public MessageWrite (Item item){
			this.item = item;
		}

		public MessageWrite (Item item, boolean isExisting){
			this.item = item;
			this.isExisting = isExisting;
		}
	}

	public static class ClientToCoordWriteRequest extends MessageWrite{

		public ClientToCoordWriteRequest(Integer itemKey, String value){
			super(itemKey,value);
		}
	}

	public static class CoordToNodeWriteRequest extends MessageWrite{

		public CoordToNodeWriteRequest(Integer itemKey){
			super(itemKey);
		}

	}

	public static class WriteReplyToCoord extends MessageWrite{

		public WriteReplyToCoord (Item item) {
			super(item);
		}
	}

	public static class WriteReplyToClient extends MessageWrite{
		boolean failed;

		public WriteReplyToClient(Item item, boolean isExisting,boolean failed) {
			super(item,isExisting);
			this.failed = failed;
		}
	}

	public static class CoordToNodeDoWrite extends MessageWrite{

		public CoordToNodeDoWrite (Item item, boolean isExisting) {
			super(item,isExisting);
		}
	}

	public static class rejectWrite extends MessageWrite{
		Integer N;
		public rejectWrite(Integer N) {
			super();
			this.N = N;
		}
	}

	public static class WriteTimeout extends MessageWrite {
		public WriteTimeout(Integer itemKey) {
			super(itemKey);

		}
	}

	/*
		----------------------------------------------------
	*/


	/*
		---- HERE ARE DEFINED THE MSGs RELATED TO RECOVERY---
	*/

	public static class RequestRecovery{
		public RequestRecovery(){

		}
	}

	/*
		----------------------------------------------------
	*/



	/*
		---- HERE ARE DEFINED COMMON MSGs (more than operation)---
	*/

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

	/*
		----------------------------------------------------
	*/

}
