package client;

import java.io.PrintWriter;
import java.util.Scanner;

public class Txmit implements Runnable {
	private PrintWriter out;
	private String msg = null;
	private Scanner sc = null;

	public Txmit(PrintWriter out) {
		this.out = out;
	}
	public void run() {
		sc = new Scanner(System.in);
		while(true) {
			msg = sc.nextLine();
			out.println(msg);
			out.flush();
		}
	}
}
