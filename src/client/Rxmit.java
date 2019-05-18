package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class Rxmit implements Runnable {

	private BufferedReader in;
	private String msg = null;
	public static Thread t5;
	private Socket socket;

	public Rxmit(BufferedReader in, Socket socket){
		this.in = in;
		this.socket = socket;
	}
	public void run() {
		while(true){
			try {
				msg = in.readLine();
				System.out.println(msg);
			} catch (IOException e) {
				System.err.println("Server has been shutdown");
				try {
					socket.close();
					System.exit(0);
				} catch (IOException e1) {
					System.err.println("Can't close the socket");
					
				}
			}
		}
		
		
	}
	
}