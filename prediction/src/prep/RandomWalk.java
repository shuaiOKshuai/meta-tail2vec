package prep;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import utils.Config;
import utils.Progress;
import utils.TextWriter;
import data.Edge;
import data.Graph;
import data.Node;


public class RandomWalk {
	
	private static Graph g;
	private static Random rnd = new Random(27839901L);
	
	private static boolean isCoreNode(Node n) {
		return n.getType().equals(Config.CORE_TYPE_STR);
	}
	
	private static void addToMap(Map<String, Integer> pairs, String pair) {
		int f = 0;
		if (pairs.containsKey(pair))
			f = pairs.get(pair);
		pairs.put(pair, f + 1);
	}
	
	private static void sample(Node n, int l, int gamma, Map<String, Integer> pairs) {
		int[] walk = new int[l + 1];
		walk[0] = n.getId();
		
		int cur = 1;
		while (cur < walk.length) {
			List<Edge> nb = n.neighbors;
			n = nb.get(rnd.nextInt(nb.size())).otherEnd;
			if (isCoreNode(n)) {
				walk[cur] = n.getId();
				cur ++;
			}
		}
		
		for (int i = 0; i < walk.length; i++) {
			// first order
			addToMap(pairs, walk[i] + "\t-1");
			if (!Config.SYMMETRY)
				addToMap(pairs, "-1\t" + walk[i]);
			
			// second order			
			for (int w = - gamma; w <= gamma; w++) { 
				int j = i + w;
				if (j >= 0 && j < walk.length && j != i) 
					if (!Config.SYMMETRY || walk[i] >= walk[j])
						addToMap(pairs, walk[i] + "\t" + walk[j]);
					else
						addToMap(pairs, walk[j] + "\t" + walk[i]);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		int k = Integer.parseInt(args[0]);
		int l = Integer.parseInt(args[1]);
		int gamma = Integer.parseInt(args[2]);
		
		System.out.println("Loading graph...");
		g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
		
		Map<String, Integer> pairs = new THashMap<String, Integer>();
		
		//System.out.println("Adding node...");
		//for (Node n : g.getNodes()) {
		//	int nid = n.getId();
		//	pairs.add(nid + "\t-1");
		//	if (!Config.SYMMETRY)
		//		pairs.add("-1\t" + nid);
		//}
		
		
		int total = 0;
		for (Node n : g.getNodes()) 
			if (isCoreNode(n))
				total ++;
			
		Progress prog = new Progress ("Sampling", total);
		for (Node n : g.getNodes()) {
			if (isCoreNode(n)) {			
				for (int i = 0; i < k; i++)
					sample(n, l, gamma, pairs);
				prog.tick();
			}			
		}
		prog.done();
		
		System.out.println("Saving...");
		TextWriter out = new TextWriter(Config.FILE_PAIR_DB);
		for (String s : pairs.keySet())
			out.writeln(s + "\t" + pairs.get(s));
		out.close();
		
		System.out.println("Done.");
	}

}
