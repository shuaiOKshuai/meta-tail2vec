package core;

import java.util.*;
import data.Example;
import data.Output;

public abstract class Ranker {

	protected abstract ScoreVector computeResult(int qid, Collection<Integer> candidates);
	public abstract String getName();
		
	public double getErrorRate(List<Example> l) { return 1.0; };
	public void learnWeights(List<Example> l) throws Exception {};
	//public void outputTime(String filename) throws Exception {};
	public void outputWeights(String filename) throws Exception {};
	public void readWeights(String filename) throws Exception {};
	
	public List<Integer> search(int qid, List<Integer> candidates) throws Exception {
		ScoreVector vector = computeResult(qid, candidates);
		List<Score> l = vector.toSortedScoreList();
		
		List<Integer> result = new ArrayList<Integer>();
		for (Score score : l) 
			result.add(score.id);
		
		return result;
	}
	
	public Output rank(Map<Integer, List<Integer>> testCases) throws Exception {
		Output output = new Output();
		for (int qid : testCases.keySet()) {
			List<Integer> result = search(qid, testCases.get(qid));
			output.add(qid, result);
		}
		return output;
	}
	
	
	
}

