package server;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Main du server on lance cette classe pour demarrer un serveur
 * 
 *
 */
public class SrvrChatMain {
	//server socket pour les clients
	private static ServerSocket serversocketClt = null;
	//server socket pour la communication entre serveurs
	private static ServerSocket serversocketSvr = null;
	// socket pour la communication entre serveurs
	private static Socket socket = null;
	private static Thread t;
	private static Thread t0;
	private static Thread t1;
	//on choisit de separer les connexions clients et serveurs (on aurait pu garder le même port et traiter les deux simultanément)
	private static int portClientCnx = 33333;
	private static int portServerCnx = 33335;
	private static String serverName;


	public static void main(String[] args) {
		
			//variable système pour sécuriser l'encoding
			System.setProperty( "file.encoding", "UTF-8" );
			// on ouvre une socket sur le premier server disponible et sur le port 33333, on écoute les clients
			serversocketClt = connectTothefirstPossibleAdresse();
			//Affichage de contrôle
			serverName = serversocketClt.getInetAddress().getHostAddress();
			System.out.println("Server "+serverName+" is listening on the port "+serversocketClt.getLocalPort());
			// on ouvre une socket sur le premier server disponible et sur le port 33333, on écoute les servers
			try {
				serversocketSvr =  new ServerSocket(portServerCnx,0, serversocketClt.getInetAddress());
			} catch (IOException e1) {
				System.err.println("Error with the socket server dedicated to server connections");
			}
			
			//Le server est mis en place, on instancie un annuaire propre au serveur
			//un annuaire par server (RFC 2813 2.Global database) mais théoriquement identiques donc global au réseau
			//ici on prefixe le fichier de l'adresse du serveur pour faciliter le debug, car nous développons en local et dans un même fichier
			String serverDatabase = serverName+"Annuaire.txt";
			File file = new File(serverDatabase);
			try {
				
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("Can't generate database file");
			}
			//On veut se connecter au reseau, on demande l'adresse de connection en prompt, s'il s'agit du premier serveur :
			//la commande est CONN me
			// sinon on doit connecter manuellement le server à un autre noeud du reseau
			Scanner sc = new Scanner(System.in);
			String conn;
			String msg[];
			boolean bool;
			do{
				 //tant que la commande CONN n'est pas juste
				 bool =true;
				 System.out.println("Connect to IRC => CONN <ipServer> or CONN me for the first server");
				 conn = sc.nextLine();
				 msg = conn.split(" ");
				 
				 if(!msg[0].toUpperCase().equals("CONN") || msg.length < 2){
					 bool =false;
				 }else{
					 if(!msg[1].equals("me")){
						 try {
								socket = new Socket(msg[1],portServerCnx);
								
							} catch (UnknownHostException e) {
								System.out.println("Impossible to connect at "+msg[1]);
								 bool =false;
							} catch (IOException e) {
								System.out.println("No server is listening at the adress :"+msg[1]);
								 bool =false;
							}
					 }
						 
				 }
				 
			}while(!bool);
			// on prepare le serveur pour le reseau via un pattern singleton (on ne l'instancie qu'une fois)
			Network.getInstance(serverName);
		
			//Si ce n'est pas le premier serveur
			if(!msg[1].equals("me")){	
				//on lance la classe de connexion à un autre serveur
				t1 = new Thread(new CnnxnSrv(socket,sc,serverName));
				t1.start();
				
			}else{
				Network.registerServer(serverName);
			}
			//on accepte la demande d'un client lorsqu'elle arrive
			t = new Thread(new AcceptCnx(serversocketClt));
			t.start();
			//on accepte la demande d'un server lorsqu'elle arrive
			t0 = new Thread(new AcceptCnxSrv(serversocketSvr));
			t0.start();
	
	}
	
	/**
	 * Pour les besoins du projet, nous devons être capable de lancer des serveurs locaux différents avec le même programme
	 * Nous avons choisi d'instancier en dur la liste de serveurs disponibles
	 * toutes les adresses IPv4 comprises entre 127.0.0.1 et 127.255.255.255 peuvent faire l'affaire
	 * Sources : https://fr.wikipedia.org/wiki/Localhost
	 */
	public static LinkedList<InetAddress> getPossibleServersList(){
		
		LinkedList<InetAddress> listServer = new LinkedList<>();
		try {
			listServer.add(InetAddress.getByName("127.0.0.1"));
			listServer.add(InetAddress.getByName("127.0.0.2"));
			listServer.add(InetAddress.getByName("127.0.0.3"));
			listServer.add(InetAddress.getByName("127.0.0.4"));
			listServer.add(InetAddress.getByName("127.0.0.5"));
			listServer.add(InetAddress.getByName("127.0.0.6"));
			listServer.add(InetAddress.getByName("127.0.0.7"));
			listServer.add(InetAddress.getByName("127.0.0.8"));
			listServer.add(InetAddress.getByName("127.0.0.9"));
			listServer.add(InetAddress.getByName("127.0.0.10"));
			listServer.add(InetAddress.getByName("127.0.0.11"));
		} catch (UnknownHostException e) {
			System.err.println("Error during the list of servers generation");
		}
		
		return listServer;
		
	}
	
	/**
	 * On parcours la liste précédente, si un server est déjà instancié, onprend l'adresse suivante
	 * 
	 */
	public static ServerSocket connectTothefirstPossibleAdresse(){
		InetAddress addr;
		for(int i = 0; i < getPossibleServersList().size(); i++){
			addr = getPossibleServersList().get(i);
			try {
				// ss avec un backlog = 0 =>  50 connections maximum
				serversocketClt = new ServerSocket(portClientCnx,0,addr);
				return serversocketClt;
			} catch (IOException e) {
				//on recherche une adresse disponible, on ne catch pas l'eventuelle erreur
			}
			
		}
		return null;
	}
	
	
}
