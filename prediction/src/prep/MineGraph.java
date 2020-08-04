package prep;




import java.io.File;
import java.util.ArrayList;
import java.util.List;

import data.MetaGraphDB;
import data.MetaGraphDB.MetaGraph;
import utils.Config;
import utils.Parallel;
import utils.Parallel.Operation;
import utils.Progress;
import utils.TextReader;
import utils.TextWriter;
import utils.Tools;

public class MineGraph {
	
	private static void createTimeLog(List<QueryStruct> l) throws Exception {
		TextWriter out = new TextWriter(Config.FILE_METAGRAPH_TIMELOG);
		for (QueryStruct q : l)
			out.writeln(q.qid + "\t" + q.gid + "\t" + q.elapsed);
		out.close();
	}
	
	private static void moveMetaGraph(List<QueryStruct> l) throws Exception {
		for (QueryStruct q : l) {
			File f1 = new File(iFile(q));
			File f2 = new File(Config.getFileMetagraphInstance(q.gid));
			if (f2.exists()) 
				f2.delete();
			if (!f1.renameTo(f2))
				throw new Exception("Cannot metagraph for qid = " + q.qid);
		}
	}
	
	private static void removeDir(List<QueryStruct> l) {
		for (QueryStruct q : l) {
			File dir = new File(qDir(q));
			for (File f : dir.listFiles())
				if (!f.delete())
					System.out.println("Cannot delete " + f.getPath());
			if (!dir.delete())
				System.out.println("Cannot delete " + dir.getPath());
		}
	}
	
	private static MetaGraph loadMetaGraph(QueryStruct q) throws Exception {
		MetaGraphDB gdb = new MetaGraphDB();
		gdb.readDB(gFile(q));
		return gdb.getMetaGraph(1);
	}
	
	private static void createMetaGraphDB(List<QueryStruct> l) throws Exception {
		TextWriter out = new TextWriter(Config.FILE_METAGRAPH_DB);
		for (QueryStruct q : l) {
			MetaGraph g = loadMetaGraph(q);
			g.updateId(q.gid);
			out.write(g.getTextRepresentation());
		}
		out.close();
	}
	
	private static void createGraphletSim() throws Exception {
		ProcessBuilder pb = new ProcessBuilder(Config.LIB_SUBMATCH, 
				Tools.genArg("mode", 1),
				Tools.genArg("data", Config.FILE_METAGRAPH_DB)
		);	
		Tools.startProcessAndWaitFor(pb, null, true);
		
		File f1 = new File(Config.FILE_METAGRAPH_DB + ".m");
		File f2 = new File(Config.FILE_METAGRAPH_SIM);
		if (f2.exists()) 
			f2.delete();
		if (!f1.renameTo(f2))
			throw new Exception("Cannot rename metagraph sim file");
	}
	
	private static String qDir(QueryStruct q) {
		return Config.getDirMetagraphInstance() + "q." + q.qid + File.separator;
	}
	
	private static String qFile(QueryStruct q) {
		return qDir(q) + "q";
	}
	
	private static String gFile(QueryStruct q) {
		return qDir(q) + "g";
	}
	
	private static String iFile(QueryStruct q) {
		return qDir(q) + "1";
	}
	
	private static List<QueryStruct> pruneQuery(List<QueryStruct> l) throws Exception {
		String tmp = Config.FILE_METAGRAPH_DB + ".tmp";
		ProcessBuilder pb = new ProcessBuilder(Config.LIB_SUBMATCH, 
				Tools.genArg("mode", 3),
				Tools.genArg("query", Config.FILE_METAGRAPH_QUERY),
				Tools.genArg("subgraph", tmp)				
		);	
		Tools.startProcessAndWaitFor(pb, null, true);
		
		MetaGraphDB gdb = new MetaGraphDB();
		gdb.readDB(tmp);
		new File(tmp).delete();
		
		List<QueryStruct> result = new ArrayList<QueryStruct>();
		int newGid = 0;
		for (QueryStruct q : l) {
			MetaGraph g = gdb.getMetaGraph(q.qid + 1);  // wenqing's gid starts from 1, but qid starts from 0, so +1
			if (g.numOfTypes() < 2)
				continue;
			if (g.numOfCoreNodes(Config.CORE_TYPE) < 2)
				continue;				
			if (Config.SYMMETRY && !g.isCoreSymmetric(Config.CORE_TYPE))
				continue;						
			if (g.hasDanglingNode(Config.CORE_TYPE, Config.SYMMETRY))
				continue;
						
			q.gid = newGid;
			newGid ++;
			result.add(q);
		}
		return result;
	}
	
	private static boolean callSubmatch(QueryStruct q) throws Exception {
				
		// prepare tmp directory
		String qDir = qDir(q);
		String iFile = iFile(q);
		String qFile = qFile(q);
		String gFile = gFile(q);
		String tmpFile = qDir + "tmp"; 
		Tools.createDirs(qDir);
		TextWriter out = new TextWriter(qFile);
		out.write(q.text);
		out.close();
		
		// prepare and run process
		ProcessBuilder pb = new ProcessBuilder(Config.LIB_SUBMATCH, 
				Tools.genArg("mode", 2),
				Tools.genArg("stats", qDir), 
				Tools.genArg("subgraph", gFile),
				Tools.genArg("result", tmpFile),
				Tools.genArg("data", Config.FILE_GRAPH),
				Tools.genArg("query", qFile),
				Tools.genArg("maxfreq", Config.MAX_FREQ) //,
		//		Tools.genArg("omark", 1)
		);	
		q.elapsed = Tools.startProcessAndWaitFor(pb, null, true);
		MetaGraph g = loadMetaGraph(q);
		if (g == null)
			return false;
		
		if (g.numInstances() == -1) {
			new File(iFile).delete(); 
			// create an empty iFile;
			TextWriter empty = new TextWriter(iFile);
			empty.close();
		}
		return true;
	}
	
	private static class QueryStruct {
		public String text;
		public int qid;
		public int gid;
		public long elapsed;
		
		public QueryStruct(String text, int qid) {
			this.text = text;
			this.qid = qid;
		}
	}
	
	private static List<QueryStruct> parseQuery() throws Exception {
		List<QueryStruct> l = new ArrayList<QueryStruct>();
		
		TextReader in = new TextReader(Config.FILE_METAGRAPH_QUERY);
		String line;
		String queryText = null;
		while ( (line = in.readln()) != null) {
			if (line.equals("t #")) {
				if (queryText != null)
					l.add(new QueryStruct(queryText, l.size()));
				queryText = "";
			}
			queryText += line + "\n";
		}
		l.add(new QueryStruct(queryText, l.size()));
		in.close();
		
		return l;
	}
	
	public static void main(String[] args) throws Exception {
		List<QueryStruct> l = parseQuery();
		System.out.println(l.size() + " queries parsed");
		l = pruneQuery(l);
		System.out.println(l.size() + " queries remained after pruning");
		
		// output filtered query
		TextWriter out = new TextWriter(Config.getFileMetagraphQueryPruned());
		for (QueryStruct q : l)
			out.write(q.text);
		out.close();
		
		
		final Progress prog = new Progress("Mining subgraph", l.size());
		final List<QueryStruct> nullQuery = new ArrayList<QueryStruct>();
		Parallel.forEach(l, Config.MAX_THREADS, new Operation<QueryStruct>() {
			@Override
			public void perform(QueryStruct q) throws Exception {
				boolean success = callSubmatch(q);
				if (!success) {
					synchronized (nullQuery) {
						nullQuery.add(q);
					}
				}
				prog.tick();
			}} );
		
		prog.done();
				
		System.out.println("Cleaning up");
		l.removeAll(nullQuery);
		createTimeLog(l);
		createMetaGraphDB(l);
		moveMetaGraph(l);
		removeDir(l);
		removeDir(nullQuery);
		
		System.out.println("Computing metagraph similarity");
		createGraphletSim();
		
		System.out.println("Done.");
		
	}

}
