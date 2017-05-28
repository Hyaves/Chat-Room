package chat_room.server;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable{
	
	private ServerSocket serverSocket;
	private Thread mainThread, listenThread;
	private Scanner scanner;

	private volatile List<ServerClient> clients = new ArrayList<>();	
	private List<String> banList = new ArrayList<>();
	private boolean running = false;
	public Server server;

	public Server(int port){		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server = this;
		
		mainThread = new Thread(this, "main");
		mainThread.start();
	}
	
	private void listen(){
		listenThread = new Thread("listen"){
			public void run(){
				while(running){
					Socket clientSocket = null;
					try {
						clientSocket = serverSocket.accept();
					} catch (IOException e) {
						System.out.println("Connection closed.");
					}
					if(clientSocket == null) return;
					ServerClient c = new ServerClient(clientSocket, server);
					
					if(UserData.isMapEmpty() || !UserData.search(c.getName())){
						UserData.register(c.getName(), c.getPassword());
						UserData.createFriendsList(c.getName());
					} else {
						if(!UserData.authenticate(c.getName()).equals(c.getPassword())){
							c.send("/d/Incorrect password");
							continue;
						} 
					}

					if(checkForDouble(c)){
						c.send("/d/Username already taken");
						continue;
					}
					if(checkForBan(c.getAddress())){
						c.send("/d/User banned");
						continue;
					}
					c.send("/c/Welcome " + c.getName());
					clients.add(c);
					new Thread(c).start();
					c.send("/f/" + UserData.getFriendList(c.getName()));
					System.out.println("Connected: " + c.getName() + " @ " + c.getAddress() + ":" + c.getPort());
					updateList();
					sendToAll("/m/" + c.getName() + " joined.");
				}
			}
		};
		listenThread.start();
	}
	
	public void process(String text, ServerClient sc) {	
		String message;
		if(text.startsWith("/m/")){
			message = text.substring(3, text.length());
			sendToAll("/m/" + sc.getName() + ": " + message);
			return;
		}
		if(text.startsWith("/p/")){
			message = text.substring(3, text.length());
			String recipient = message.substring(0, message.indexOf(' '));
			for(int i = 0; i < clients.size(); i++){
				if(recipient.equals(clients.get(i).getName())){
					message = message.substring(recipient.length(), message.length());
					sendPrivate(message, recipient, sc.getName());
					return;
				}
			}
			sc.send("/o/" + recipient + " *user offline");
		}
		if(text.startsWith("/f/")){
			message = text.substring(3, text.length());
			if(UserData.search(message)){
				UserData.addFriend(sc.getName(), message);
				sc.send("/f/" + UserData.getFriendList(sc.getName()));
			} else {
				sc.send("/m/Invalid username");
			}
		}
		if(text.startsWith("/r/")){
			message = text.substring(3, text.length());
			UserData.removeFriend(sc.getName(), message);
			sc.send("/f/" + UserData.getFriendList(sc.getName()));
		}
	}	

	private void sendToAll(String msg){
		for(int i = 0; i < clients.size(); i++){
			clients.get(i).send(msg);
		}
	}

	private void sendPrivate(String msg, String recipient, String sender){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).getName().equals(recipient)){
				clients.get(i).send("/p/" + msg);
			} else if(clients.get(i).getName().equals(sender)){
				clients.get(i).send("/s/" + recipient + msg);
			}
		}
	}
	
	private void updateList(){
		StringBuilder sb = new StringBuilder("/u/");
		for(int i = 0; i < clients.size(); i++){
			sb.append(clients.get(i).getName() + "/./");
		}
		sendToAll(sb.toString());
	}
	
	private boolean checkForBan(String address){
		for(int i = 0; i < banList.size(); i++){
			if(banList.get(i).equals(address)){
				return true;
			}
		}
		return false;
	}
	
	private boolean checkForDouble(ServerClient sc){
		for(int i = 0; i < clients.size(); i++){
			if(clients.get(i).getName().equals(sc.getName())){
				return true;
			}
		}
		return false;
	}
	
	public void removeUser(ServerClient sc, boolean status){
		clients.remove(sc);
		updateList();
		if(!status){
			sendToAll("/m/" + sc.getName() + " left.");
		} else
		{
			sendToAll("/m/" + sc.getName() + " removed.");
		}

	}

	@Override
	public void run() {
		running = true;
		listen();
		scanner = new Scanner(System.in);
		System.out.println("Server is running on port: " + serverSocket.getLocalPort());
		while(running){						
			String text = scanner.nextLine();
			if(!text.startsWith("-") && !text.equals("")){
				sendToAll("/m/Admin: " + text);
				System.out.println(text);
			} else if(text.startsWith("-shutdown")){
				shutdown();
			} else if(text.startsWith("-clients")){
				System.out.println("========================");
				for(int  i = 0; i < clients.size(); i++){
					ServerClient c = clients.get(i);
					System.out.println(c.getName() + " @" + c.getAddress() + ":" + c.getPort());
				}
				System.out.println("========================");
			} else if(text.startsWith("-kick")){
				text = text.split(" ")[1];
				boolean exist = false;
				for(int i = 0; i < clients.size(); i++){
					ServerClient c = clients.get(i);
					if(text.equals(c.getName())){					
						exist = true;
					}
					if(exist){
						c.send("/d/Kicked from server");
						removeUser(c, true);
						System.out.println(c.getName() + " kicked");
					} else {
						System.out.println("Invalid username");
					}
				} 
			} else if(text.startsWith("-ban")){
				text = text.split(" ")[1];
				boolean exist = false;
				for(int i = 0; i < clients.size(); i++){
					ServerClient c = clients.get(i);
					if(text.equals(c.getName())){					
						exist = true;
					}
					if(exist){
						banList.add(c.getAddress());
						c.send("/d/Banned from server");
						removeUser(c, true);
						System.out.println(c.getName() + " banned");
					} else {
						System.out.println("Invalid username");
					}
				} 
			} else if(text.startsWith("-unban")){
				text = text.split(" ")[1];
				boolean exist = false;
				for(int i = 0; i < banList.size(); i++){
					if(text.equals(banList.get(i))){					
						exist = true;
					}
					if(exist){
						banList.remove(text);
						System.out.println(text + " unbanned");
					} else {
						System.out.println("Invalid address");
					}
				} 
			} else if(text.startsWith("-hardreset")){
				UserData.clearMap();
				System.out.println("All passwords reseted");
			} else if(text.startsWith("-reset")){
				text = text.split(" ")[1];
				if(UserData.search(text)){
					UserData.resetPassword(text);
					System.out.println(text + "'s password reseted");
				} else {
					System.out.println("Incorrect username");
				}
			}		
		}
		try {
			listenThread.join();
			System.out.println("Server shut down");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void shutdown(){
		running = false;
		scanner.close();
		sendToAll("/e/Server shut down");
		for(int i = 0; i < clients.size(); i++){
			clients.get(i).close();
		}
		clients.clear();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args){
		
		EventQueue.invokeLater(new Runnable(){

			@Override
			public void run() {
				new Server(3231);
			}			
		});
	}
	
}
