package data;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


import java.util.Random;
import java.util.Set;

import utils.Config;
import utils.TextReader;
import utils.TextWriter;


public class GroundTruth {
	
	private Map<String, GTRecord> db;
	private Map<String, Set<Integer>> queries;
	private Map<String, Set<Integer>> trainQ;
	private Map<String, Set<Integer>> testQ;
	private int split;
	
	
	public GroundTruth() throws Exception {
		db = new THashMap<String, GTRecord>();
		queries = new THashMap<String, Set<Integer>>();
	}
	
	public Set<String> getSimTypes() {
		return Collections.unmodifiableSet(queries.keySet());
	}
	
	public Set<Integer> getAllQueries(String simType) {
		return Collections.unmodifiableSet(queries.get(simType));
	}
	
//	public void removeSimType(String simType) {
//		db.remove(simType);
//		queries.remove(simType);
//		if (trainQ != null)
//			trainQ.remove(simType);
//		if (testQ != null)
//			testQ.remove(simType);
//	}
	
	public int getRank(String simType, int qid, int nid) {
		GTRecord dummy = new GTRecord(simType, qid);
		GTRecord rec = db.get(dummy.getKey());
		if (rec == null)
			return 0;
		else
			return rec.getRank(nid);
	}
	
	public List<Integer> getCandidateNodes(String simType, int qid) {
		GTRecord dummy = new GTRecord(simType, qid);
		
		List<Integer> result = new ArrayList<Integer>();
		GTRecord rec = db.get(dummy.getKey());
		if (rec != null) 
			result.addAll(rec.getNodes());
		
		// sort by id first to ensure same ordering from set
		Collections.sort(result); 
		Collections.shuffle(result, new Random(1381998011L));
				
		return result;
	}
	
	public void load(String filename) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] splits = line.split("\t");
			String simType = splits[0];
			int qid = Integer.parseInt(splits[1]);
			for (int i = 2; i < splits.length; i++) {
				int colon = splits[i].indexOf(":");
				int nid = Integer.parseInt(splits[i].substring(0, colon));
				int rank = Integer.parseInt(splits[i].substring(colon + 1));
				this.add(simType, qid, nid, rank);
			}
		}
		in.close();
	}
	
	private boolean testRecord(GTRecord rec, int minRank) {
		Set<Integer> nodes = rec.getNodes();
		
		Set<Integer> rankSet = new THashSet<Integer>();
		for (int nid : nodes) {
			int rank = rec.getRank(nid);
			rankSet.add(rank);
		}
		
		return rankSet.size() >= minRank;
	}
	
		
	public void filter(int minRank) {
		List<GTRecord> l = new ArrayList<GTRecord>();
		for (GTRecord rec : db.values()) {
			if (testRecord(rec, minRank))
				l.add(rec);
		}
		
		db = new THashMap<String, GTRecord>();
		queries = new THashMap<String, Set<Integer>>();
		trainQ = null;
		testQ = null;
		
		for (GTRecord rec : l) 
			for (int nid : rec.results.keySet()) {
				int rank = rec.results.get(nid);
				this.add(rec.simType, rec.qid, nid, rank);
			}
		
	}
	
	public void addNoise(double rate) {
		List<GTRecord> l = new ArrayList<GTRecord>();
		for (GTRecord rec : db.values())
			l.add(rec);
		
		db = new THashMap<String, GTRecord>();
		queries = new THashMap<String, Set<Integer>>();
		trainQ = null;
		testQ = null;
		
		Random rnd = new Random(317883090L);
		
		for (GTRecord rec : l) 
			for (int nid : rec.results.keySet()) {
				int rank = rec.results.get(nid);
				if (rnd.nextDouble() < rate)
					rank = rnd.nextBoolean() ? 1 : 0;
				this.add(rec.simType, rec.qid, nid, rank);
			}		
	}
	
	
	public void save(String filename) throws Exception {
		TextWriter out = new TextWriter(filename);
		for (GTRecord rec : db.values()) {
			out.write(rec.simType + "\t");
			out.write(rec.qid);
			for (int nid : rec.getNodes())
				out.write("\t" + nid + ":" + rec.getRank(nid));
			out.writeln();
		}
		out.close();
	}
	
	public void saveTraining(String filename, String simType, int numExamples) throws Exception {
		TextWriter out = new TextWriter(filename);
		for (Example l : generateTrainingExamples(simType, numExamples)) {
			out.writeln(l.qid + "\t" + l.nid + "\t" + l.mid);
		}
		out.close();
	}
	
	public void saveIdealRanking(String filename, String simType) throws Exception {
		TextWriter out = new TextWriter(filename);
		for (int qid : queries.get(simType)) {
			GTRecord dummy = new GTRecord(simType, qid);
			GTRecord rec = db.get(dummy.getKey());
			out.write(qid);
			for (int nid: getIdealRanking(rec))
				out.write("\t" + nid);
			out.writeln();
		}
		out.close();
	}
	
	public void saveTesting(String filename, String simType) throws Exception {
		TextWriter out = new TextWriter(filename);
		for (int qid : testQ.get(simType)) {
			out.write(qid);
			for (int nid: getCandidateNodes(simType, qid))
				out.write("\t" + nid);
			out.writeln();
		}
		out.close();
	}
	
	public void printStats() {
		for (String simType : queries.keySet())
			System.out.printf("%-10d%s\n", queries.get(simType).size(), simType );
	}
	
	public List<Example> generateTrainingExamples(String simType, int numExamples) {
		
		List<Example> result = new ArrayList<Example>();
		for (int qid : trainQ.get(simType)) {
			GTRecord rec = db.get(new GTRecord(simType, qid).getKey());
			List<Example> exList = new ArrayList<Example>();
			for (int nid : rec.getNodes())
				for (int mid : rec.getNodes()) {
					if (nid != mid && rec.getRank(nid) > rec.getRank(mid)) {
						Example ex = new Example(qid, nid, mid, null);
						exList.add(ex);
					}
				}
			result.addAll(exList);
		}
		Random rnd = new Random(14408781L + split * 3 + simType.hashCode() * 11);
		Collections.sort(result);
		Collections.shuffle(result, rnd);
		if (numExamples >= result.size())
			return result;
		else
			return result.subList(0, numExamples); 
	}
	
	public void randomSplit(int split) {
		this.split = split;
		trainQ = new THashMap<String, Set<Integer>>();
		testQ = new THashMap<String, Set<Integer>>();
		for (String simType : getSimTypes()) {
			Set<Integer> train = new THashSet<Integer>();
			Set<Integer> test = new THashSet<Integer>();
			trainQ.put(simType, train);
			testQ.put(simType, test);
			List<Integer> qidList = new ArrayList<Integer>(queries.get(simType));
			Collections.sort(qidList); // fix order
			Collections.shuffle(qidList, new Random(split * 7 + simType.hashCode() * 23));
			int div = (int)(qidList.size() * (1 - Config.FRACTION_TEST_QUERIES));
			train.addAll(qidList.subList(0, div));
			test.addAll(qidList.subList(div, qidList.size()));
		}
	}
	

	
	public void add(String simType, int qid, int nid, int rank) {
		GTRecord dummy = new GTRecord(simType, qid);

		GTRecord rec = db.get(dummy.getKey());
		if (rec == null) { 
			rec = new GTRecord(simType, qid);
			db.put(rec.getKey(), rec);
		}
		rec.addNode(nid, rank);
		
		// query set
		Set<Integer> qSet = queries.get(rec.simType);
		if (qSet == null) {
			qSet = new THashSet<Integer>();
			queries.put(rec.simType, qSet);
		}
		qSet.add(rec.qid);
	}
	
	private double logNorm(int i) {
		if (i == 0)
			return 1;
		else
			return Math.log(i + 1.0) / Math.log(2.0);
	}
	
	private double computeNDCGHelper(GTRecord rec, List<Integer> ranking, int k) {
		double sum = 0;
		for (int i = 0; i < ranking.size() && i < k; i++) {
			int nid = ranking.get(i);
			int rank = rec.getRank(nid);
			sum += rank / logNorm(i);			
		}
		return sum;
	}
	
	/*
	public List<Integer> getIdealRanking(String simType, int qid) {
		GTRecord rec = db.get(new GTRecord(simType, qid).getKey());
		return getIdealRanking(rec);
	}
	*/
	
	private List<Integer> getIdealRanking(final GTRecord rec) {
		List<Integer> ideal = new ArrayList<Integer>();
		for (int nid : rec.getNodes()) 
			if (rec.getRank(nid) > 0)
				ideal.add(nid);
		
		Collections.sort(ideal, new Comparator<Integer>() {

			@Override
			public int compare(Integer arg0, Integer arg1) {
				int rank0 = rec.getRank(arg0);
				int rank1 = rec.getRank(arg1);
				return - Integer.compare(rank0, rank1);
			}}
		);
		
		return ideal; 
	}
	
	
	
	public double computeMAP(String simType, int qid, List<Integer> ranking, int k) {
		GTRecord rec = db.get(new GTRecord(simType, qid).getKey());
		List<Integer> idealRanking = getIdealRanking(rec);
		double sum = 0;
		double relSoFar = 0;
		for (int i = 0; i < k & i < ranking.size(); i++) {
			int nid = ranking.get(i);
			boolean ri = rec.getRank(nid) > 0;
			if (ri) {
				relSoFar ++;
				sum += relSoFar / (i + 1);
			}
		}
		return sum / Math.min(idealRanking.size(), k);
	}
	
	public double computeNDCG(String simType, int qid, List<Integer> ranking, int k) {
		GTRecord rec = db.get(new GTRecord(simType, qid).getKey());
		
		double idealScore = computeNDCGHelper(rec, getIdealRanking(rec), k);
		double actualScore = computeNDCGHelper(rec, ranking, k);
		if (idealScore == 0)
			return Double.NaN;
		else
			return actualScore / idealScore;
	}
	
			
	private class GTRecord {

		public String simType;
		public int qid;
		private Map<Integer, Integer> results;
		
		public Set<Integer> getNodes() {
			return Collections.unmodifiableSet(results.keySet());
		}
		
		public int getRank(int nid) {
			if (results.containsKey(nid))
				return results.get(nid);
			else
				return 0;
		}
		
		public void addNode(int nid, Integer rank) {
			if (results.containsKey(nid)) {
				int oldRank = results.get(nid);
				if (rank != oldRank) {
					rank = Math.max(rank, oldRank);
					System.out.println("Warning: adding inconsistent rank, retaining the highest only. " +
						"(" + simType + ", qid=" + qid +", nid=" + nid + ", rank=" + rank + ")");
				}
			}
			results.put(nid, rank);
		}
		
		
		public GTRecord(String simType, int qid) {
			this.simType = simType;
			this.qid = qid;
			this.results = new THashMap<Integer, Integer>();
		}
		
		public String getKey() {
			return simType + "," + qid;
		}
	}
	
}
