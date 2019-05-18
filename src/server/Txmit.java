package server;

import java.io.PrintWriter;

public class Txmit implements Runnable {
	private PrintWriter out;
	private String message = null;
	
	public Txmit(PrintWriter out, String message) {
		this.out = out;
		this.message = message;
	}
	public void run() {
				System.out.println(message);
				out.println(message);
				out.flush();
	}
}