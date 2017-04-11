import java.io.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gianluke on 11/04/17.
 */
public class Utilities {

	public static void goBackToTerminal(){
		System.out.print(">> ");
	}

	/*
		This method is responsible to load local storage of the node
	 */
	public static void loadItems(int myId, Map<Integer,Item> items){
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
			} catch (IOException e) {
				System.out.println("ERROR: An error occured while reading arguments");
			}


		}

	}

	// initialize storage file and add items
	public static void initializeStorageFile (int myId, Map<Integer, Item> items){

		String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file
		try{
			PrintWriter writer = new PrintWriter(storagePath, "UTF-8");
			for (Item i : items.values()){
				writer.println(i.toString());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
		Append the item in the local storage of the node
	 */
	public static void appendItemToStorageFile(int myId, Item item){

		String storagePath = "./"+myId+"myLocalStorage.txt"; //path to file
		try(FileWriter fw = new FileWriter(storagePath, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			out.println(item.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void updateLocalStorage(int myId, Map<Integer,Item> items){
		initializeStorageFile(myId,items);
	}

}
