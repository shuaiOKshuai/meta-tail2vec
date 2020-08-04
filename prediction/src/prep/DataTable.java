package prep;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import utils.TextReader;
import utils.TextWriter;

public class DataTable {

	private Map<Integer, Set<Attribute>> table;
	private Map<Attribute, Integer> attrIdMap;
	private Map<Integer, Attribute> attrIdReverseMap;
	
	public DataTable() {
		table = new THashMap<Integer, Set<Attribute>>();
	}
	
	
	public void addAttribute(int eid, Attribute f) {
		Set<Attribute> subl = table.get(eid);
		if (subl == null) {
			subl = new THashSet<Attribute>();
			table.put(eid, subl);
		}
		subl.add(f);
	}
	
	public void save(String filename) throws Exception {
		TextWriter out = new TextWriter(filename);
		for (int eid : table.keySet()) {
			Set<Attribute> subl = table.get(eid);
			out.write(eid);
			for (Attribute f : subl) {
				out.write("\t");
				out.write(f.toString());
			}
			out.writeln();
		}
		
		out.close();
	}
	
	public void load(String filename) throws Exception {
		String line;
		TextReader in = new TextReader(filename);
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split("\t");
			int eid = Integer.parseInt(splits[0]);
			for (int i = 1; i < splits.length; i += 2) {
				Attribute f = new Attribute(splits[i], splits[i+1]);
				addAttribute(eid, f);
			}
		}	
		in.close();
	}
	
	public int generateIds(int startingId) throws Exception {
		attrIdMap = new THashMap<Attribute, Integer>();
		attrIdReverseMap = new THashMap<Integer, Attribute>();
		for (Attribute f : getAllAttributes()) {
			attrIdMap.put(f, startingId);
			attrIdReverseMap.put(startingId, f);
			startingId ++;
		}		
		return startingId;
	}
	
	public Set<Attribute> getAllAttributes() {
		Set<Attribute> result = new THashSet<Attribute>();
		for (Set<Attribute> set : table.values())
			result.addAll(set);	
		return Collections.unmodifiableSet(result);
	}
	
	public Set<Integer> getAllEntities() {
		return Collections.unmodifiableSet(table.keySet());
	}
	
	public int getAttributeId(Attribute f) {
		return attrIdMap.get(f);
	}
	
	public Attribute getAttribute(int id) {
		return attrIdReverseMap.get(id);
	}
	
	public Set<Attribute> getEntityAttributes(int eid) {
		return Collections.unmodifiableSet(table.get(eid));
	}
	
	public void printStats() {
		System.out.println("# entities: " + getAllEntities().size());
		System.out.println("# attributes: " + getAllAttributes().size());
	}
	

}
