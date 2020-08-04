package prep.linkedin;


import prep.DataTable;
import prep.Attribute;
import utils.Config;
import utils.TextReader;


public class BuildTable {
	

	private static void processAttribute(String attrName, DataTable table) throws Exception {
		TextReader in = new TextReader(LinkedIn.getFileAttr(attrName));
		String line;
		while ( (line = in.readln()) != null ) {
			int uid = LinkedIn.parseUserId(line);
			int n = Integer.parseInt(in.readln());
			for (int i = 0; i < n; i++) {
				String v = in.readln().replace('\t', ' ').trim();
				if (!v.isEmpty())
					table.addAttribute(uid, new Attribute(attrName, v));
			}
		}
		in.close();
	}
	
	public static void main(String[] args) throws Exception {
		DataTable table = new DataTable();
		for (String attrName : LinkedIn.attrNameSet) 
			processAttribute(attrName, table);
		
		table.save(Config.Z_FILE_TABLE);	
	}

}
