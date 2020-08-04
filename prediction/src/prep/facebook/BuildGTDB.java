package prep.facebook;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import utils.Config;
import utils.TextWriter;
import data.Edge;
import data.GroundTruth;
import data.Graph;
import data.Node;

public class BuildGTDB {
	
	private static final String USER = "user";
	
	private static Set<Node> getNeighborAttr(Node n) {
		Set<Node> result = new THashSet<Node>();
		for (Edge e : n.neighbors)
			if (!e.otherEnd.getType().equals(USER))
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
	
	private static void processSimType(Graph g, GroundTruth gtdb, String simType, String[] attrTypes1, String[] attrTypes2) {
		for (Node n : g.getNodes()) {
			if (!n.getType().equals(USER))
				continue;
			
			List<Node> mList = new ArrayList<Node>();
			for (Edge e : n.neighbors) {
				Node m = e.otherEnd;
				if (m.getType().equals(USER))
					mList.add(m);
			}
			
			int qid = n.getId();
			int count = 0;
			for (Node m : mList) {
				int rank1 = computeRank(n, m, attrTypes1);
				int rank2 = computeRank(n, m, attrTypes2);
				int rank = Math.max(rank1, rank2);
				
				if (rank > 0 || count < 5) {
					gtdb.add(simType, qid, m.getId(), rank);
					if (rank == 0) 
						count ++;
				}
				
				/*
				if (rank > 0) {
					gtdb.add(simType, qid, m.getId(), rank);
					String simTypeNeg;
					if (simType.equals("classmate"))
						simTypeNeg = "family";
					else
						simTypeNeg = "classmate";
					gtdb.add(simTypeNeg, qid, m.getId(), 0);
				}
				*/
			}
		}
	}
	
	
	private static void processOthers(Graph g, GroundTruth gtdb) {
		for (Node q : g.getNodes()) {
			if (!q.getType().equals(USER))
				continue;
			
			int qid = q.getId();
			Set<Integer> allCands = new THashSet<Integer>();
			Set<Integer> assigned = new THashSet<Integer>();
			for (String simType : new String[] { "classmate", "family"}) {
				for (int nid : gtdb.getCandidateNodes(simType, qid)) {
					allCands.add(nid);
					if (gtdb.getRank(simType, qid, nid) == 1)
						assigned.add(nid);
				}
			}
			for (int nid : allCands)
				if (assigned.contains(nid))
					gtdb.add("other", qid, nid, 0);
				else
					gtdb.add("other", qid, nid, 1);
		}
	}
	

	public static void main(String[] args) throws Exception {
		
		Graph g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
	
		System.out.println("Generating groundtruth...");
		GroundTruth gtdb = new GroundTruth();
		
		
		processSimType(g, gtdb, "classmate", 
				new String[] { "education;concentration;id", "education;school;id" }, 
				new String[] { "education;degree;id", "education;school;id" });
		processSimType(g, gtdb, "family",
				new String[] { "hometown;id", "last_name" },
				new String[] { "location;id", "last_name" });
		processOthers(g, gtdb);
		
		gtdb.addNoise(0.1);
		
		
		
		System.out.println("Generating training/testing splits for link prediction...");
		
		TextWriter out = new TextWriter(Config.getCSVLabelsLink());
		out.writeln("NodeX,NodeY,LinkType");
		
		for (String simType : gtdb.getSimTypes()) {
			for (int qid :  gtdb.getAllQueries(simType)) {
				for (int nid : gtdb.getCandidateNodes(simType, qid)) {
					if (gtdb.getRank(simType, qid, nid) > 0) {
						out.writeln(qid + "," + nid + "," + simType);
					}
				}
			}
		}		
		out.close();
		
		
		gtdb.filter(2);
		
		System.out.println("Generating training/testing splits...");
		for (String simType : new String[] { "classmate", "family"}) {
			for (int split = 1; split <= 10; split++) {
				gtdb.randomSplit(split);
				gtdb.saveTraining(Config.getFileTrain(simType, split), simType, 1000); 
				gtdb.saveTesting(Config.getFileTest(simType, split), simType);
			}
			gtdb.saveIdealRanking(Config.getFileIdeal(simType), simType);
		}
		
		
		gtdb.printStats();
		gtdb.save(Config.FILE_GROUNDTRUTH_DB);
		
		System.out.println("Done.");
		
	}

}
