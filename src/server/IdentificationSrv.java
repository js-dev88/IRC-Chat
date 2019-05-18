package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class IdentificationSrv implements Runnable {
	private static Socket socket;
	private PrintWriter out = null;
	private BufferedReader in = null;
	public boolean authentifier = false;
	private String ipAdress = null, pass = null, version = null, serverName = null;
	public Thread t2;
	
	
	public IdentificationSrv(Socket s){
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
				version = cmd[2];
				//on recupere le pseudo,  on relance tant que la commande n'est pas valide
				String serverCmd;
				do{
					//le servername sert de token, on récupere automatiquement l'adresse ip du serveur
					out.println("Server => SERVER  <serverName> :");
					out.flush();
					serverCmd = in.readLine();
					cmd = serverCmd.split(" ");
				}while(!checkServ(cmd, out));
				ipAdress = cmd[2];
				serverName = cmd[1];
				//On enregistre le server sur le reseau
				Network.registerServer(ipAdress, serverName);
				//Reponse positive du server (message d'accueil 001)
				Message mess = new Message("002",socket, null, version);
				out.println(mess.toString());
				out.flush();
				System.out.println(serverName +" has been connected ");
				//on sort de la boucle
				authentifier = true;
			}
			//La connection est effecive, on synchronise les annuaires, le nouveau server ayant un annuaire vide
			t2 = new Thread(new SynchronizeAnnuaireSend(socket, serverName));
			t2.start();
		} catch (IOException e) {
			System.err.println(serverName+" is not answering !");
		}
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
		}else if(cmd.length < 3 || cmd[1].trim() == ""){
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
	private static boolean checkServ(String[] cmd, PrintWriter out){
		if(!cmd[0].toUpperCase().equals("SERVER")){
			out.println(new ErrorMessage("421",null,cmd[0],socket).toString());
			out.flush();
		}else if(cmd.length < 3 || cmd[1].trim() == ""){
			out.println(new ErrorMessage("461",null,"SERVER",socket).toString());
			out.flush();
		}else if(Network.isAlreadyRegistered(cmd[1])){
			out.println(new ErrorMessage("462",null,"SERVER",socket).toString());
			out.flush();
		}else if(cmd[1].length() > 63){
			 //le server doit avoir un nom d'une longueur < 63 caracteres rfc2812
			out.println(new ErrorMessage("597",cmd[2],"SERVER",socket).toString());
			out.flush();
		}else{
			return true;
		}
		return false;
	}
	
}
