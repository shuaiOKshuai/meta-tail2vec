package prep.facebook;



import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import prep.Attribute;
import prep.DataTable;
import utils.Config;
import utils.TextReader;

public class BuildTable {
	
	private static final String[] exclusions = new String[] { 
					"education;type", "education;with;id", "education;year;id", "education;classes;id",
					"work;from;id", "work;with;id", "work;position;id", "work;start_date", "work;end_date",  // work;projects;id added
					"middle_name", "name", "gender", "locale", "languages;id", "birthday", "first_name", "political", "religion" };
	private static final Set<String> exclSet = new THashSet<String>(Arrays.asList(exclusions));
	
	private static void processEgoUser(int egoId, DataTable table) throws Exception {
		// load feature names
		String line;
		TextReader inNames = new TextReader(Facebook.getFileAttrName(egoId));
		List<Attribute> l = new ArrayList<Attribute>();
		while ( (line = inNames.readln()) != null ) {
			int space = line.indexOf(" ");
			int colon = line.lastIndexOf(";");
			String type = line.substring(space + 1, colon);
			String value = line.substring(colon + 1);
			l.add(new Attribute(type, value));
		}
		inNames.close();
		
		// load features
		TextReader inFeat = new TextReader(Facebook.getFileAttr(egoId));
		while ( (line = inFeat.readln()) != null ) {
			String[] splits = line.split(" ");
			int friendId = Integer.parseInt(splits[0]);
			for (int i = 1; i < splits.length; i++) {
				Attribute f = l.get(i - 1);
				if (splits[i].equals("1") && !exclSet.contains(f.key))
					table.addAttribute(friendId, f);
			}
		}
		inFeat.close();
		
		// load ego features
		TextReader egoFeat = new TextReader(Facebook.getFileEgoAttr(egoId));
		line = egoFeat.readln();
		String[] splits = line.split(" ");
		for (int i = 0; i < splits.length; i++) {
			Attribute f = l.get(i);
			if (splits[i].equals("1") && !exclSet.contains(f.key))
				table.addAttribute(egoId, f);
		}
		egoFeat.close();
	}
	
	public static void main(String[] args) throws Exception {
		DataTable table = new DataTable();
		for (int egoId : Facebook.egoIdSet)
			processEgoUser(egoId, table);
		
		table.save(Config.Z_FILE_TABLE);
		
	}

}
