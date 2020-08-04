package prep.bib;


import java.util.Set;

import data.Edge;
import data.Graph;
import data.Node;
import gnu.trove.set.hash.THashSet;
import utils.Config;
import utils.TextReader;
import utils.TextWriter;


public class BuildGraph {
	
	private static String getConf(Node p) {
		for (Edge e : p.neighbors)
			if (e.otherEnd.getType().equals("conf"))
				return e.otherEnd.getContent();
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		// get paper nodes in the selected conferences
		System.out.println("Loading full graph...");
		Graph g = new Graph(); 
		Set<Integer> nSubset = new THashSet<Integer>();
		
		g.loadFromFile(Bib.getFileNode(), Bib.getFileEdge());
		for (Node n : g.getNodes()) {
			if (n.getType().equals("paper") && Bib.confSet.contains(getConf(n)))
				nSubset.add(n.getId());
			if (n.getType().equals("author") && n.neighbors.size() >= 5)
				nSubset.add(n.getId());
			if (n.getType().equals("term") && n.neighbors.size() >= 5)
				nSubset.add(n.getId());
			if (n.getType().equals("conf") && Bib.confSet.contains(n.getContent()))
				nSubset.add(n.getId());
		}
		
				
		// filter edge file and gather node id
		System.out.println("Filtering edge file...");
		TextReader in = new TextReader(Bib.getFileEdge());
		TextWriter out = new TextWriter(Bib.getFileEdgeSub());
		String line;
		Set<Integer> nSet = new THashSet<Integer>();
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\\t");
			int from = Integer.parseInt(parts[0]);
			int to = Integer.parseInt(parts[1]);
			
			if (nSubset.contains(from) && nSubset.contains(to)) {
				out.writeln(line);
				nSet.add(from);
				nSet.add(to);
			}
		}
		in.close();
		out.close();
			
		// filter node file
		System.out.println("Filtering node file...");
		in = new TextReader(Bib.getFileNode());
		out = new TextWriter(Bib.getFileNodeSub());
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\\t");
			int id = Integer.parseInt(parts[0]);
			if (nSet.contains(id))
				out.writeln(line);
		}
		in.close();
		out.close();		
				

		System.out.println("loading filtered graph...");
		g = new Graph(); 
		g.loadFromFile(Bib.getFileNodeSub(), Bib.getFileEdgeSub());
		g.reAssignIds();
		g.saveToFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);		
		g.saveToNode2Vec(Config.Z_FILE_GRAPH_EDGE + ".node2vec");
		g.saveToHin2Vec(Config.Z_FILE_GRAPH_EDGE + ".hin2vec");
	}
}
