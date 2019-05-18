package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;



public class CnnxnSrv implements Runnable {
	private Socket socket = null;
	private static Thread t2;
	private static String pass = null, ipAdress = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private Scanner sc = null;
	private boolean connect = false;
	private String version = "v1.0";
	private String nameOfServer[];
	
	

	public CnnxnSrv(Socket s,Scanner sc,String ipAdresse) {
		this.socket = s;
		this.sc = sc;
		ipAdress = ipAdresse;
	}
	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sc= new Scanner(System.in);
			
			int nbConnMax = 0; // nombre de connexions max autorisées
			while(nbConnMax < 3 ) {
				nbConnMax++;
				String msg[];
				String line;
				//tant que le mot de passe n'est pas rentré de la forme PASS xxxx on recommence l'opération
				do{
					System.out.println(in.readLine());
					//on rajoute la version en dur car elle n'a pas d'interêt pour le projet
					pass = sc.nextLine()+" "+version;
					out.println(pass);
					out.flush();
					line = in.readLine();
					msg = line.split(" ");
					System.out.println(line);
				}while(msg[1].equals("421") || msg[1].equals("461"));
				
				//la forme du pass est correcte, on fait de même pour server
				String servCmd;
				do{
					//on rajoute automatiquement l'adresse ip
					servCmd = sc.nextLine()+" "+ipAdress; 
					nameOfServer = servCmd.split(" ");
					out.println(servCmd);
					out.flush();
					line = in.readLine();
					msg = line.split(" ");
					System.out.println(line);
					
					if(msg[1].equals("421") || msg[1].equals("461") || msg[1].equals("597") || msg[1].equals("462") ){
						if(nbConnMax == 3) break;
						authFail(nbConnMax);
						nbConnMax++;
						System.out.println(in.readLine());
					}
					
					  
				}while(msg[1].equals("421") || msg[1].equals("461") || msg[1].equals("597") || msg[1].equals("462") );
				
				//la demande de conexion est valide
				if(msg[1].equals("002")) {
					connect = true;
					break;
				}
				
			}
			//Si le client arive à s'authentifier, il peut rentrer et chatter
			if(connect) {
				t2 = new Thread(new SynchronizeAnnuaireRecieve(socket,nameOfServer[1],ipAdress));
				t2.start();
			}else{
				System.err.println("Max of attemps reached. Disconnected...");
				sc.close();
				System.exit(0);
			}
			
		} catch (IOException e) {
			System.err.println("Server is not answering ");
		}
	}
	public static void authFail(int nbConnMax){
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
