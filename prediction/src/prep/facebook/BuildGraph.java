package prep.facebook;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

import data.Graph;
import prep.Attribute;
import prep.DataTable;
import utils.Config;
import utils.TextReader;
import utils.TextWriter;

public class BuildGraph {
	
	private static final int MAX_PEOPLE_ID = 4038;
	
	private static boolean verifyPeopleNodes(Set<Integer> nodes) {
		// verify id starts from 0 and are consecutive
		return nodes.size() == MAX_PEOPLE_ID + 1 &&
			Collections.max(nodes) == MAX_PEOPLE_ID &&
			Collections.min(nodes) == 0;
	}
	
	private static Set<Integer> gatherPeopleNodes() throws Exception {
		Set<Integer> result = new THashSet<Integer>();
		String line;
		TextReader in = new TextReader(Facebook.getFileConnections());
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split(" ");
			result.add(Integer.parseInt(splits[0]));
			result.add(Integer.parseInt(splits[1]));
		}		
		in.close();
		return result;
	}
	
	private static void outputPeopleEdges(TextWriter out) throws Exception {
		String line;
		TextReader in = new TextReader(Facebook.getFileConnections());
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split(" ");
			int a = Integer.parseInt(splits[0]);
			int b = Integer.parseInt(splits[1]);
			out.writeln(a + "\t" + b);
			out.writeln(b + "\t" + a);
		}		
		in.close();
	}
	
	private static void outputAttributeEdges(DataTable table, TextWriter out) throws Exception {
		
		for (int uid : table.getAllEntities()) {
			for (Attribute f : table.getEntityAttributes(uid)) {
				int id = table.getAttributeId(f);
				out.writeln(uid + "\t" + id);
				out.writeln(id + "\t" + uid);
			}
		}		
	}
		
	public static void main(String[] args) throws Exception {

		
		Set<Integer> peopleNodes = gatherPeopleNodes();
		if (!verifyPeopleNodes(peopleNodes))
			throw new Exception("Something wrong with facebook connection file!");
		
		DataTable table = new DataTable();
		table.load(Config.Z_FILE_TABLE);
		int nextId = table.generateIds(MAX_PEOPLE_ID + 1);
		
		TextWriter out = new TextWriter(Config.Z_FILE_GRAPH_NODE);
		for (int id = 0; id <= MAX_PEOPLE_ID; id++) 
			out.writeln(id + "\t" + "user" + "\t0\t0");
		for (int id = MAX_PEOPLE_ID + 1; id < nextId; id++) {
			Attribute f = table.getAttribute(id);
			out.writeln(id + "\t" + f.key + "\t" + f.value + "\t0");
		}
		out.close();
		
		
		out = new TextWriter(Config.Z_FILE_GRAPH_EDGE);
		outputPeopleEdges(out);
		outputAttributeEdges(table, out); 
		out.close();
	}
}
