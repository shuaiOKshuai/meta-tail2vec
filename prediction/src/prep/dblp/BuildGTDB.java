package prep.dblp;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import utils.Config;
import utils.TextReader;
import utils.TextWriter;
import data.Edge;
import data.GroundTruth;
import data.Graph;
import data.Node;


public class BuildGTDB {
	
	private static Map<String, Integer> idMap; 
	private static Map<Integer, String> gtAuthors;
	private static Graph g;
		
	private static Integer getId(int gtId) {
		String aName = gtAuthors.get(gtId);
		return idMap.get(aName);
	}
	
	private static boolean isAuthor(int nid) {
		return g.getNode(nid).getType().equals("author");
	}
	
	private static void processAdviseeAdvisor(String filename, GroundTruth gtdb) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split(" ");
			Integer advisee = getId(Integer.parseInt(splits[0]));
			Integer advisor = getId(Integer.parseInt(splits[1]));
			if (advisor == null || advisee == null)
				continue;			
			if (!isAuthor(advisee) || !isAuthor(advisor))
				throw new Exception("Something wrong: advisor or advisee must be an 'author'!");
			
			gtdb.add("advisor", advisee, advisor, 1);
			gtdb.add("advisee", advisor, advisee, 1);
			
			gtdb.add("advisor", advisor, advisee, 0);
			gtdb.add("advisee", advisee, advisor, 0);
			gtdb.add("colleague", advisee, advisor, 0);
			gtdb.add("colleague", advisor, advisee, 0);
		}
		in.close();
	}
	
	// should only be invoked after Advisor/advisee
	private static void processColleague(String filename, GroundTruth gtdb) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split("\t");
			Integer a = getId(Integer.parseInt(splits[0]));
			Integer b = getId(Integer.parseInt(splits[1]));
			if (a == null || b == null)
				continue;
			if (!isAuthor(a) || !isAuthor(b))
				throw new Exception("Something wrong: colleague must be an 'author'!");
						
			if (gtdb.getRank("advisor", a, b) == 0 && gtdb.getRank("advisor", b, a) == 0) {
				gtdb.add("colleague", a, b, 1);
				gtdb.add("colleague", b, a, 1);
				
				gtdb.add("advisor", a, b, 0);
				gtdb.add("advisor", b, a, 0);
				gtdb.add("advisee", a, b, 0);
				gtdb.add("advisee", b, a, 0);	
			}

		}
		in.close();
	}
	
	
	private static List<Integer> getCoAuthors(int qid) {
		Set<Integer> result = new THashSet<Integer>();
		
		for (Edge e : g.getNode(qid).neighbors) 
			for (Edge e2 : g.getNode(e.otherEnd.getId()).neighbors) {
				int mid = e2.otherEnd.getId();
				if (isAuthor(mid) && mid != qid) 
					result.add(mid);
			}
		
		List<Integer> l = new ArrayList<Integer>(result);
		// sort by id first to ensure same ordering from set
		Collections.sort(l); 
		Collections.shuffle(l, new Random(3998011L + qid * 11));
		return Collections.unmodifiableList(l);
	}
	
	
	private static void processCoAuthorsForRanking(GroundTruth gtdb, Graph g) {
		for (String simType : gtdb.getSimTypes()) 
			for (int qid : gtdb.getAllQueries(simType)) {
				int count = 0;
				for (int nid : getCoAuthors(qid)) 
					if (gtdb.getRank(simType, qid, nid) == 0) {
						gtdb.add(simType, qid, nid, 0);						
						// note: it is still necessary to add rank 0 record explicitly. The getRank() test returns 0 too if no such record found.
						count ++;
						if (count >= 5)
							break;
					}
			}
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("Loading graph...");
		g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
		idMap = new THashMap<String, Integer>();
		for (Node node : g.getNodes()) {
			if (isAuthor(node.getId()))
				idMap.put(node.getContent(), node.getId());
		}
		gtAuthors = DBLP.loadLabelAuthors();
		
		System.out.println("Generating groundtruth...");
		GroundTruth gtdb = new GroundTruth();
		
		processAdviseeAdvisor(DBLP.getFileLabelAI(), gtdb);
		processAdviseeAdvisor(DBLP.getFileLabelMath(), gtdb);
		processAdviseeAdvisor(DBLP.getFileLabelTeacher(), gtdb);
		processAdviseeAdvisor(DBLP.getFileLabelPhd(), gtdb);
		
		processColleague(DBLP.getFileLabelColleague(), gtdb);	
		processCoAuthorsForRanking(gtdb, g);
		
		
		
		System.out.println("Generating training/testing splits for link prediction...");
		
		TextWriter out = new TextWriter(Config.getCSVLabelsLink());
		out.writeln("NodeX,NodeY,LinkType");
		
		for (String simType : new String[] { "advisor", "advisee", "colleague" }) {
			for (int qid :  gtdb.getAllQueries(simType)) {
				for (int nid : gtdb.getCandidateNodes(simType, qid)) {
					if (gtdb.getRank(simType, qid, nid) > 0) {
						out.writeln(qid + "," + nid + "," + simType);
					}
				}
			}
		}		
		out.close();
		
		gtdb.filter(2);
		
		
		
		System.out.println("Generating training/testing splits for semantic proximity...");
		for (String simType : new String[] {"advisor", "advisee"} ) {
			for (int split = 1; split <= 10; split++) {
				gtdb.randomSplit(split);
				gtdb.saveTraining(Config.getFileTrain(simType, split), simType, 1000); 
				gtdb.saveTesting(Config.getFileTest(simType, split), simType);				
			}
			gtdb.saveIdealRanking(Config.getFileIdeal(simType), simType);
		}
		
		gtdb.printStats();
		gtdb.save(Config.FILE_GROUNDTRUTH_DB);

		System.out.println("Done.");
	}

}
