package server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * 
 * Le multicast est géré à partir d'une adrsse de classe D, comprise entre  224.0.0.0 	et 239.255.255.255
 * On utilisera ce canal pour mettre à jour les annuaires, prévenir de la connexion d'un nouveau serveur
 * et faire passer une information à l'ensemble des serveurs (messages one2one par exemple)
 * Source : https://docs.oracle.com/javase/7/docs/api/java/net/MulticastSocket.html
 * Code Source https://www.infres.telecom-paristech.fr/people/hudry/coursJava/reseau/multicast.html
 */
public class MulticastTxmit implements Runnable{
	 private InetAddress  groupeIP;
	 private int port;
	 private MulticastSocket socketEmission;
	 private String name;
	 private String msg;
	
   
	public MulticastTxmit(String nom, String msg){
		try {
			this.groupeIP = InetAddress.getByName("228.6.1.9");
			this.port = 33334;
			this.socketEmission = new MulticastSocket();
			this.name = nom;
			this.msg = ":"+name+" "+msg;
			socketEmission.setTimeToLive(15); 
			socketEmission.joinGroup(groupeIP);
		} catch ( IOException e) {
			System.err.println("Error during multicast transmission construction");
		}
		
	}
	public void run() {
		
					  try {
						transmit(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
		      
		  
	}
	void transmit(String msg) throws Exception {
		byte[] contenuMessage;
		DatagramPacket DTGmessage;
		ByteArrayOutputStream sortie = new ByteArrayOutputStream(); 
		
		//encode en UTF8 modifié
		(new DataOutputStream(sortie)).writeUTF(msg); 
		contenuMessage = sortie.toByteArray();
		DTGmessage = new DatagramPacket(contenuMessage, contenuMessage.length, groupeIP, port);
		socketEmission.send(DTGmessage);
		socketEmission.close();
  }
}
