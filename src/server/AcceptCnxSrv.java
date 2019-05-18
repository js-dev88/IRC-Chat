package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptCnxSrv implements Runnable{
	ServerSocket socketserver = null;
	private Socket socket = null;
	public Thread t1;
	

	public AcceptCnxSrv(ServerSocket ss){
		socketserver = ss;
	}
	public void run() {
		try {
			while(true) {
				//on accepte la demande du client
				socket = socketserver.accept();
				System.out.println("Request of connexion from server ");
				t1 = new Thread(new IdentificationSrv(socket));
				t1.start();
			}
		} catch (IOException e) {
			System.err.println("Server error");
			e.printStackTrace();
		}
	}
}
