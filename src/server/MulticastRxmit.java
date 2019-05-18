package server;


import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map.Entry;

/**
 * Ecoute du canal multicast
 * Code Source https://www.infres.telecom-paristech.fr/people/hudry/coursJava/reseau/multicast.html
 * la communication multicast se fait via UDP, on utilise donc des datagrammes
 */
public class MulticastRxmit implements Runnable{
	
	private InetAddress  groupeIP ;
	private int port;
	private static String name;
	private MulticastSocket socketReception;
	
	
	public MulticastRxmit(String nom){
		try {
			//adresse de type D regroupant tous les participants au canal multicast
			this.groupeIP = InetAddress.getByName("228.6.1.9");
			this.port = 33334;
			socketReception = new MulticastSocket(port);
			name = nom;
			socketReception.joinGroup(groupeIP);
		} catch (UnknownHostException e) {
			System.err.println("Error during multicast reception construction");
		} catch (IOException e) {
			System.err.println("Error during multicast socket construction");
		};
		
	}
	@Override
	public void run() {
		 DatagramPacket message;
		    byte[] contenuMessage;
		    String texte;
		    
		    while(true) {
				  contenuMessage = new byte[1024];
				  message = new DatagramPacket(contenuMessage, contenuMessage.length);
				  try {
					  	  //On reçoit un message sur le canal multicast, et on le traite
			              socketReception.receive(message);
			              texte = (new DataInputStream(new ByteArrayInputStream(contenuMessage))).readUTF();
					      if(!texte.startsWith(":"+name,0)){
					    	  System.out.println(texte);
					    	  treatMessage(texte);
					      }
			    	   
				  }
				  catch(Exception exc) {
			    		System.out.println("You have missed a package from multicast");
				  }
		    }
		
	}
	
	public static void treatMessage(String msg){
		String cmdParser[] = msg.split(" ");
		//String serverOriginAdress = cmdParser[0].substring(1);
		String cmd = cmdParser[1];
		String arg1 = cmdParser[2];
		switch(cmd){
		case "NICK": 
		if(arg1.equals("add")){ 
			String nick = cmdParser[3];
			String pass = cmdParser[4];
			String email = cmdParser[5];
			String completeName = cmdParser[6];
			String ipAddress = cmdParser[7];
			String serverAdress = cmdParser[8];//ajout d'un client au reseau (Annuaire et référentiel utilisateur connectés)
			addClient2Network(nick, pass, email, completeName,ipAddress, serverAdress);
		}else if(arg1.equals("del")){
			String nick = cmdParser[3];
			//supprime le client du reseau TODO
			delClientOfNetwork(nick);
		}else if(arg1.equals("dec")){
			String nick = cmdParser[3];
			//deconnecte le client du reseau
			decClientOfNetwork(nick);
		}
		case "SERVER":
			String serverName = cmdParser[3];
			String serverIpAddress = cmdParser[4];
			if(!name.equals(serverIpAddress)){
				if(arg1.equals("add")){ 
					String serverOrigin = cmdParser[5];
					Network.getDNS().put(serverName, serverIpAddress);
					Network.getnetworkTable().put(serverIpAddress, serverOrigin);
					System.out.println("Multicast Register DNS : "+Network.getDNS());
					System.out.println("Multicast Register Ref : "+Network.connectedList());
					System.out.println("Multicast Register NT : "+Network.getnetworkTable());
				}else if(arg1.equals("dec")){
					Network.getnetworkTable().remove(serverIpAddress);
					ArrayList<String> keys = new ArrayList<String>();
					for(Entry<String, String> s : Network.getnetworkTable().entrySet()){
						if(s.getValue().equals(serverIpAddress)){
							keys.add(s.getKey());
							
						}
					}
					//nettoyage de la table
					for(String key: keys){
						Network.getnetworkTable().remove(key);
					}
					//TODO RECONNECTER AU SERVEUR SUIVANT
					Network.getDNS().remove(serverName);
					System.out.println("Multicast unRegister DNS : "+Network.getDNS());
					System.out.println("Multicast unRegister Ref : "+Network.connectedList());
					System.out.println("Multicast unRegister NT : "+Network.getnetworkTable());
				}			
			}
			break;
		default : System.out.println("@multicast "+cmd+" :Unknown command");
			break;
		}
	}
	
	public static void addClient2Network(String nick, String pass, String email, String completeName,String ipAddress, String serverAdress){
		addOrUpdateClientOnAnnuary(nick, pass, email, completeName, serverAdress);
		addOrUpdateClientOnRef(nick, pass, email, completeName, ipAddress, serverAdress);
	}
	
	public static void delClientOfNetwork(String nick){
		
	}
	
    public static void decClientOfNetwork(String nick){
    	System.out.println(Network.getRef());
    	User utoRemov = null;
		for(User user : Network.getRef()){
			if(user.getNick().equals(nick)){
				utoRemov = user;
				break;
			}
		}
		Network.getRef().remove(utoRemov);
		System.out.println("Updated Ref : \n"+Network.getRef());
	}
    
    public static void addOrUpdateClientOnRef(String nick, String pass, String email, String completeName, String ipAddress, String serverAdress){
		Network.setConnected(new User(nick, email, completeName,ipAddress, serverAdress));
		System.out.println("Updated Ref : "+Network.connectedList());
   	}
    
    public static void addOrUpdateClientOnAnnuary(String nick, String pass, String email, String completeName, String serverAdress){
    	String serverDatabase = name+"Annuaire.txt";
    	String userToREgister = nick+" "+pass+" "+email+" "+completeName;
    	boolean update = false;
		try (Scanner sc = new Scanner(new File(serverDatabase));){
	    	String toReplace="";
	    	while(sc.hasNext() && !update) {
	    		String input = sc.nextLine();
	    		toReplace += input+ System.lineSeparator();
	    		String words[] = input.split(" ");
	    		if(words[0].equals(nick) && words[1].equals(pass)){ //l'utilisateur existe deja
	    			try (BufferedWriter bw = new BufferedWriter(new FileWriter(serverDatabase))) {
	    					while(sc.hasNext()) {
	    						toReplace += sc.nextLine()+ System.lineSeparator();
	    					}
	    					toReplace = toReplace.replace(input, userToREgister);
	    					bw.write(toReplace);
	    					System.out.println("User has been updated by multicast");
	    					update = true;
	    			} catch (IOException e) {
	    				System.err.println("Error during update of informations");
	    			}
	    		}

	    	}
	    	if(!update){
	    		try (BufferedWriter bw = new BufferedWriter(new FileWriter(serverDatabase,true))) {//l'utilisateur n'existe pas
					bw.write(userToREgister+ System.lineSeparator());
					System.out.println("User Registered in the annuary by multicast");
				} catch (IOException e) {
					System.err.println("Error during user registration");
				}
	    	}
	    	
	    } catch (FileNotFoundException e1) {
	    	System.err.println("Database doesn't exist!");
		}
		
    }

}
