package prep.linkedin;

import java.util.Map;
import java.util.Random;

import gnu.trove.map.hash.THashMap;
import utils.Config;
import utils.TextReader;
import utils.TextWriter;



public class BuildLabels {
	
	private static String formPair(int a, int b) {
		return a + "," + b;
	}
	
	private static void process(Map<String, String> labels) throws Exception {
		//Random rand = new Random(691018L);
		TextReader in = new TextReader(LinkedIn.getFileLabel());
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split("\t");
			int a = LinkedIn.parseUserId(splits[0]);
			int b = LinkedIn.parseUserId(splits[1]);
			String type = splits[2];
			if (type.equals("COWORKER"))
				type = "COLLEAGUE";
			
			labels.put(formPair(a,b), type);
			/*
			String pair1 = formPair(a, b);
			String pair2 = formPair(b, a);
			if (!labels.containsKey(pair1) && !labels.containsKey(pair2)) {
				if (rand.nextBoolean())
					labels.put(pair1, type);
				else
					labels.put(pair2, type);			
			} else {
				if (labels.containsKey(pair1))
					System.out.print(pair1 + " " + labels.get(pair1));
				else
					System.out.print(pair2 + " " + labels.get(pair2));
				System.out.println(" " + type);
			}
			*/
			
		}
		in.close();
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Generating labels...");
		Map<String, String> labels = new THashMap<String, String>();
		process(labels);

		TextWriter out = new TextWriter(Config.getCSVLabelsLink());
		out.writeln("User1,User2,Label");
		for (String pair : labels.keySet())
			out.writeln(pair + "," + labels.get(pair));
		out.close();
	
		System.out.println("Done.");
	}

}
