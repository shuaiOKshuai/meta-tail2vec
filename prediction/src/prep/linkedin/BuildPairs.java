package prep.linkedin;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

import utils.Config;
import utils.TextWriter;
import data.Edge;
import data.Graph;
import data.Node;


public class BuildPairs {
	
	private static Graph g;
		
	/*
	private static boolean isUser(int nid) {
		return g.getNode(nid).getType().equals("user");
	}
	 */
	
	
	private static Set<Integer> getNeighbors2Hop(int qid) {
		Random rnd = new Random(7831990L + qid * 3);
		Set<Integer> result = new THashSet<Integer>();
		
		//if (!isUser(qid))
		//	return Collections.unmodifiableSet(result);
		
		for (Edge e : g.getNode(qid).neighbors) {
			int nid = e.otherEnd.getId();
			if (nid == qid) // || !isUser(nid))
				continue;
			result.add(nid);
			for (Edge e2 : g.getNode(nid).neighbors) {
				if (rnd.nextDouble() >= 0.2)
					continue;
				int mid = e2.otherEnd.getId();
				if (mid == qid) // || !isUser(mid))
					continue;
				result.add(mid);
			}
		}
		
		return Collections.unmodifiableSet(result);
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Loading graph...");
		g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
			
		
		TextWriter out = new TextWriter(Config.FILE_PAIR_DB);
		for (Node n : g.getNodes()) {
			int nid = n.getId();
			//if (!isUser(nid))
			//	continue;
			out.writeln(nid + "\t-1");
			out.writeln("-1\t" + nid);
			for (int mid: getNeighbors2Hop(nid))
				out.writeln(nid + "\t" + mid);
		}
		
		out.close();
		
	
		System.out.println("Done.");
	}

}
