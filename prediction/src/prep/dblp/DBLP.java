package prep.dblp;

import java.util.Map;
import java.util.Set;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import utils.Config;
import utils.TextReader;

public class DBLP {
	
	public static String getFilePaper() { return Config.getZDirRaw() + "SunYZ/paper.txt"; };
	public static String getFilePaperTerm() { return Config.getZDirRaw() + "SunYZ/paper_term.txt"; };
	public static String getFilePaperEvent() { return Config.getZDirRaw() + "SunYZ/paperEvent.txt"; };
	public static String getFileAuthor() { return Config.getZDirRaw() + "SunYZ/author.txt"; };
	public static String getFileTerm() { return Config.getZDirRaw() + "SunYZ/term.txt"; };
	public static String getFileConf() { return Config.getZDirRaw() + "SunYZ/conf.txt"; };
	
	public static String getFileLabelAuthor() { return Config.getZDirRaw() + "WangC/author.txt"; };
	public static String getFileLabelAI() { return Config.getZDirRaw() + "WangC/ai.ans"; };
	public static String getFileLabelMath() { return Config.getZDirRaw() + "WangC/MathGenealogy_50896.ans"; };
	public static String getFileLabelTeacher() { return Config.getZDirRaw() + "WangC/teacher.ans"; };
	public static String getFileLabelPhd() { return Config.getZDirRaw() + "WangC/phd.ans"; };
	public static String getFileLabelColleague() { return Config.getZDirRaw() + "WangC/colleague.txt"; };
	
	
	private static void addAuthors(String filename, Set<Integer> authors) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split(" |\t");
			int a = Integer.parseInt(splits[0]);
			int b = Integer.parseInt(splits[1]);
			authors.add(a);
			authors.add(b);
		}
		in.close();
	}
	
	public static Map<Integer, String> loadLabelAuthors() throws Exception {
		Map<Integer, String> result = new THashMap<Integer, String>();
		
		TextReader in = new TextReader(DBLP.getFileLabelAuthor());
		String line;
		while ( (line = in.readln()) != null ) {
			String[] parts = line.split("\t");
			int wid = Integer.parseInt(parts[0]); // Wang Chi's author ID
			String aName = parts[1].toUpperCase();
			result.put(wid, aName);			
		}
		in.close();
		
		Set<Integer> authors = new THashSet<Integer>();
		addAuthors(DBLP.getFileLabelAI(), authors);
		addAuthors(DBLP.getFileLabelMath(), authors);
		addAuthors(DBLP.getFileLabelTeacher(), authors);
		addAuthors(DBLP.getFileLabelPhd(), authors);
		addAuthors(DBLP.getFileLabelColleague(), authors);
		result.keySet().retainAll(authors);
		
		return result;
	}
	

}
