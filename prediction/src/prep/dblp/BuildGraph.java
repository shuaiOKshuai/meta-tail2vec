package prep.dblp;

import java.util.Collections;
import java.util.Set;

import prep.Attribute;
import prep.DataTable;
import utils.Config;
import utils.TextWriter;


public class BuildGraph {
	
	private static final int MAX_PAPER_ID = 88538;
	
	private static boolean verifyPeopleNodes(Set<Integer> nodes) {
		// verify id starts from 0 and are consecutive
		return nodes.size() == MAX_PAPER_ID + 1 &&
			Collections.max(nodes) == MAX_PAPER_ID &&
			Collections.min(nodes) == 0;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("loading table...");
		DataTable table = new DataTable();
		table.load(Config.Z_FILE_TABLE);
		
		if (!verifyPeopleNodes(table.getAllEntities()))
			throw new Exception("Something wrong with the paper IDs!");
		
		int nextId = table.generateIds(MAX_PAPER_ID + 1);
		
		System.out.println("generating graph nodes...");
		TextWriter out = new TextWriter(Config.Z_FILE_GRAPH_NODE);
		for (int id = 0; id <= MAX_PAPER_ID; id++) 
			out.writeln(id + "\t" + "paper" + "\t0\t0");
		for (int id = MAX_PAPER_ID + 1; id < nextId; id++) {
			Attribute f = table.getAttribute(id);
			out.writeln(id + "\t" + f.key + "\t" + f.value + "\t0"); 
				// need to put f.value, ie, original user/author ID
				// needed when generating gtdb
		}
		out.close();
		
		System.out.println("generating graph edges...");
		out = new TextWriter(Config.Z_FILE_GRAPH_EDGE);
		for (int eid : table.getAllEntities()) {
			for (Attribute f : table.getEntityAttributes(eid)) {
				int id = table.getAttributeId(f);
				out.writeln(eid + "\t" + id);
				out.writeln(id + "\t" + eid);
			}
		}		
		out.close();
		
		System.out.println("generating graph edges for node2vec...");
		out = new TextWriter(Config.Z_FILE_GRAPH_EDGE + ".node2vec");
		for (int eid : table.getAllEntities()) {
			for (Attribute f : table.getEntityAttributes(eid)) {
				int id = table.getAttributeId(f);
				out.writeln(id + " " + eid);
			}
		}		
		out.close();
		
		System.out.println("generating graph edges for hin2vec...");
		out = new TextWriter(Config.Z_FILE_GRAPH_EDGE + ".hin2vec");
		out.writeln("#source_node	source_class	dest_node	dest_class	edge_class");
		for (int eid : table.getAllEntities()) {
			for (Attribute f : table.getEntityAttributes(eid)) {
				int id = table.getAttributeId(f);
				out.writeln(id + "\t" + f.key + "\t" + eid + "\tpaper\t" + (f.key + "-paper"));
				out.writeln(eid + "\tpaper\t" + id + "\t" + f.key + "\t" + ("paper-" + f.key));
			}
		}		
		out.close();
		
		System.out.println("done.");
		
	}
}
