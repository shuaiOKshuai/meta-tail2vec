package prep.dblp;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

import utils.Config;
import utils.TextWriter;
import data.Edge;
import data.Graph;
import data.Node;


public class BuildPairs {
	
	private static Graph g;
		
	
	private static boolean isAuthor(int nid) {
		return g.getNode(nid).getType().equals("author");
	}

	private static Set<Integer> getCoAuthors(int qid) {
		Set<Integer> result = new THashSet<Integer>();
		
		for (Edge e : g.getNode(qid).neighbors) 
			for (Edge e2 : g.getNode(e.otherEnd.getId()).neighbors) {
				int mid = e2.otherEnd.getId();
				if (isAuthor(mid) && mid != qid) 
					result.add(mid);
			}
		
		return Collections.unmodifiableSet(result);
	}
	
	/*
	private static Set<Integer> getNeighbors2Hop(int qid) {
		Random rnd = new Random(7831990L + qid * 3);
		Set<Integer> result = new THashSet<Integer>();
		
		for (Edge e : g.getNode(qid).neighbors) {
			int nid = e.otherEnd.getId();
			if (nid == qid)
				continue;
			result.add(nid);
			for (Edge e2 : g.getNode(nid).neighbors) {
				int mid = e2.otherEnd.getId();
				if (mid == qid)
					continue;
				if (rnd.nextDouble() < 0) 
					result.add(mid);
				else if (isAuthor(qid) && isAuthor(mid))
					result.add(mid);
			}
		}
		
		return Collections.unmodifiableSet(result);
	}
	*/

	public static void main(String[] args) throws Exception {
		System.out.println("Loading graph...");
		g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
		Set<Integer> aidSet = new THashSet<Integer>();
		for (Node node : g.getNodes()) {
			if (isAuthor(node.getId()))
				aidSet.add(node.getId());
		}
		
		
		TextWriter out = new TextWriter(Config.FILE_PAIR_DB);
		for (int aid : aidSet) {
			Set<Integer> coauthors = getCoAuthors(aid);
			Set<Integer> coauthorsHop2 = new THashSet<Integer>();
			for (int coauthor : coauthors) 
				coauthorsHop2.addAll(getCoAuthors(coauthor));
			coauthorsHop2.removeAll(coauthors);
			coauthorsHop2.remove(aid);
			
			out.writeln(aid + "\t-1\t0");
			out.writeln("-1\t" + aid + "\t0");
			for (int coauthor: coauthors)
				out.writeln(aid + "\t" + coauthor + "\t1");
			for (int coauthor: coauthorsHop2)
				out.writeln(aid + "\t" + coauthor + "\t2");
			
		}
		
		out.close();
		
	
		System.out.println("Done.");
	}

}
