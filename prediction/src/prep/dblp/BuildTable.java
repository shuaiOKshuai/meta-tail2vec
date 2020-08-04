package prep.dblp;



import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Map;
import java.util.Set;

import prep.Attribute;
import prep.DataTable;
import utils.Config;
import utils.TextReader;

public class BuildTable {
	
	public static boolean isWantedPaper(String[] parts, Map<Integer, String> authors, Set<String> gtAuthors) {
		boolean found = false;
		for (int i = 3; i < parts.length; i++) {
			String aName = authors.get(Integer.parseInt(parts[i]));
			if (gtAuthors.contains(aName)) {
				found = true;
				break;
			}
		}
					
		return found;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("loading authors...");
		Map<Integer, String> authors = new THashMap<Integer, String>();
		TextReader in = new TextReader(DBLP.getFileAuthor());
		String line;
		int aid = 1;
		while ( (line = in.readln()) != null ) {
			String aName = line.split("\t")[0].toUpperCase();
			authors.put(aid, aName);
			aid ++;
		}
		in.close();

		Set<String> gtAuthors = new THashSet<String>(DBLP.loadLabelAuthors().values());
		
		System.out.println("loading term lists...");
		Map<Integer, String> terms = new THashMap<Integer, String>();
		in = new TextReader(DBLP.getFileTerm());
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\t");
			if (parts.length >= 2)
				terms.put(Integer.parseInt(parts[0]), parts[1]);
		}
		in.close();
		
		System.out.println("loading conferences...");
		Map<Integer, String> confs = new THashMap<Integer, String>();
		in = new TextReader(DBLP.getFileConf());
		int cid = 1;
		while ( (line = in.readln()) != null ) {
			confs.put(cid, line.split("\t")[0]);
			cid ++;
		}
		in.close();
		
		
		
		// construct table
		System.out.println("Constructing table...");
		
		System.out.println("Constructing paper-conf-author...");
		DataTable table = new DataTable();		
		in = new TextReader(DBLP.getFilePaperEvent());
		int autoId = 0; // To reassign paper ID (must start from 0 and be consecutive)
		Map<Integer, Integer> idMap = new THashMap<Integer, Integer>();
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\t");
			if (!isWantedPaper(parts, authors, gtAuthors))
				continue;
			int pid = Integer.parseInt(parts[0]);
			idMap.put(pid, autoId);
			String conf = parts[1].isEmpty() ? null : confs.get(Integer.parseInt(parts[1]));
			if (conf != null)
				table.addAttribute(autoId, new Attribute("conf", conf));
			for (int i = 3; i < parts.length; i++) {
				String aName = authors.get(Integer.parseInt(parts[i]));
				table.addAttribute(autoId, new Attribute("author", aName));
			}
			autoId ++;
		}
		in.close();
		
		System.out.println("Constructing paper-term...");
		in = new TextReader(DBLP.getFilePaperTerm());
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\t");
			Integer eid = idMap.get(Integer.parseInt(parts[0]));
			if (eid == null)
				continue;
			for (int i = 1; i < parts.length; i++) {
				String term = terms.get(Integer.parseInt(parts[i]));
				table.addAttribute(eid, new Attribute("term", term));
			}
		}
		in.close();
		
		System.out.println("Constructing paper-year...");
		in = new TextReader(DBLP.getFilePaper());
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\t");
			Integer eid = idMap.get(Integer.parseInt(parts[0]));
			if (eid == null)
				continue;
			table.addAttribute(eid, new Attribute("year", parts[2]));
			
		}
		in.close();
		
		table.save(Config.Z_FILE_TABLE);		
		table.printStats();
		System.out.print("Done.");
	}

}
