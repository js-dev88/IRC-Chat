package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Rxmit implements Runnable {
	private  BufferedReader in;
	private static PrintWriter out;
	private static String message = null, serverHost = null;
	private  String nick = null;
	private static Socket socket;
	private static Thread t4, t5;

	public Rxmit(BufferedReader in, String nickCons, String serverHostCons, Socket socketCons,PrintWriter outCons){
		this.in = in;
		this.nick = nickCons;
		serverHost = serverHostCons;
		socket = socketCons;
		out = outCons;
	}
	public void run() {
		
		while(true){
			try {
				message = in.readLine();
				treatMessage(message);
			} catch (IOException e) {
				if(!Network.setDisconnected(nick,socket)){
					Network.unRegisterServer(serverHost);
					System.out.println(serverHost +" has been disconnected\n");
				}else{
					System.out.println(nick +" has been disconnected\n");
				}
				break;
			}
		}
	}
	public String getNick(){
		return this.nick;
	}
	public  void treatMessage(String message){
		String response ="test";
		String args[] = message.split(" ");
		String cmd = null;
		if(args.length > 0){
			cmd = args[0];
		}
		//transmission d'un message a un serveur
		if(args[0].toUpperCase().equals("SERV") && args[1].toUpperCase().equals("PRIVMSG")){
			String userFrom = args[2];
			String userTo = args[3];
			String message2Transmit = args[4];
			System.out.println(message);
			System.out.println(" UserFrom " + userFrom);
			transmitPrivateMessage(userFrom,userTo,message2Transmit);
		}
		if(args.length > 0 && Network.isValidCommand(cmd.toUpperCase())){
			switch(cmd.toUpperCase()){
			case "HELP": response = new Message("000", Network.getNiceCommandRef(),socket).toString();
				break;
			case "TIME" : response = treatTimeRequest(args);
				break;
			case "PRIVMSG" : response = treatPrivateMessage(getNick(),message);
			break;
			default : response = new Message("315", "The server knows the command, but it's not implemented yet",socket).toString();
				break;
			}
		}else{
			response = new ErrorMessage("421",null,cmd,socket).toString();
		}
		if(response != null){
			sendResponse(response);
		}
		
	}
	
	
	
	
	public  String treatPrivateMessage(String nick, String message){
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String separe[] = message.split(":");
		String args[] = separe[0].split(" ");
		String response;
		if(separe.length < 2){
			response = new ErrorMessage("412",null,"PRIVMSG",socket).toString();
			return response;
		}else if(args.length != 2){
			response = new ErrorMessage("411",null,"PRIVMSG",socket).toString();
			return response;
		}else if(!Network.isInAnnuary(args[1])){
			response = new ErrorMessage("401", args[1],"PRIVMSG",socket).toString();
			return response;
		}else{
			if(Network.isConnected(args[1])){
				
				if(Network.serverFromUSerCon(args[1]).equals("/"+serverHost)){
					System.out.println("Socket table :"+Network.returnSocketTale());
					Socket socketDest = Network.getSocketFromTable(args[1]);
					String rep2User = ":"+nick+" PRIVMSG <"+sdf.format(cal.getTime())+">"+separe[1];
					try {
						PrintWriter outDes = new PrintWriter(socketDest.getOutputStream());
						t5 = new Thread(new Txmit(outDes, rep2User));
						t5.start();
					} catch (IOException e) {
						System.err.println("Error during outputstream from one2one");
					}
				}else{
					String relayServer = Network.searchNextServer2Transmit(args[1]);
					String trans2Serv = "SERV PRIVMSG "+nick+" "+args[1]+" "+separe[1];
					Socket socketDest = Network.getSocketFromTable(relayServer);
					try {
						PrintWriter outDes = new PrintWriter(socketDest.getOutputStream());
						t5 = new Thread(new Txmit(outDes, trans2Serv));
						t5.start();
					} catch (IOException e) {
						System.err.println("Error during outputstream from firstServerRelay");
					}
				}
				return null;
			}else{
				response = new Message("391", args[1]+": I Am no connected",socket).toString();
				//TODO enregistrer message dans db local
				return response;
			}
		}
		
	}
	public static void transmitPrivateMessage(String from, String to, String message){
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		if(Network.serverFromUSerCon(to).equals("/"+serverHost)){
			Socket socketDest = Network.getSocketFromTable(to);
			String rep2User = ":"+from+" PRIVMSG <"+sdf.format(cal.getTime())+">"+message;
			try {
				PrintWriter outDes = new PrintWriter(socketDest.getOutputStream());
				t5 = new Thread(new Txmit(outDes, rep2User));
				t5.start();
			} catch (IOException e) {
				System.err.println("Error during outputstream from one2one final");
			}
		}else{
			String relayServer = Network.searchNextServer2Transmit(to);
			String trans2Serv = "SERV PRIVMSG "+from+" "+to+" "+message;
			Socket socketDest = Network.getSocketFromTable(relayServer);
			try {
				PrintWriter outDes = new PrintWriter(socketDest.getOutputStream());
				t5 = new Thread(new Txmit(outDes, trans2Serv));
				t5.start();
			} catch (IOException e) {
				System.err.println("Error during outputstream from serverRealy");
			}
		}
	}
	public static String treatTimeRequest(String[] requestArgs){
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	  
		String answer;
		if(requestArgs.length > 1){
			answer = new Message("391", "The Time : "+sdf.format(cal.getTime()),socket).toString();
		}else{
			 
			answer = new Message("391", "The Time : "+sdf.format(cal.getTime()),socket).toString();  
		}
		
		return answer;
		
	}
	public static void sendResponse(String message){
		t4 = new Thread(new Txmit(out, message));
		t4.start();
	}
}