package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

/**
 * Afin de gérer le réseau, nous voulons :
 * 
 * d'une part conserver la liste des servers connectés 
 * RF 2813  When a new server is connected
 * to net, information about it MUST be broadcasted to the whole
 * network.
 * 
 * d'autre part, l'architecture du réseau IRC est un arbre, un serveur ne peut etre conecté qu'a un seul serveur 
 * il peut cependant recevoir plusieurs connexions
 *
 */
public class Network {
	//référentiel des commandes
	private static ArrayList<Command> commandRef;
	//référentiel des channels
	private static HashMap<String, ArrayList<User>> channelRef;
	private static HashMap<String, String> DNS;
	private static HashMap<String, String> networkTable;
	private static ArrayList<User> whoIsConnected;
	private static Thread t0;
	private static Thread t00;
	private static Thread tMc;
	private int numberOfConnection;
	private static String thisServerName;
	private static HashMap<String, Socket> socketTable;
	
	
	 private static Network INSTANCE = null;

	    public static Network getInstance(String name){           
	        if (INSTANCE == null) 
	        	INSTANCE = new Network(name); 
	     
	        return INSTANCE;
	    }
	
	private Network(String name) {
		//C'est l'ip
		thisServerName = name;
		//on instancie la liste de commandes disponbiles et si elles sont implementees
		commandRef = createCommandRef();
		//on initialse la socket table
		socketTable = new HashMap<>();
		//on initialise la liste de channel
		channelRef = new HashMap<>();
		//on retient la liste de tous les servers enregistrés sur le réseau <Nom, ipAdress>
		DNS = new HashMap<String, String>();
		//on map le réseau <adresseServerHost, adresseServerNew>
		networkTable = new HashMap<String, String>();
		//referentiel des utilisateurs connectes
		whoIsConnected = new ArrayList<User>();
		//non utilisé
		this.numberOfConnection =0;
		//on lance l'écoute du canal multicast
		launchMulticastReciever(thisServerName);
		
	}
	
	public static void addToSocketTable(String userOrServer, Socket s){
		socketTable.put(userOrServer, s);
	}
	public static Socket getSocketFromTable(String userOrServer){
		return socketTable.get(userOrServer);
	}
	public static HashMap<String, Socket> returnSocketTale(){
		return socketTable;
	}
	public static HashMap<String, ArrayList<User>> getChannelRef(){
		return channelRef;
	}
	public static void  addChanneltoRef(String channelName){
		 channelRef.put(channelName, new ArrayList<User>());
	}
	public static void  addUserToChannel(String channelName, User u){
		 ArrayList<User> listOfUSers =  channelRef.get(channelName);
		 listOfUSers.add(u);
		 channelRef.put(channelName, listOfUSers);
	}
	public static String getChannelRefDirectory(){
		String s = "";
		for(Entry<String, ArrayList<User>> chanel: channelRef.entrySet()) {
			s += chanel.getKey();
			for(User u : chanel.getValue()){
				s += u.getNick();
			}
			s+="\n";
		}
		return s;
	}
	public static ArrayList<Command>  createCommandRef(){
		ArrayList<Command> cmdRef = new ArrayList<Command>();
		//Comandes spécifiques au projet
		cmdRef.add(new Command("HELP","No argument needed",true)); 
		cmdRef.add(new Command("CONN","Args : me or <serverIpAddress>",true));  
		cmdRef.add(new Command("GDPR","Args : me",false)); 
		//Comandes de connexion
		cmdRef.add(new Command("PASS","",false)); 
		cmdRef.add(new Command("NICK","",false)); 
		cmdRef.add(new Command("USER","",false)); 
		cmdRef.add(new Command("OPER","",false)); 
		cmdRef.add(new Command("MODE","",false)); 
		cmdRef.add(new Command("SERVICE","",false)); 
		cmdRef.add(new Command("QUIT","",false)); 
		cmdRef.add(new Command("SQUIT","",false)); 
		//Comandes de channel
		cmdRef.add(new Command("JOIN","",false)); 
		cmdRef.add(new Command("PART","",false)); 
		cmdRef.add(new Command("TOPIC","",false)); 
		cmdRef.add(new Command("NAMES","",false)); 
		cmdRef.add(new Command("LIST","",false)); 
		cmdRef.add(new Command("INVITE","",false)); 
		cmdRef.add(new Command("KICK","",false)); 
		//Envoi de message
		cmdRef.add(new Command("PRIVMSG","PRIVMSG <target> :<my message>",false)); 
		cmdRef.add(new Command("NOTICE","",false)); 
		cmdRef.add(new Command("MOTD","",false)); 
		cmdRef.add(new Command("LUSERS","",false)); 
		cmdRef.add(new Command("VERSION","",false)); 
		cmdRef.add(new Command("STATS","",false)); 
		cmdRef.add(new Command("LINKS","",false)); 
		cmdRef.add(new Command("TIME","No argument Required",true));
		//commande serveur
		cmdRef.add(new Command("CONNECT","",false)); 
		cmdRef.add(new Command("TRACE","",false));
		cmdRef.add(new Command("ADMIN","",false)); 
		cmdRef.add(new Command("INFO","",false)); 
		//Services
		cmdRef.add(new Command("SERVLIST","",false)); 
		cmdRef.add(new Command("SQUERY","",false)); 
		//User based
		cmdRef.add(new Command("WHO","",false)); 
		cmdRef.add(new Command("WHOIS","",false)); 
		cmdRef.add(new Command("WHOWAS","",false)); 
		//Divers
		cmdRef.add(new Command("KILL","",false)); 
		cmdRef.add(new Command("PING","",false)); 
		cmdRef.add(new Command("PONG","",false)); 
		cmdRef.add(new Command("ERROR","",false));
		cmdRef.add(new Command("AWAY","",false)); 
		cmdRef.add(new Command("REHASH","",false));
		cmdRef.add(new Command("DIE","",false)); 
		cmdRef.add(new Command("RESTART","",false)); 
		cmdRef.add(new Command("SUMMON","",false)); 
		cmdRef.add(new Command("USERS","",false)); 
		cmdRef.add(new Command("WALLOPS","",false)); 
		cmdRef.add(new Command("USERHOST","",false)); 
		cmdRef.add(new Command("ISON","",false)); 
		return cmdRef;
	}
	
	public static String getNiceCommandRef(){
		 ArrayList<Command> ac = getCommandRef();
		 String help="\n";
		 for(Command c : ac){
			 help += c.toString();
		 }
		 return help;
	}
	public static ArrayList<Command>  getCommandRef(){
		return commandRef;
	}
	public static boolean isValidCommand(String cmd){
		for(Command comd : commandRef){
			if( comd.getName().equals(cmd)){
				return true;
			}
		}
		 return false;
	}
	
	public int getNumberOfConnection() {
		return numberOfConnection;
	}

	public void setNumberOfConnection(int numberOfConnection) {
		this.numberOfConnection = numberOfConnection;
	}

	public static void launchMulticastReciever(String name){
		t0 = new Thread(new MulticastRxmit(name));
		t0.start();

	}
	
	public static void launchMulticastTransmiter(String name, String mess){
		t00 = new Thread(new MulticastTxmit(name, mess));
		t00.start();
	}
	
	public static boolean isAlreadyRegistered(String serverName){
		if(DNS.containsKey(serverName)){
			return true;
		}
		return false;
	}
	
	public static void registerServer(String ipAdress){
		DNS.put("server1", ipAdress);
		System.out.println("Register DNS : "+DNS);
		System.out.println("Register Ref : "+connectedList());
		System.out.println("Register NT : "+getnetworkTable());
	}
	public static void registerServer(String ipAdress, String serverName){
		DNS.put(serverName, ipAdress);
		networkTable.put(ipAdress, thisServerName);
		System.out.println("Register DNS : "+DNS);
		System.out.println("Register Ref : "+connectedList());
		System.out.println("Register NT : "+getnetworkTable());
		String mcMessAnuaire ="SERVER add "+serverName+" "+ipAdress+" "+thisServerName;
		tMc = new Thread(new MulticastTxmit(thisServerName,mcMessAnuaire));
		tMc.start();
	}
	
	public static void unRegisterServer(String serverName){
		//deconnexion des utilisateurs du serveur
		setDisconnectedWhenServerIsDown(DNS.get(serverName));
		networkTable.remove(DNS.get(serverName));
		ArrayList<String> keys = new ArrayList<String>();
		for(Entry<String, String> s : networkTable.entrySet()){
			if(s.getValue().equals(DNS.get(serverName))){
				keys.add(s.getKey());
				
			}
		}
		//nettoyage de la table
		for(String key: keys){
			networkTable.remove(key);
		}
		
		//TODO RECONNECTER AU SERVEUR SUIVANT
		DNS.remove(serverName);
		System.out.println("unRegister DNS : "+DNS);
		System.out.println("unRegister Ref : "+connectedList());
		System.out.println("unRegister NT : "+getnetworkTable());
		String mcMessAnuaire ="SERVER dec "+serverName+" "+DNS.get(serverName);
		tMc = new Thread(new MulticastTxmit(thisServerName,mcMessAnuaire));
		tMc.start();
	}
	public static String getServerName(String ipAdress){
		for(Entry<String, String> name : DNS.entrySet()){
			if(name.getValue().equals(ipAdress)){
				return name.getKey();
			}
		}
		return null;
	}
	public static HashMap<String, String> getDNS(){
		return DNS;
	}
	public static void addDNS(String name, String ip){
		DNS.put(name,  ip);
	}
	public static HashMap<String, String> getnetworkTable(){
		return networkTable;
	}
	public static void addnetworkTable(String srvIsconnected2, String thisServer){
		networkTable.put(srvIsconnected2,  thisServer);
	}
	
	public static ArrayList<User> getRef(){
		return whoIsConnected;
	}
	public static void addRef(String nick, String completeName, String email, String userIp, String userServerIP){
		whoIsConnected.add(new User(nick, completeName, email, userIp, userServerIP));
	}
	
	public static void setConnected(User u) {
		 whoIsConnected.add(u);
		 System.out.println("Set connected user Ref : "+connectedList());
	}
	
	public static boolean setDisconnected(String nick, Socket socket) {
		User utoRemov = null;
		for(User user : whoIsConnected){
			if(user.getNick().equals(nick)){
				utoRemov = user;
				break;
			}
		}
		if(whoIsConnected.remove(utoRemov)){
			String mcMessAnuaire ="NICK dec "+nick;
			tMc = new Thread(new MulticastTxmit(socket.getInetAddress().getHostName(),mcMessAnuaire));
			tMc.start();
			
			System.out.println("disconected user DNS : "+DNS);
			System.out.println("disconected user Ref : "+connectedList());
			System.out.println("disconected user NT : "+getnetworkTable());
			return true;
		}
		
		return false;
	}
	
	public static void setDisconnectedWhenServerIsDown(String hostIpServer) {
		ArrayList<User> userToremove = new ArrayList<>();
		for(User user : whoIsConnected){
			if(user.getUserServer().equals(hostIpServer)){
				userToremove.add(user);
			}
		}
		whoIsConnected.removeAll(userToremove);
		for(User u : userToremove){
			System.err.println(u.getNick()+"has been disconnected - server is Down");
			String mcMessAnuaire ="NICK dec "+u.getNick();
			tMc = new Thread(new MulticastTxmit(hostIpServer,mcMessAnuaire));
			tMc.start();
		}
		System.out.println("disconected user DNS : "+DNS);
		System.out.println("disconected user Ref : "+connectedList());
		System.out.println("disconected user NT : "+getnetworkTable());
		

	}

	public static boolean isConnected(String nick){
		for(User user : whoIsConnected){
			if(user.getNick().equals(nick)){
				return true;
			}
		}
   	return false;
   }
	
	public static String serverFromUSerCon(String nick){
		User u;
		for(User user : whoIsConnected){
			if(user.getNick().equals(nick)){
				u = user;
				return u.getUserServer();
			}
		}
		return "noserver";
	}
	
	public static String getWhoIs(String nick){
		String whoIs = "noOne";
		for(User user : whoIsConnected){
			if(user.getNick().equals(nick)){
				return user.getCompleteName();
			}
		}
		return whoIs;
	}
	
	public static String connectedList(){
		return whoIsConnected.toString();
	}
	
	public static boolean isInAnnuary(String nick){
		String serverDatabase = thisServerName+"Annuaire.txt";
		try (Scanner sc = new Scanner(new File(serverDatabase));){
			while(sc.hasNext() ) {
				String input = sc.nextLine();
				String words[] = input.split(" ");
				if(words[0].equals(nick)){
					return true;
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found");
		}
		return false;
	}
	
	public static String searchNextServer2Transmit(String user){
		String targettedServer = Network.serverFromUSerCon(user);
		String relayServer= null;
		String originServer = thisServerName;
		
		while(!Network.getnetworkTable().get(targettedServer).equals(originServer)){
				relayServer = Network.getnetworkTable().get(targettedServer);
				targettedServer = relayServer;	
		}
		return targettedServer;
	}
	
}
