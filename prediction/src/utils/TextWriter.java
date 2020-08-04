package utils;

import java.io.*;

public class TextWriter {
	private BufferedWriter out;
	
	public TextWriter(String filename) throws Exception {
		out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
	}
	
	public TextWriter(String filename, boolean append) throws Exception {
		out =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append)));
	}
	
	public void write(int i) throws Exception {
		out.write(i + "");
	}
	
	public void write(double d) throws Exception {
		out.write(d + "");
	}
	
	public void writeln(int i) throws Exception {
		write(i);
		writeln();
	}
	
	public void writeln(double d) throws Exception {
		write(d);
		writeln();
	}
	
	public void write(String s) throws Exception {
		out.write(s);
	}
	
	public void writeln(String s) throws Exception {
		write(s);
		writeln();
	}

	public void writeln() throws Exception {
		out.write('\n');
	}
	
	public void close() throws Exception {
		out.close();
	}
	
	public void flush() throws Exception {
		out.flush();
	}
}
