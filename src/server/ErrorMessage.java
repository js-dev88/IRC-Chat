package server;


import java.net.Socket;


public class ErrorMessage {

	private String digits;
	private String target;
	private String command;
	private Socket socket;
	
	

	public ErrorMessage( String digits, String target, String command, Socket socket) {
		this.digits = digits;
		this.target = target;
		this.command = command;
		this.socket = socket;
	}
	
	 public String getDigits() {
		return digits;
	}

	public String getTarget() {
		return target;
	}
	public Socket getSocket() {
		return socket;
	}

	public String getCommand() {
		return command;
	}
	private String getHost(){
		 return Network.getServerName(socket.getInetAddress().getHostName()); 
	 }
	
	public String getMessage(String digits){
		String mess = "test";
		switch(digits){
			//401 ERR_NOSUCHNICK
		    case "401" : mess = getCommand()+" "+getTarget()+" :No such nick/channel";
			break;
			//411 ERR_NORECIPIENT
		    case "411" : mess = getCommand()+" :No Recipient";
			break;
		    //412 ERR_NOTEXTTOSEND
		    case "412" : mess = getCommand()+" :No text to send";
			break;
		    //421    ERR_UNKNOWNCOMMAND
			case "421" : mess = getCommand()+" :Unknown command";
			break;
			//431    ERR_NONICKNAMEGIVEN
			case "431" : mess = " :No nickname given";
			break;
			//432    ERR_ERRONEUSNICKNAME
			case "432" : mess = getTarget()+" :Erroneous nickname";
			break;
			//433    ERR_NICKNAMEINUSE
			case "433" : mess = getTarget()+" :Nickname is already in use";
			break;
			//436    ERR_NICKCOLLISION
			case "436" : mess = getTarget()+" :Nickname collision KILL from "+getHost();
			break;
			// 461    ERR_NEEDMOREPARAMS
			case "461" : mess = getCommand()+" :Not enough parameters";
			break;
			// 462    ERR_ALREADYREGISTRED
			case "462" : mess = getCommand()+" :Unauthorized command (already registered)";
			break;
			// 598    invalid email
			case "598" : mess = getCommand()+" :Email is Invalid";
			break;
			// 597    name is too long
		    case "597" : mess = getCommand()+" :CompleteName is too long";
			break;
		}
		return mess;
	}
	
	 
	 @Override
	public String toString() {
		return ":"+getHost()+" "+getDigits()+" "+getMessage(getDigits());
	}
	  
}
