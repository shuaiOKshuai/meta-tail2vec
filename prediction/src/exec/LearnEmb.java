package exec;

import java.util.Set;

import core.GraphSimEmb;
import core.GraphSimEmb_nonactivation;
import core.GraphSimEmb_sigmod;
import data.Feature;
import data.FeatureDB;
import gnu.trove.set.hash.THashSet;
import utils.Config;
import utils.TextReader;


public class LearnEmb {
	
	private static double[][] loadEmb(String embFile) throws Exception {
		TextReader in = new TextReader(embFile);
		String[] header = in.readln().split(" ");
		int nodes = Integer.parseInt(header[0]);
		int dims = Integer.parseInt(header[1]);
		
		double[][] emb = new double[nodes][dims];
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split(" ");
			int nid = Integer.parseInt(parts[0]);
			for (int d = 0; d < dims; d ++)
				emb[nid][d] = Double.parseDouble(parts[d + 1]);
		}
		in.close();
		
		return emb;
	}
	
	/*
	private static void loadTrainPairs(String trainFile, Set<String> pairs) throws Exception {
		
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
	
	private static void loadTestPairs(String testFile, Set<String> pairs) throws Exception {
		
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
	*/
	
	
	
	private static FeatureDB buildFeatureFromEmb(double[][] emb, Set<String> pairs) throws Exception {
		FeatureDB fdb = new FeatureDB(Config.SYMMETRY);
						
		int dims = emb[0].length;		
		fdb.initEmbAsFeatures(dims * 2);
		
		
		for (String pair : pairs) {
			String[] parts = pair.split(",");
			int i = Integer.parseInt(parts[0]);
			int j = Integer.parseInt(parts[1]);
			fdb.add(new Feature(i, j, Config.SYMMETRY));
			//fdb.add(new Feature(i, -1, Config.SYMMETRY));
			//fdb.add(new Feature(-1, j, Config.SYMMETRY));
		}
		
		
		for (Feature feat : fdb.getAllFeatures()) {
			int nid = feat.getKey().getNid();
			int mid = feat.getKey().getMid();
			for (int d = 0; d < dims; d++) {
				double nval = nid != -1 ? emb[nid][d] : 0.5;
				double mval = mid != -1 ? emb[mid][d] : 0.5;
				feat.set(d, nval);
				feat.set(d + dims, mval);
			}			
		}
		return fdb;
		
	}
	
	public static void main(String[] args) throws Exception {
		int splits = Integer.parseInt(System.getProperty("splits"));
		String embFile = System.getProperty("emb");
		String gtFile = System.getProperty("gt");
		String trainFile = System.getProperty("train");
		String testFile = System.getProperty("test");
		String predFile = System.getProperty("pred");
		
		double[][] emb = loadEmb(embFile);
		Set<String> pairs = new THashSet<String>();
		for (int split = 1; split <= splits; split ++) {
			prep.BuildFeature.loadTrainPairs(trainFile + split, pairs);
			prep.BuildFeature.loadTestPairs(testFile + split, pairs);
		}
		
		FeatureDB fdb = buildFeatureFromEmb(emb, pairs);
		GraphSimEmb_sigmod r = new GraphSimEmb_sigmod(fdb);
		//r.setMu(Config.MU);
		
		Learn.core(r, splits, gtFile, trainFile, testFile, predFile);
	}
}
