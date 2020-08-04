 package prep.dblp;

import gnu.trove.map.hash.THashMap;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


import utils.Config;
import utils.TextReader;
import utils.TextWriter;
import data.Graph;
import data.Node;


public class BuildLabels {
	
	private static Map<String, Integer> idMap; 
	private static Map<Integer, String> gtAuthors;
	private static Graph g;
		
	private static Integer getId(int gtId) {
		String aName = gtAuthors.get(gtId);
		return idMap.get(aName);
	}
	
	private static boolean isAuthor(int nid) {
		return g.getNode(nid).getType().equals("author");
	}
	
	private static String formPair(int a, int b) {
		return a + "," + b;
	}
	
	private static void processAdviseeAdvisor(String filename, Map<String, String> labels) throws Exception {
		Random rand = new Random(91018L);
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split(" ");
			Integer advisee = getId(Integer.parseInt(splits[0]));
			Integer advisor = getId(Integer.parseInt(splits[1]));
			if (advisor == null || advisee == null)
				continue;			
			if (!isAuthor(advisee) || !isAuthor(advisor))
				throw new Exception("Something wrong: advisor or advisee must be an 'author'!");
			
			labels.put(formPair(advisee, advisor), "advisor");
			labels.put(formPair(advisor, advisee), "advisee");
		}
		in.close();
	}
	
	// should only be invoked after Advisor/advisee
	private static void processColleague(String filename, Map<String, String> labels) throws Exception {
		Random rand = new Random(8910018L);
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split("\t");
			Integer a = getId(Integer.parseInt(splits[0]));
			Integer b = getId(Integer.parseInt(splits[1]));
			if (a == null || b == null)
				continue;
			if (!isAuthor(a) || !isAuthor(b))
				throw new Exception("Something wrong: colleague must be an 'author'!");
			
			String pair1 = formPair(a, b);
			String pair2 = formPair(b, a);
			if (!labels.containsKey(pair1) && !labels.containsKey(pair2)) {
				labels.put(pair1, "colleague");
				labels.put(pair2, "colleague");
			}
		}
		in.close();
	}
	
	/*
	private static void processNegatives(Graph g, Map<String, String> labels) throws Exception {
		Random rand = new Random(1089171L);
		List<Integer> aidList = new ArrayList<Integer>();
		for (Node node : g.getNodes()) {
			if (isAuthor(node.getId()))
				aidList.add(node.getId());
		}
		
		int num = labels.size();
		while (labels.size() < num * 2) {
			int a1 = aidList.get(rand.nextInt(aidList.size()));
			int a2 = aidList.get(rand.nextInt(aidList.size()));
			String pair1 = formPair(a1, a2);
			String pair2 = formPair(a2, a1);
			if (a1 != a2 && !labels.containsKey(pair1) && !labels.containsKey(pair2)) {
				labels.put(pair1, "none");
			}
		}
	}
	*/
	
	public static void main(String[] args) throws Exception {
		System.out.println("Loading graph...");
		g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
		idMap = new THashMap<String, Integer>();
		for (Node node : g.getNodes()) {
			if (isAuthor(node.getId()))
				idMap.put(node.getContent(), node.getId());
		}
		gtAuthors = DBLP.loadLabelAuthors();
		
		System.out.println("Generating labels...");
		
		Map<String, String> labels = new THashMap<String, String>();
		processAdviseeAdvisor(DBLP.getFileLabelAI(), labels);
		processAdviseeAdvisor(DBLP.getFileLabelMath(), labels);
		processAdviseeAdvisor(DBLP.getFileLabelTeacher(), labels);
		processAdviseeAdvisor(DBLP.getFileLabelPhd(), labels);
		
		processColleague(DBLP.getFileLabelColleague(), labels);
		
		//processNegatives(g, labels);
				
		TextWriter out = new TextWriter(Config.getCSVLabelsLink());
		out.writeln("Author1,Author2,Label");
		for (String pair : labels.keySet())
			out.writeln(pair + "," + labels.get(pair));
		out.close();
	
		System.out.println("Done.");
	}

}
