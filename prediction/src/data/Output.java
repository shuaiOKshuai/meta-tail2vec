package data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Stats;
import utils.TextReader;
import utils.TextWriter;

public class Output {

	private Map<Integer, List<Integer>> results;
	
	public void add(int qid, List<Integer> l) {
		List<Integer> lDup = new ArrayList<Integer>();
		lDup.addAll(l);
		results.put(qid, lDup);
	}
	
	public Output() {
		results = new HashMap<Integer, List<Integer>>();
	}
	
	public Set<Integer> getQSet() {
		return Collections.unmodifiableSet(results.keySet());
	}
	
	public List<Integer> getRanking(int qid) {
		return Collections.unmodifiableList(results.get(qid));
	}
	
	private double logNorm(int i) {
		if (i == 0)
			return 1;
		else
			return Math.log(i + 1.0) / Math.log(2.0);
	}
	
	private double computeMRRHelper(List<Integer> ranking, Set<Integer> posSet, int k) {
		int first = 0;
		for (int i = 0; i < ranking.size() && i < k; i++) {
			int nid = ranking.get(i);
			if (posSet.contains(nid)) {
				first = i + 1;
				break;
			}
		}
		
		if (first == 0)
			return 0;
		else
			return 1.0 / (double)first;
	}
	

	public double computeMRR(Map<Integer, Set<Integer>> groundtruth, int k) {
		Stats mrr = new Stats();
		for (int qid : this.getQSet()) 
			mrr.add(computeMRRHelper(this.getRanking(qid), groundtruth.get(qid), k));
		return mrr.getMean();
	}
	
	private double computeNDCGHelper(List<Integer> ranking, Set<Integer> posSet, int k) {
		double sum = 0;
		for (int i = 0; i < ranking.size() && i < k; i++) {
			int nid = ranking.get(i);
			int rel = posSet.contains(nid) ? 1 : 0;
			sum += rel / logNorm(i);
		}
		
		double norm = 0;
		for (int i = 0; i < posSet.size() && i < k; i++) 
			norm += 1 / logNorm(i);
		
		return sum / norm;
	}
	

	public double computeNDCG(Map<Integer, Set<Integer>> groundtruth, int k) {
		Stats ndcg = new Stats();
		for (int qid : this.getQSet()) 
			ndcg.add(computeNDCGHelper(this.getRanking(qid), groundtruth.get(qid), k));
		return ndcg.getMean();
	}
	
	private double computeMAPHelper(List<Integer> ranking, Set<Integer> posSet, int k) {
		double sum = 0;
		double relSoFar = 0;
		for (int i = 0; i < k & i < ranking.size(); i++) {
			int nid = ranking.get(i);
			boolean ri = posSet.contains(nid);
			if (ri) {
				relSoFar ++;
				sum += relSoFar / (i + 1);
			}
		}
		return sum / Math.min(posSet.size(), k);
	}
	
	public double computeMAP(Map<Integer, Set<Integer>> groundtruth, int k) {
		Stats map = new Stats();
		for (int qid : this.getQSet()) 
			map.add(computeMAPHelper(this.getRanking(qid), groundtruth.get(qid), k));
			
		return map.getMean();
	}
	
	
	public void load(String filename) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null ) {
			String[] split = line.split("\t");
			int qid = Integer.parseInt(split[0]);
			List<Integer> l = new ArrayList<Integer>();
			for (int i = 1; i < split.length; i++)
				l.add(Integer.parseInt(split[i]));
			add(qid, l);
		}
		in.close();
	}
	
	public void save(String filename) throws Exception {
		TextWriter out = new TextWriter(filename);
		for (int qid : results.keySet()) {
			List<Integer> l = results.get(qid);
			out.write(qid);
			for (int i : l)
				out.write("\t" + i);
			out.writeln();
		}
		out.close();
	}
}
