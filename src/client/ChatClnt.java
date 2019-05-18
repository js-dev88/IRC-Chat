package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import client.Rxmit;
import client.Txmit;

public class ChatClnt implements Runnable {

	private Socket socket;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Thread t3, t4;
	public ChatClnt(Socket s){
		this.socket = s;
	}
	public void run() {
		
			try {
				out = new PrintWriter(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
				t4 = new Thread(new Txmit(out));
				t4.start();
				t3 = new Thread(new Rxmit(in, socket));
				t3.start();
			} catch (IOException e) {
				System.err.println("The server has been disconnected!");
				try {
					socket.close();
					System.exit(0);
				} catch (IOException e1) {
					System.err.println("Can't close the socket");
				}
				
		}
		
	}
}
