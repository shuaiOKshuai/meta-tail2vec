package data;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import utils.TextReader;
import utils.TextWriter;


public class Graph {

	protected Map<Integer, Node> nodes;
		
	public int numNodes() {
		return nodes.size();
	}
	
	public void putNode(Node n) throws Exception {
		nodes.put(n.getId(), n);
	}
	
	public Graph() {
		nodes = new THashMap<Integer, Node>();
	}
	
	
	public Node getNode(int id) {
		return nodes.get(id);
	}
	
	public Collection<Node> getNodes() {
		return Collections.unmodifiableCollection(nodes.values());
	}
	
	// make new ids consecutive
	public void reAssignIds() {
		Map<Integer, Node> newNodes = new THashMap<Integer, Node>();
		int autoId = 0;
		List<Integer> nidList = new ArrayList<Integer>();
		nidList.addAll(nodes.keySet());
		Collections.sort(nidList);
		for (int id : nidList) {
			Node n = nodes.get(id);
			n.setId(autoId);
			newNodes.put(autoId, n);
			autoId ++;
		}
		nodes = newNodes;
	}
	
	public void loadFromFile(String nodeFile, String edgeFile) throws Exception {
		// load nodes
		
		TextReader in = new TextReader(nodeFile);
		String line;
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\\t");
			int id = Integer.parseInt(parts[0]);
			String type = parts[1];
			String content = parts[2];
			nodes.put(id, new Node(id, type, content));
		}
		in.close();
		
		// load edges
		in = new TextReader(edgeFile);
		
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\\t");
			Node from = nodes.get(Integer.parseInt(parts[0]));
			Node to = nodes.get(Integer.parseInt(parts[1]));
			
			double weight = 1.0;
			if (parts.length > 2)
				weight = Double.parseDouble(parts[2]);
			from.neighbors.add(new Edge(to, weight));
		}
		in.close();
	}
	
	public void saveToFile(String nodeFile, String edgeFile) throws Exception {
		List<Integer> nidList = new ArrayList<Integer>();
		nidList.addAll(nodes.keySet());
		Collections.sort(nidList);
		
		
		TextWriter out = new TextWriter(nodeFile);
		for (int nid : nidList) { 
			Node n = getNode(nid);
			out.writeln(n.getId() + "\t" + n.getType() + "\t" + n.getContent());
		}
		out.close();
		
		out = new TextWriter(edgeFile);
		for (int nid : nidList) {
			Node n = getNode(nid);
			for (Edge e : n.neighbors) 
				out.writeln(n.getId() + "\t" + e.otherEnd.getId());
		}
		out.close();
	}
	
	public void saveToNode2Vec(String edgeFile) throws Exception {
		TextWriter out = new TextWriter(edgeFile);
		for (Node n : this.getNodes()) {
			for (Edge e : n.neighbors) {
				int nid = n.getId();
				int mid = e.otherEnd.getId();
				if (nid <= mid)
					out.writeln(mid + "\t" + mid);
			}
		}
		out.close();
	}
	
	public void saveToHin2Vec(String edgeFile) throws Exception {
		TextWriter out = new TextWriter(edgeFile);
		out.writeln("#source_node\tsource_class\tdest_node\tdest_class\tedge_class");
		for (Node n : this.getNodes()) {
			for (Edge e : n.neighbors) {
				int nid = n.getId();
				int mid = e.otherEnd.getId();
				String nt = n.getType();
				String mt = e.otherEnd.getType();
				out.writeln(nid + "\t" + nt + "\t" + mid + "\t" + mt + "\t" + (nt + "-" + mt));
			}
		}
		out.close();
	}
	
	public void saveEdges(String type1, String type2, String edgeFile) throws Exception {
		TextWriter out = new TextWriter(edgeFile);
		for (Node n : this.getNodes()) {
			if (!n.getType().equals(type1))
				continue;
			
			for (Edge e : n.neighbors) {
				Node m = e.otherEnd;
				if (!m.getType().equals(type2))
					continue;
				int nid = n.getId();
				int mid = m.getId();
				if (!type1.equals(type2) || nid < mid)
					out.writeln(nid + "\t" + mid);
			}
		}
		out.close();
	}
	
	
}
