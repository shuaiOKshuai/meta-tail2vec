package prep.linkedin;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

import data.Graph;
import prep.DataTable;
import prep.bib.Bib;
import prep.Attribute;
import utils.Config;
import utils.TextReader;
import utils.TextWriter;

public class BuildGraph {
	
	private static final int MAX_PEOPLE_ID = 29039;
	
	private static boolean verifyPeopleNodes(Set<Integer> nodes) {
		// verify id starts from 0 and are consecutive
		return nodes.size() == MAX_PEOPLE_ID + 1 &&
			Collections.max(nodes) == MAX_PEOPLE_ID &&
			Collections.min(nodes) == 0;
	}
	
	private static Set<Integer> gatherPeopleNodes() throws Exception {
		Set<Integer> result = new THashSet<Integer>();
		String line;
		TextReader in = new TextReader(LinkedIn.getFileConnections());
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split("\t");
			result.add(LinkedIn.parseUserId(splits[1]));
			result.add(LinkedIn.parseUserId(splits[2]));
		}		
		in.close();
		return result;
	}
	
	private static void outputPeopleEdges(TextWriter out) throws Exception {
		String line;
		TextReader in = new TextReader(LinkedIn.getFileConnections());
		Set<String> seen = new THashSet<String>();
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split("\t");
			int a = LinkedIn.parseUserId(splits[1]);
			int b = LinkedIn.parseUserId(splits[2]);
			String edge = a + "\t" + b;
			if (!seen.contains(edge)) {  // there may be repeated edges (in different ego network) in the raw file
				out.writeln(edge);
				seen.add(edge);
			}
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
			throw new Exception("Something wrong with linkedin connection file!");
		
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
