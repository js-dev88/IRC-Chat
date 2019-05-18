package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * 
 * Connexion côté client gestion des commandes PASS - NICK - USER => cf. Classe identification du serveur
 *
 */
public class Cnnxn implements Runnable {
	private Socket socket = null;
	public static Thread t2;
	public static String nick = null, pass = null, user = null, completeName = null, email = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Scanner sc = null;
	private boolean connect = false;
	

	public Cnnxn(Socket s) {
		socket = s;
	}
	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sc = new Scanner(System.in);
			
			int nbConnMax = 0; // nombre de connexions max autorisées
			while(nbConnMax < 3 ) {
				nbConnMax++;
				String msg[];
				String line;
				//tant que le mot de passe n'est pas rentré de la forme PASS xxxx on recommence l'opération
				do{
					System.out.println(in.readLine());
					pass = sc.nextLine();
					out.println(pass);
					out.flush();
					line = in.readLine();
					msg = line.split(" ");
					System.out.println(line);
				}while(msg[1].equals("421") || msg[1].equals("461"));
				
				//la forme du pass est correcte, on fait de même pour le nick
				do{
					nick = sc.nextLine();
					out.println(nick);
					out.flush();
					line = in.readLine();
					msg = line.split(" ");
					System.out.println(line);
					if(msg[1].equals("421") || msg[1].equals("431") || msg[1].equals("432"))
					  System.out.println(in.readLine());
				}while(msg[1].equals("421") || msg[1].equals("431") || msg[1].equals("432"));
				
				//la forme du nick est correcte, on fait de même pour le user
				do{
					user = sc.nextLine();
					out.println(user);
					out.flush();
					line = in.readLine();
					msg = line.split(" ");
					System.out.println(line);
					if(msg[1].equals("436") || msg[1].equals("001"))break;
					if(msg[1].equals("421") || msg[1].equals("461") || msg[1].equals("598") || msg[1].equals("597"))
						  System.out.println(in.readLine());
				}while(msg[1].equals("421") || msg[1].equals("461") || msg[1].equals("598") || msg[1].equals("597"));
				
				if(msg[1].equals("001")) {
					line = in.readLine();
					System.out.println(line);
					connect = true;
					break;
				}
				else {
					//Si l'erreur vient d'une erreur dans la combinaison nick / pass on lance un delay
					//permet de lutter contre les attaques par dictionnaire / bruteforce
					//3 tentatives permises avant déconnexion
					System.err.println("Number of attempt : " +nbConnMax);
					for(int i = 0; i < 4; i++) {
					    try {
					    	TimeUnit.SECONDS.sleep(1);
					        System.out.println("Next try in : (" +
					                (4-i)+") seconds");
					    } catch(Throwable e) {
					        e.printStackTrace();
					    }
					}
				}
			}
			//Si le client arive à s'authentifier, il peut rentrer et chatter
			if(connect) {
				t2 = new Thread(new ChatClnt(socket));
				t2.start();
			}else{
				System.err.println("Max of attemps reached. Disconnected...");
				sc.close();
			}
			
		} catch (IOException e) {
			System.err.println("Server is not answering ");
		}
	}
	
	
}
