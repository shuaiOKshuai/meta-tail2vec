package prep;




import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import data.Feature;
import data.FeatureDB;
import data.MetaGraphDB;
import data.MetaGraphDB.MetaGraph;
import gnu.trove.set.hash.THashSet;
import utils.Config;
import utils.Parallel;
import utils.Parallel.Operation;
import utils.Progress;
import utils.TextReader;

public class BuildFeature {
		
	/*
	private static String getTmpDir() {
		return (new File(Config.FILE_FEATURE_DB)).getParent() + "/mergeTmp/";
	}
	
	private static Feature parseFeature(String line) {
		if (line == null)
			return null;
		else
			return new Feature(line, Config.SYMMETRY);
	}
	
	private static void writeRemaining(Feature f, TextReader in, TextWriter out) throws Exception {
		while (f != null) {
			out.writeln(f.toString());
			f = parseFeature(in.readln());
		}
	}
	
	private static void merge(String file1, String file2, String fileOut) throws Exception {
		TextReader in1 = new TextReader(file1);
		TextReader in2 = new TextReader(file2);
		TextWriter out = new TextWriter(fileOut);
		
		Feature f1 = parseFeature(in1.readln());
		Feature f2 = parseFeature(in2.readln());
		while (f1 != null && f2 != null) {
			if (f1.compareTo(f2) < 0) {
				out.writeln(f1.toString());
				f1 = parseFeature(in1.readln());
			}
			else if (f1.compareTo(f2) == 0) {
				f1.add(f2);
				out.writeln(f1.toString());
				f1 = parseFeature(in1.readln());
				f2 = parseFeature(in2.readln());
			}
			else { // (f1 > f2)
				out.writeln(f2.toString());
				f2 = parseFeature(in2.readln());
			}
		}
		
		writeRemaining(f1, in1, out);
		writeRemaining(f2, in2, out);
		
		in1.close();
		in2.close();
		out.close();
	}
	
	private static void delete(String file) {
		(new File(file)).delete();
	}
	
	private static void mergeAll(int numPartitions) throws Exception {
		for (int i = 1; i < numPartitions; i++) {
			System.out.print("+");
			String file1 = (i == 1) ? getTmpDir() + "0" : getTmpDir() + "0_" + (i - 1);
			String file2 = getTmpDir() + i;
			String out = (i < numPartitions - 1) ? getTmpDir() + "0_" + i : Config.FILE_FEATURE_DB;
			merge(file1, file2, out);
			delete(file1);
			delete(file2);
		}
		delete(getTmpDir());
		System.out.println();
	}
	*/
	
	public static void loadTrainPairs(String trainFile, Set<String> pairs) throws Exception {
		
		TextReader in = new TextReader(trainFile);
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split("\t");
			int qid = Integer.parseInt(parts[0]);
			int nid = Integer.parseInt(parts[1]);
			int mid = Integer.parseInt(parts[2]);
			pairs.add(qid + "," + nid);
			pairs.add(qid + "," + mid);
		}
		in.close();
	}
	
	public static void loadTestPairs(String testFile, Set<String> pairs) throws Exception {
		
		TextReader in = new TextReader(testFile);
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split("\t");
			int qid = Integer.parseInt(parts[0]);
			for (int i = 1; i < parts.length; i++) {
				int nid = Integer.parseInt(parts[i]);
				pairs.add(qid + "," + nid);
			}
		}
		in.close();
	}
	
	private static void process(final MetaGraphDB gdb, final FeatureDB fdb, boolean append, String filename) throws Exception {
		final Progress prog = new Progress("Filling features", gdb.numInstances());
		
		Parallel.forEach(gdb.getMetaGraphs(), 4, new Operation<MetaGraph>() {
			@Override
			public void perform(MetaGraph g) throws Exception {
				/*
				TextReader in = new TextReader(Config.getFileMetagraphInstance(g.getId()));
				double entropy = g.computeEntropy(Config.CORE_TYPE, in);
				if (entropy > 7.4346)
					return;
				in.close();
				//synchronized (gdb) {
				//	System.out.println(g.getId() + " " + g.numInstances() + " " + g.computeEntropy(Config.CORE_TYPE, in));
				//}
				 
				 */
				TextReader in = new TextReader(Config.getFileMetagraphInstance(g.getId()));
				int[] inst;
				while ( (inst = g.readInstance(in)) != null) {
					fdb.fill(g, inst, Config.CORE_TYPE);
					prog.tick();
				}
				in.close();		
			}});	
		prog.done();
		fdb.save(filename, append);
	}
	
	public static void main(String[] args) throws Exception {		
		
		long start = System.currentTimeMillis();
		MetaGraphDB gdb = new MetaGraphDB();		
		gdb.readDB(Config.FILE_METAGRAPH_DB);
		
		final FeatureDB fdb_base = new FeatureDB(Config.SYMMETRY);
		final FeatureDB fdb_mg2vec = new FeatureDB(Config.SYMMETRY);
		
		if (args[0].equals("base")) {
			/*
			int splits = Integer.parseInt(System.getProperty("splits"));
			String trainFile = System.getProperty("train");
			String testFile = System.getProperty("test");
			
			System.out.println("Preparing labels");
			fdb_base.createAllFid(gdb, Config.CORE_TYPE);	
			
			Set<String> pairs = new THashSet<String>();
			for (int split = 1; split <= splits; split ++) {
				loadTrainPairs(trainFile + split, pairs);
				loadTestPairs(testFile + split, pairs);
			}
			
			for (String pair : pairs) {
				String[] parts = pair.split(",");
				int i = Integer.parseInt(parts[0]);
				int j = Integer.parseInt(parts[1]);
				fdb_base.add(new Feature(i, j, Config.SYMMETRY));
				fdb_base.add(new Feature(i, -1, Config.SYMMETRY));
				fdb_base.add(new Feature(-1, j, Config.SYMMETRY));
			}
			*/
			
			System.out.println("Preparing labels");
			fdb_base.createAllFid(gdb, Config.CORE_TYPE);	
			
			TextReader in = new TextReader(Config.getCSVLabelsLink());
			String line;
			
			in.readln(); // ignore header
			while ((line = in.readln()) != null) {
				String[] parts = line.split(",");
				int i = Integer.parseInt(parts[0]);
				int j = Integer.parseInt(parts[1]);
				fdb_base.add(new Feature(i, j, Config.SYMMETRY));
				fdb_base.add(new Feature(i, -1, Config.SYMMETRY));
				fdb_base.add(new Feature(-1, j, Config.SYMMETRY));
			}
			in.close();
			
			
			/*
			TextReader in = new TextReader(Config.getCSVLabelsNode());
			String line;
			in.readln(); // ignore header
			while ((line = in.readln()) != null) {
				String[] parts = line.split(",");
				int i = Integer.parseInt(parts[0]);
				fdb_base.add(new Feature(i, -1, Config.SYMMETRY));
			}
			in.close();
			*/			
			
			process(gdb, fdb_base, false, Config.FILE_FEATURE_DB);
			
		}
		if (args[0].equals("mg2vec")) {	
			fdb_mg2vec.createAllFid(gdb, Config.CORE_TYPE);	
		
			TextReader in = new TextReader(Config.FILE_PAIR_DB);
			String line;
			boolean append = false;
			while ((line = in.readln()) != null) {
				String[] parts = line.split("\t");
				int i = Integer.parseInt(parts[0]);
				int j = Integer.parseInt(parts[1]);
				fdb_mg2vec.add(new Feature(i, j, Config.SYMMETRY));
				
				if (fdb_mg2vec.size() == 10000000) {
					process(gdb, fdb_mg2vec, append, Config.FILE_FEATURE_DB + ".mg2vec");
					append = true;
					fdb_mg2vec.clear();
				}
			
			}
			process(gdb, fdb_mg2vec, append, Config.FILE_FEATURE_DB + ".mg2vec");
			in.close();
				
			
		}
		if (!args[0].equals("base") && !args[0].equals("mg2vec") && !args[0].equals("both")) 
			throw new Exception("Unknown method!");

		System.out.println("Done");	
		System.out.println("Total time spent: " + (System.currentTimeMillis() - start) / 1000 + " sec");		
	}
	
}
