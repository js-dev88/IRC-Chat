package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * 
 * La classe gère l'appel d'un client, on accepte l'appel et on affiche le menu d'accueil
 *
 */
public class AcceptCnx implements Runnable {
	private ServerSocket socketserver = null;
	private Socket socket = null;
	public Thread t1;
	

	public AcceptCnx(ServerSocket ss){
		socketserver = ss;
	}
	public void run() {
		try {
			while(true) {
				//on accepte la demande du client

				socket = socketserver.accept();
				System.out.println(socketserver.getInetAddress().getHostName());
				System.out.println(socket.getInetAddress().getHostName());
				System.out.println(socket.getRemoteSocketAddress().toString());
				System.out.println("Request of connexion from client ");
				t1 = new Thread(new Identification(socket));
				t1.start();
			}
		} catch (IOException e) {
			System.err.println("Server error");
			e.printStackTrace();
		}
	}
}
