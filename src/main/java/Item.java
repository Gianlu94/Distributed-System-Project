//package src.main.java;

import scala.Serializable;

/**
 * Created by gianluke on 16/03/17.
 */
/*
		This is the class that identify an item
*/

public class Item implements Serializable {
	private Integer key;
	private String value;
	private Integer version;

	public Item(Integer key, String value, Integer version ){
		this.setKey(key);
		this.setValue(value);
		this.setVersion(version);
	}

	public String toString(){
		String res = getKey() + " " + getValue() + " " + getVersion();
		return res;
	}

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
