package server;

import java.net.InetAddress;
import java.net.SocketAddress;

/**
 * 
 *rfc2813 : For each client, all servers MUST have the following information: a
 * netwide unique identifier (whose format depends on the type of
 * client) and the server to which the client is connected.
 *Ici on cherche à stocker un client avec son nick, son adresse ip et l'adresse du serveur auquel il s'est connecté
 *
 */
public class User {
	private String nick;
	private String completeName;
	private String email;
	private String userIp;
	private String userServer;
	
	public User(String nick, String completeName, String email, SocketAddress userIp, InetAddress userServer) {
		this.nick = nick;
		this.completeName = completeName;
		this.email = email;
		this.userIp = userIp.toString();
		this.userServer = userServer.toString();
	}
	
	public User(String nick, String completeName, String email, String userIp, String userServer) {
		this.nick = nick;
		this.completeName = completeName;
		this.email = email;
		this.userIp = userIp;
		this.userServer = userServer;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public void setCompleteName(String completeName) {
		this.completeName = completeName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public void setUserServer(String userServer) {
		this.userServer = userServer;
	}
	
	@Override
	public String toString() {
		
		return this.nick+" "+this.completeName+" "+this.email+" "+this.userIp+" "+this.userServer;
	}

	public String getNick() {
		return nick;
	}

	public String getCompleteName() {
		return completeName;
	}

	public String getEmail() {
		return email;
	}

	public String getUserIp() {
		return userIp;
	}

	public String getUserServer() {
		return userServer;
	}
	
	
	
}
