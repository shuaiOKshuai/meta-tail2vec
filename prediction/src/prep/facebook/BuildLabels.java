package prep.facebook;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import utils.Config;
import utils.TextWriter;
import data.Edge;
import data.Graph;
import data.Node;

public class BuildLabels {
	
	
	private static Set<Node> getNeighborAttr(Node n) {
		Set<Node> result = new THashSet<Node>();
		for (Edge e : n.neighbors)
			if (!e.otherEnd.getType().equals(Config.CORE_TYPE_STR))
			result.add(e.otherEnd);
		return result;
	}
	
	private static int computeRank(Node n, Node m, String[] attrTypes) {
		Set<Node> nAttr = getNeighborAttr(n);
		Set<Node> mAttr = getNeighborAttr(m);
		nAttr.retainAll(mAttr);
		int rank = 1;
		for (String t : attrTypes) {
			int count = 0;
			for (Node x : nAttr)
				if (x.getType().equals(t))
					count ++;
			rank *= (count > 0) ? 1 : 0;
		}
		return rank;
	}
	
	private static String formPair(int a, int b) {
		return a + "," + b;
	}
	
	private static void processSimType(Graph g, String simType, String[] attrTypes1, String[] attrTypes2, Map<String, String> labels, Random rnd) {
		for (Node n : g.getNodes()) {
			if (!n.getType().equals(Config.CORE_TYPE_STR))
				continue;
			
			List<Node> mList = new ArrayList<Node>();
			for (Edge e : n.neighbors) {
				Node m = e.otherEnd;
				if (m.getType().equals(Config.CORE_TYPE_STR))
					mList.add(m);
			}
			
			int qid = n.getId();
			for (Node m : mList) {
				int rank1 = computeRank(n, m, attrTypes1);
				int rank2 = computeRank(n, m, attrTypes2);
				int rank = Math.max(rank1, rank2);
				if (rank > 0) {
					String pair1 = formPair(qid, m.getId());
					String pair2 = formPair(m.getId(), qid);
					if (!labels.containsKey(pair1) && !labels.containsKey(pair2)) {
						if (rnd.nextBoolean())
							labels.put(pair1, simType);
						else
							labels.put(pair2, simType);
					}
				}
			}
		}
	}
	
	private static void processNegatives(Graph g, Map<String, String> labels, Random rnd) throws Exception {
		List<Integer> uidList = new ArrayList<Integer>();
		for (Node node : g.getNodes()) {
			if (node.getType().equals(Config.CORE_TYPE_STR))
				uidList.add(node.getId());
		}
		
		int num = labels.size();
		while (labels.size() < num * 11) {
			int u1 = uidList.get(rnd.nextInt(uidList.size()));
			int u2 = uidList.get(rnd.nextInt(uidList.size()));
			String pair1 = formPair(u1, u2);
			String pair2 = formPair(u2, u1);
			if (u1 != u2 && !labels.containsKey(pair1) && !labels.containsKey(pair2)) {
				labels.put(pair1, "none");
			}
		}
	}
	
	private static void addNoise(double rate, Map<String, String> labels, Random rnd) {
		double countClass = 0;
		double countFamily = 0;
		for (String l : labels.values()) {
			if (l.equals("classmate"))
				countClass ++;
			if (l.equals("family"))
				countFamily ++;
		}
		double ratioClass = countClass / (countClass + countFamily);
		
		for (String pair : labels.keySet()) {
			if (rnd.nextDouble() > rate) 
				continue;
				
			String l = labels.get(pair);
			if (l.equals("none")) {
				if (rnd.nextDouble() <= ratioClass)
					labels.put(pair, "classmate");
				else
					labels.put(pair, "family");
			}
			else
				labels.put(pair, "none");
		}
	}
	

	public static void main(String[] args) throws Exception {
		
		Graph g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
	
		System.out.println("Generating labels...");
		Random rnd = new Random(12092111L);
		Map<String, String> labels = new THashMap<String, String>();
		processSimType(g, "classmate", 
				new String[] { "education;concentration;id", "education;school;id" }, 
				new String[] { "education;degree;id", "education;school;id" }, 
				labels, rnd);
		processSimType(g, "family",
				new String[] { "hometown;id", "last_name" },
				new String[] { "location;id", "last_name" },
				labels, rnd);
		processNegatives(g, labels, rnd);
		
		addNoise(0.05, labels, rnd);
		
		TextWriter out = new TextWriter(Config.getCSVLabelsLink());
		out.writeln("User1,User2,Label");
		for (String pair : labels.keySet())
			out.writeln(pair + "," + labels.get(pair));
		out.close();
		
		System.out.println("Done.");
		
	}

}
