package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;
/**
 * On suit le protocle d'authentification comme décrit dans la RFC 2812 : 
 * Commandes : PASS - NICK - USER
 * Liberté prise avec le protocole concernant user => on récupère le nom complet et l'adresse mail au lieu de l'host et de l'utilisateur
 * L'objectif est de fusionné l'authentification classique avec le service "nickserv" pour les besoins spécifiques du projet
 */
public class Identification implements Runnable {
	private static Socket socket;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private String nick = null, pass = null, user = null, completeName = null, email = null;
	public boolean authentifier = false;
	public Thread t2, tMc;

	public Identification(Socket s){
		socket = s;
	}
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			
			while(!authentifier) {
				String cmd[];
				//on recupere le mot de passe, on relance tant que la commande n'est pas valide
				do{
					out.println("Inscription => PASS <myPassword> :");
					out.flush();
					pass = in.readLine();
					cmd = pass.split(" ");
				}while(!checkPass(cmd, out));
				pass = cmd[1];
				//on recupere le pseudo,  on relance tant que la commande n'est pas valide
				do{
					out.println("Nickname => NICK <myNickName> :");
					out.flush();
					nick = in.readLine();
					cmd = nick.split(" ");
				}while(!checkNick(cmd, out));
				nick = cmd[1];
				//on recupere les informations user (email et nom complet),  on relance tant que la commande n'est pas valide
				do{
					out.println("User => USER  <myEmailAdress> <myCompleteName>:");
					out.flush();
					user = in.readLine();
					cmd = user.split(" ");
				}while(!checkUser(cmd, out));
				
				if(cmd.length > 3)
					completeName = cmd[2].trim()+" "+cmd[3].trim();
				else
					completeName = cmd[2].trim();
				email = cmd[1].trim();
				ErrorMessage errmess;
				switch(controlUser(nick, pass, completeName, email)){
					case 1 :
						//erreur pendant l'identification (nick collision)
						errmess = new ErrorMessage("436",nick,null,socket);
						out.println(errmess);
						out.flush();
						break;
					default :
						//l'identification ou l'inscription est correcte
						//On enregistre l'utilisateur et son adress ip dans le registre d'utilisateurs connectés
						Network.setConnected(new User(nick, completeName, email,socket.getRemoteSocketAddress(), socket.getLocalAddress()));
						//Reponse positive du server (message d'accueil 001)
						Message mess = new Message("001",socket, nick, completeName);
						out.println(mess.toString());
						out.flush();
						out.println("Type HELP for displaying the commands");
						out.flush();
						System.out.println(nick +" has been connected ");
						//On annonce sur le multicast que l'utilisateur est connecte
						//rfc2813 nick est un combinaison de nick et user du client
						String mcMessAnuaire ="NICK add "+nick+" "+pass+" "+email+" "+completeName+" "+socket.getRemoteSocketAddress()+" "+socket.getLocalAddress().getHostName();
						tMc = new Thread(new MulticastTxmit(socket.getLocalAddress().getHostName(),mcMessAnuaire));
						tMc.start();
						//on sort de la boucle
						authentifier = true;
						break;
				}
				
			}
			//Si tout va bien on connecte le client au serveur pour chatter
			t2 = new Thread(new ChatClntSrvr(socket,nick,socket.getLocalAddress().getHostName()));
			t2.start();
		} catch (IOException e) {
			System.err.println(nick+" is not answering !");
		}
	}
	/**
	 * Si le nick n'existe pas dans l'annuaire on inscrit l'utilisateur
	 * Si le nick existe déjà et est connecté on ne permet pas la connexion
	 * Si le nick existe mais que le mot de passe est faux on indique que l'utilisateur existe déjà
	 * Si le nick existe, correspond au mot de passe et n'est pas connecté on connecte l'utilisateur
	 * Dans ce cas,  si l'email et le nom complet sont différents, ils sont mis à jour
	 */
	private static int controlUser(String nick, String pass, String completeName, String email) {
		//un annuaire par server (RFC 2813 2.Global database) mais théoriquement identiques donc global au réseau
		//ici on prefixe le fichier de l'adresse du serveur pour faciliter le debug
		String serverDatabase = socket.getInetAddress().getHostAddress()+"Annuaire.txt";
		try {
			
			Scanner sc = new Scanner(new File(serverDatabase));
			String toReplace="";
			while(sc.hasNext()) {
				String input = sc.nextLine();
				toReplace += input+ System.lineSeparator();
				String words[] = input.split(" ");
				if(words[0].equals(nick) && !Network.isConnected(nick)) {
					if(words[1].equals(pass)){
						//Si l'utilisateur existe déjà et le mot de passe correspnd, on met à jour le nom complet et l'adresse mail
						try (BufferedWriter bw = new BufferedWriter(new FileWriter(serverDatabase))) {
							String userToREgister = nick+" "+pass+" "+email+" "+completeName;
							while(sc.hasNext()) {
								toReplace += sc.nextLine()+ System.lineSeparator();
							}
							toReplace = toReplace.replace(input, userToREgister);
							bw.write(toReplace);
							System.out.println("User has been recognized");
						} catch (IOException e) {
							System.err.println("Error during update of informations");
						}
						sc.close();
						return 0;
					}else{
						//le pseudo existe déjà ou erreur sur le mot de passe   436    ERR_NICKCOLLISION
						sc.close();
						return 1;
					}	
				}
				if(words[0].equals(nick) && Network.isConnected(nick)) {
					//utilisateur déjà connecté  436    ERR_NICKCOLLISION
					sc.close();
					return 1;
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.err.println("Database doesn't exist!");
		}
		//inscription de l'utilisateur dans l'annuaire
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(serverDatabase,true))) {
			String userToREgister = nick+" "+pass+" "+email+" "+completeName+"\n";
			bw.write(userToREgister);
			System.out.println("User Registered");
		} catch (IOException e) {
			System.err.println("Error during user registration");
		}
		return 3;
	}
	
	/**
	 * 
	 * Si la commande PASS n'est pas présente => 421 
	 * Si le mot de passe est manquant => 461
	 */
	private static boolean checkPass(String[] cmd, PrintWriter out){
		if(!cmd[0].toUpperCase().equals("PASS")){
			out.println(new ErrorMessage("421",null,cmd[0],socket).toString());
			out.flush();
		}else if(cmd.length < 2 || cmd[1].trim() == ""){
			out.println(new ErrorMessage("461",null,"PASS",socket).toString());
			out.flush();
		}else{
			return true;
		}	
		return false;
	}
	/**
	 * 
	 * Si la commande NICK n'est pas présente => 421 
	 * Si le nick est manquant => 431
	 */
	private static boolean checkNick(String[] cmd, PrintWriter out){
		if(!cmd[0].toUpperCase().equals("NICK")){
			out.println(new ErrorMessage("421",null,cmd[0],socket).toString());
			out.flush();
		}else if(cmd.length < 2 || cmd[1].trim() == ""){
			out.println(new ErrorMessage("431",null,"NICK",socket).toString());
			out.flush();
		}else if(cmd.length >= 2){
			//Le nick doit être de longueur 9 maximum avec des caractères compris entre a-z  et [] {} | pour le premier caractere et 0 9 _ - pour les 8 suivants
			//on simplifie la regex pour le nick 2 à 9 caractères autorisés parmis les suivants
		    if(!Pattern.matches("[\\[\\]\\{\\}\\-_a-zA-Z0-9]{2,9}",cmd[1])){
				out.println(new ErrorMessage("432",cmd[1],"NICK",socket).toString());
				out.flush();
		    }else{
		    	return true;
		    }
		}
		return false;
	}
	/**
	 * 
	 * Si la commande USER n'est pas présente => 421 
	 * Si il manque un argument => 461
	 * RFC 2812 Error replies are found in the range from 400 to 599.
	 * => on définit 598 comme erreur dans le pattern de l'adresse
	 * => on définit 597 si les arguments sont trop longs
	 */
	private static boolean checkUser(String[] cmd, PrintWriter out){
		if(!cmd[0].toUpperCase().equals("USER")){
			out.println(new ErrorMessage("421",null,cmd[0],socket).toString());
			out.flush();
		}else if(cmd.length < 3 || cmd[1].trim() == "" || cmd[2].trim() == ""){
			out.println(new ErrorMessage("461",null,"USER",socket).toString());
			out.flush();
		}else if(!Pattern.matches("[-._@a-zA-Z0-9]{2,30}",cmd[1])){
			out.println(new ErrorMessage("598",null,"USER",socket).toString());
			out.flush();
		}else if(cmd.length > 3 && (cmd[2].length() > 15 || cmd[3].length() > 15 )){
			out.println(new ErrorMessage("597",null,"USER",socket).toString());
			out.flush();
		}else{
			return true;
		}	
		return false;
	}
}