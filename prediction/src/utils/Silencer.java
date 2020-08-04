package utils;

import java.io.PrintStream;

public class Silencer {
	
	private static PrintStream stderr;
	private static PrintStream nps;
	
	static {
		// setup null PrintStream / backup original for redirect
		nps = new java.io.PrintStream(new java.io.OutputStream() { public void write(int b) {} } );
		stderr = new PrintStream(System.err);
	}
	
	public static void silence() {
		System.setErr(nps);
		//System.err.println("this should not show!");  
	}
	
	public static void unsilence() {
		System.setErr(stderr);
		//System.err.println("this should show!");
	}
	
}
