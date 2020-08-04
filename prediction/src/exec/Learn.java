package exec;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.GraphSim;
import core.GraphSimEmb_sigmod;
import core.Ranker;
import utils.Config;
import utils.Stats;
import utils.TextReader;
import data.Example;
import data.FeatureDB;
import data.Output;

public class Learn {
	
	private static List<Example> loadTrainingExamples(String trainFile) throws Exception {
		List<Example> l = new ArrayList<Example>();
		
		TextReader in = new TextReader(trainFile);
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split("\t");
			int qid = Integer.parseInt(parts[0]);
			int nid = Integer.parseInt(parts[1]);
			int mid = Integer.parseInt(parts[2]);
			l.add(new Example(qid, nid, mid, null));
		}
		in.close();
		
		return l;
				
	}
	
	private static Map<Integer, List<Integer>> loadTestCases(String testFile) throws Exception {
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		
		TextReader in = new TextReader(testFile);
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split("\t");
			int qid = Integer.parseInt(parts[0]);
			List<Integer> cand = new ArrayList<Integer>();
			for (int i = 1; i < parts.length; i++) 
				cand.add(Integer.parseInt(parts[i]));
			map.put(qid, cand);
		}
		in.close();
		return map;
	}
	
	private static Map<Integer, Set<Integer>> loadGroundTruth(String gtFile) throws Exception {
		Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
		
		TextReader in = new TextReader(gtFile);
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split("\t");
			int qid = Integer.parseInt(parts[0]);
			Set<Integer> gt = new HashSet<Integer>(); 
			for (int i = 1; i < parts.length; i++) 
				gt.add(Integer.parseInt(parts[i]));
			map.put(qid, gt);
		}
		in.close();
		return map;
	}
	
	public static void core(Ranker r, int splits, String gtFile, String trainFile, String testFile, String predFile) throws Exception {
		Map<Integer, Set<Integer>> gt = loadGroundTruth(gtFile);
		
		Stats ndcg = new Stats();
		Stats map = new Stats();
		Stats mrr = new Stats();
		
		for (int split = 1; split <= splits; split ++) {
			if (!Config.SILENT)
				System.out.println("--- Split " + split + " ---");
			List<Example> l = loadTrainingExamples(trainFile + split);
											
			// training 
			r.learnWeights(l);
			
			if (!Config.SILENT)
				System.out.println("\tTraining Error = " + r.getErrorRate(l));
				
			// testing 
			Map<Integer, List<Integer>> testCases = loadTestCases(testFile + split);
			Output output = r.rank(testCases);
			output.save(predFile + split);
			
			ndcg.add(output.computeNDCG(gt, 10));
			map.add(output.computeMAP(gt, 10));
			mrr.add(output.computeMRR(gt, 10));
		}
		
		if (!Config.SILENT) {
			System.out.println("\n*** NDCG@10");
			for (int i = 0; i < ndcg.size(); i++) 
				System.out.println(ndcg.get(i));
			
			System.out.println("\n*** MAP@10");
			for (int i = 0; i < map.size(); i++) 
				System.out.println(map.get(i));
			
			System.out.println("\n*** MRR@10");
			for (int i = 0; i < mrr.size(); i++) 
				System.out.println(mrr.get(i));
		}
		
		
		System.out.println();
		System.out.println("*** Test NDCG@10       : " + ndcg.getMean());
		System.out.println("*** Test MAP@10        : " + map.getMean());
		System.out.println("*** Test MRR@10        : " + mrr.getMean());
	}
	

	
	public static void main(String[] args) throws Exception {
		int splits = Integer.parseInt(System.getProperty("splits"));
		String fdbFile = System.getProperty("fdb");
		String gtFile = System.getProperty("gt");
		String trainFile = System.getProperty("train");
		String testFile = System.getProperty("test");
		String predFile = System.getProperty("pred");
		
		FeatureDB fdb = new FeatureDB(Config.SYMMETRY);
		fdb.load(fdbFile);		
							
		GraphSim r = new GraphSim(fdb);
		r.setMu(Config.MU);
		if (Config.LOG_FEATURE)
			r.logFeatures(); 
		
		core(r, splits, gtFile, trainFile, testFile, predFile);
	
	}

}
