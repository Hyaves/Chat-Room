package chat_room.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class UserData {

	private static Map<String, String> users = new HashMap<>();
	private static Map<String, String> friends = new HashMap<>();
	private static ObjectOutputStream oos, oosF;	
	private static ObjectInputStream ois, oisF;
	private static String path = "res/RegisteredUsers.ser";
	private static String pathF = "res/FriendList.ser";
	
	static{
		try {
			if(new File(path).exists()){
				ois = new ObjectInputStream(new FileInputStream(path));
				users = (Map<String, String>)ois.readObject();
			}
			if(new File(pathF).exists()){
				oisF = new ObjectInputStream(new FileInputStream(pathF));
				friends = (Map<String, String>)oisF.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("File not found!");
		}		
	}
	
	public static void register(String name, String password){
		try {						
			users.put(name, password);
			oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(users);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String authenticate(String key){
		return users.get(key);
	}
	
	public static boolean search(String name){
		if(users.containsKey(name)){
			return true;
		}
		return false;
	}
	
	public static void createFriendsList(String name){				
		friends.put(name, "");
		saveFriendList();
	}
	
	public static void addFriend(String name, String friend){
		String list = friends.get(name);
		list += friend + "/./";
		friends.replace(name, list);
		saveFriendList();
	}
	
	public static void removeFriend(String name, String friend){
		String list = friends.get(name);
		String[] values = list.split("/./");
		for(int i = 0; i < values.length; i++){
			if(values[i].equals(friend)){
				String temp = values[0];
				values[0] = values[i];
				values[i] = temp;
			}
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < values.length - 1; i++){
			sb.append(values[i + 1] + "/./");
		}
		friends.replace(name, sb.toString());
	}
	
	private static void saveFriendList(){
		try {						
			oosF = new ObjectOutputStream(new FileOutputStream(pathF));
			oosF.writeObject(friends);
			oosF.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getFriendList(String name){
		return friends.get(name);
	}
	
	public static boolean isMapEmpty(){
		if(users.isEmpty()){
			return true;
		}
		return false;
	}
	
	public static void clearMap(){
		users.clear();
	}
	
	public static void resetPassword(String key){
		if(!users.isEmpty()){
			users.remove(key);
		}
	}

}
