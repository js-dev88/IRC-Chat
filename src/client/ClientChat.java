package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Main de la classe client Demande une connexion, la connexion s'établit Le
 * menu est renvoyé automatiquement par le server => gestion du menu côté client
 * dans la classe Menu
 */
public class ClientChat {
	public static Socket socket = null;
	public static int port = 33333;
	public static Thread t1;

	// public static ArrayList<String> connectedServers;
	public static HashMap<Integer, InetAddress> connectedServers = getPossibleServersList();
	private static Scanner sc = null;

	public static void main(String[] args) {
		boolean bool = false;
		while(!bool){
			try {
				System.setProperty("file.encoding", "UTF-8");
				System.out.println("Asking for connexion");
				// création de la socket dirigée vers le serveur local 127.0.0.1 et sur le port
				// 33333
				int choice = 0;
				//On offre le choix au client de selectionner un serveur dans la plage de serveur réservée) 
				// Si le serveur selectionné n'est pas disponible, l'utilisateur devra relancer l'appli
				// et selectionner un autre serveur
				do {
					System.out.println("Please select a server (if server not available, try another one)");
					for (int i = 1; i < connectedServers.size(); ++i)
						System.out.println(i + " - " + connectedServers.get(i));

					System.out.println("Please select one of the number before the \" - \" (ex: 1)");
					sc = new Scanner(System.in);
					if (sc.hasNextInt())
						choice = sc.nextInt();
				} while (!connectedServers.containsKey(choice));

				socket = new Socket(connectedServers.get(choice), port);
				bool = true;
				// Si le message s'affiche c'est que je suis connecté
				System.out.println("Connexion established with the server : " + socket.getInetAddress().getHostAddress());
				// Demande de connexion
				t1 = new Thread(new Cnnxn(socket));
				t1.start();

			} catch (UnknownHostException e) {
				System.err.println("Impossible to connect at " + socket.getLocalAddress());
			} catch (IOException e) {
				System.err.println("No server is listening to the port :" + port);
			}	
		}
		
	}

	public static HashMap<Integer, InetAddress> getPossibleServersList() {

		HashMap<Integer, InetAddress> servers = new HashMap<>();
		int maxPlage = 12;
		try {
			for (int i = 1; i < maxPlage; ++i)
				servers.put(i, InetAddress.getByName("127.0.0." + i));
		} catch (UnknownHostException e) {
			System.err.println("Error during the list of servers generation");
		}

		return servers;

	}
}
