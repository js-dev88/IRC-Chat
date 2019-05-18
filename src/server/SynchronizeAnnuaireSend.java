package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * 
 * Lors d'une connexion reussie a un serveur, on envoie l'annuaire au serveur qui se connecte
 * source : https://gist.github.com/CarlEkerot/2693246
 *  on synchronse aussi le DNS et la networkTable (classe Network)
 */
public class SynchronizeAnnuaireSend implements Runnable{
	private Socket socket = null;
	private String serverName = null;
	private PrintWriter out = null;
	private Thread t2;
	
	public SynchronizeAnnuaireSend(Socket s,String serverName) {
		this.socket = s;
		this.serverName = serverName;
		
	}
	
	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream());
			Scanner sc = new Scanner(new File(socket.getInetAddress().getHostName()+"Annuaire.txt"));
			while(sc.hasNext()){
				String input = sc.nextLine();
				out.println(input);
				out.flush();
			}
			out.println("EOF");
			out.flush();
			System.out.println("Annuary is send to "+serverName);
			sc.close();
			
			//L'annuaire es transmis, on transmet le DNS
			for(Entry<String, String> name : Network.getDNS().entrySet()){
				out.println(name.getKey()+" "+name.getValue());
				out.flush();
			}
			out.println("EODNS");
			out.flush();
			System.out.println("DNS is send to "+serverName);
			
			//Le DNS est transmis, on transmet la network table
		
			for(Entry<String, String> name : Network.getnetworkTable().entrySet()){
				out.println(name.getKey()+" "+name.getValue());
				out.flush();
			}
			out.println("EONT");
			out.flush();
			System.out.println("Network Table is send to "+serverName);
			
			//la network table est transmise, on transmet la liste d'utilisateur connecté
		
			for(User user : Network.getRef()){
				out.println(user.getNick()+" "+user.getCompleteName()+" "+
			                user.getEmail()+" "+user.getUserIp().toString()+" "+
						    user.getUserServer().toString());
				out.flush();
			}
			out.println("EOREF");
			out.flush();
			System.out.println("Referential of connected users is send to "+serverName);
			
			
			//on demarre la reception / transmission entre les deux serveurs
			t2 = new Thread(new ChatClntSrvr(socket,Network.getServerName(socket.getInetAddress().getHostName()),serverName));
			t2.start();
			
			
		} catch (IOException e) {	
			System.err.println("error during annuary synchronization");	
		}
	}
}
