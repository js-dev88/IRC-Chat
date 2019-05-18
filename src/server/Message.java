package server;
import java.net.Socket;


public class Message {
	private String nick;
	private Socket socket;
	private String digits;
	private String completeName;
	private String message;
	
	public Message(String digits, Socket s, String nick, String completeName) {
		this.digits = digits;
		this.socket = s;
		this.nick = nick;
		this.completeName = completeName;
		
	}
	
	public Message(String digits, String message, Socket socket) {
		this.digits = digits;
		this.message = message;
		this.socket = socket;
		
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getOriginalMessage(){
		return this.message;
	}

	public String getDigits() {
			return digits;
	}
	 
	public String getNick() {
		return nick;
	}

	public Socket getSocket() {
		return socket;
	}
	
	public String getCompleteName() {
		return completeName;
	}
    
	public String getClientHost(){
		return getSocket().getLocalAddress().getHostName();
	}
	
	private String getHost(){
		 return Network.getServerName(getSocket().getLocalAddress().getHostName()); 
	 }
	
	
	public String getMessage(){
		String mess = null;
		switch(digits){
		//RPL_HELP : 
		case "000" : mess = message;
		break;
		// RPL_WELCOME
		case "001" : mess = "Welcome to the Internet Relay Network "+getNick()+"!"+getCompleteName()+"@"+ getClientHost();
		break;
		// RPL_WELCOME
		case "002" : mess = "Your host is "+getHost()+", running version "+getCompleteName();
		break;
		// RPL_AWAY
		case "301" : mess = message;
		break;
		//le numero 396 des command response est libre, on l"utilise pour notre première communication sur le canal muticast
		case "396" : mess = ":"+getHost()+" available";
		break;
		//Réponse par défaut
		case "315" : mess = message;
		break;
		//RPL_TIME
		case "391" : mess = message;
		break;
		

	}
		return mess;
	}
	
	 @Override
	public String toString() {
			return ":"+getHost()+" "+getDigits()+" "+getMessage();
	}
	
	
}
