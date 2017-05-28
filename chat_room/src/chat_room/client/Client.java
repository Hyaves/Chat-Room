package chat_room.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client{
	
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	private String address, name, password;
	private int port;
	
	public Client(String name, String password, String address, int port) throws UnknownHostException, IOException{		
		this.name = name;
		this.address = address;
		this.port = port;
		this.password = password;
		
		System.out.println("Connecting to server...");
		socket = new Socket(InetAddress.getByName(this.address), this.port);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.println(this.name);
		out.println(this.password);
	}
	
	public String getName(){
		return name;
	}
	
	public String receive(){
		String text;
		try {
			while((text = in.readLine()) != null){
				System.out.println(text);
				return text;
			}
		} catch (IOException e) {
			return "/e/Server shut down";
		}
		return "";
	}
	
	public void send(String msg){
		out.println(msg);
	}
	
	public void close(){
		synchronized(socket){
			try {	
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
