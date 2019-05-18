package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Une fois connecté, le serveur recupere l'annuaire depuis le serveur d'origine
 * source : https://gist.github.com/CarlEkerot/2693246
 * on synchronse aussi le DNS et la networkTable (classe Network)
 */
public class SynchronizeAnnuaireRecieve implements Runnable {
		private Socket socket = null;
		private String serverName = null;
		private BufferedReader in = null;
		private String ipaddress= null;
		private Thread t2;
		
		public SynchronizeAnnuaireRecieve(Socket s,String serverName,String ipaddres) {
			this.socket = s;
			this.serverName = serverName;
			this.ipaddress = ipaddres;
			
		}
		
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));			
				String serverDatabase = ipaddress+"Annuaire.txt";
				File file = new File(serverDatabase);
				if (file.exists()) {
				     file.delete(); 
				}
				String line;
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(serverDatabase))) {
					
					while((line = in.readLine()) != null){
						if(!line.equals("EOF")){
							bw.write(line+ System.lineSeparator());
							bw.flush();
						}else{
							break;
						}
						
					}
				}
				System.out.println("Annuary is synchronized");
				
				//On synchronise le DNS
				
				String lineDNS;
				String parseLine[];
				while((lineDNS = in.readLine()) != null){
					if(!lineDNS.equals("EODNS")){
						parseLine = lineDNS.split(" ");
						Network.addDNS(parseLine[0], parseLine[1]);
					}else{
						break;
					}
				}
				System.out.println("DNS synchronized");
				//On synchronise la network table
				String lineNT;
				String parseLineNT[];
				while((lineNT = in.readLine()) != null){
					if(!lineNT.equals("EONT")){
						parseLineNT = lineNT.split(" ");
						Network.addnetworkTable(parseLineNT[0], parseLineNT[1]);
					}else{
						break;
					}
				}
				System.out.println("Network Table synchronized");
			
				//On synchronise la liste des utiisateurs connectes
				String lineUser;
				String parseLineUser[];
				while((lineUser = in.readLine()) != null){
					if(!lineUser.equals("EOREF")){
						parseLineUser = lineUser.split(" ");
						Network.addRef(parseLineUser[0], parseLineUser[1], parseLineUser[2], parseLineUser[3], parseLineUser[4]);
					}else{
						break;
					}
				}
				System.out.println("User referential is synchronized");
				
				//on demarre la reception / transmission entre les deux serveurs
				t2 = new Thread(new ChatClntSrvr(socket,serverName,Network.getServerName(socket.getInetAddress().getHostName())));
				t2.start();
			} catch (IOException e) {	
				System.err.println("error during annuary synchronization");	
			}
		}
}
