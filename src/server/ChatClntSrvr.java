package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClntSrvr implements Runnable {
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private String nick = null, serverHost =null;
	private Thread t3;

	public ChatClntSrvr(Socket s, String nick, String serverHost) {
		Network.addToSocketTable(nick, s);
		this.socket = s;
		this.nick = nick;
		this.serverHost =serverHost;
	}
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
		    t3 = new Thread(new Rxmit(in,nick,serverHost,socket,out));
			t3.start();
			
		} catch (IOException e) {
			Network.setDisconnected(nick, socket);
			Network.unRegisterServer(nick);
			System.err.println(nick +" is disconnected");
			
		}
	}
}
