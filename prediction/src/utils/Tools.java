package utils;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Tools {
	
	// return time taken
	public static long startProcessAndWaitFor(ProcessBuilder pb, String workingDir, boolean suppressOutput) throws Exception {
		if (workingDir != null && !workingDir.isEmpty())
			pb.directory(new File(workingDir));
		pb.redirectInput(Redirect.INHERIT);
	    pb.redirectError(Redirect.INHERIT);
	    if (!suppressOutput)
	    	pb.redirectOutput(Redirect.INHERIT);
	    else {
	    	File tmp = File.createTempFile("suppressed", ".tmp");
	    	pb.redirectOutput(tmp);
	    	tmp.deleteOnExit();
	    }
		long start = System.currentTimeMillis();
		
		final Process process = pb.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() { public void run() { process.destroy(); } }	));		
		process.waitFor();
		
		return System.currentTimeMillis() - start;
	}
	
	
	public static double square(double x) {
		return x * x;
	}
	
	public static <T> void incMap(Map<T, Double> map, T key, double delta) {
		Double d = map.get(key);
		if (d == null)
			d = 0d;
		map.put(key, d + delta);
	}
	
	public static <T> void incMapAll(Map<T, Double> map, double delta) {
		for (T key : map.keySet())
			map.put(key, map.get(key) + delta);
	}
	
	public static <T> void scaleMap(Map<T, Double> map, T key, double factor) {
		Double d = map.get(key);
		if (d == null)
			d = 0d;
		map.put(key, d * factor);
	}
	
	public static <T> void scaleMapAll(Map<T, Double> map, double factor) {
		for (T key : map.keySet())
			map.put(key, map.get(key) * factor);
	}
	
	public static String genArg(String left, String right) {
		//return '"' + left + "=" + right + '"';
		return left + "=" + right;
	}
	
	public static String genArg(String left, int right) {
		return genArg(left, right + "");
	}

	public static void createDirs(String dir) {
		File f = new File(dir);
		if (!f.exists())
			f.mkdirs();
	}
	
	public static double relu(double x) {
		if (x > 0)
			return x;
		else
			return 0;
	}
	
	public static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}
	
	public static double invSigmoid(double x) {
		return - Math.log(1.0 / x  - 1.0);
	}
	
	public static double logSigmoid(double x) {
		return - Math.log(1.0 + Math.exp(-x));
	}

	
	public static boolean fileExists(String file) {
		File f = new File(file);
		return f.exists();
	}
	
	public static double div(double x, double y) {
		if (y == 0)
			return 0;
		else
			return x / y;
	}
	
	public static double getHMean(double p, double r) {
		if (p == 0 || r == 0)
			return 0;
		else
			return 2 * p * r / (p + r);
	}
	
	
	public static void PrintCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
		Date now = new Date();
		String time = sdf.format(now);
		System.out.println("Current time is " + time);
	}
}
