package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import core.GradAscd.DiffFunc;
import utils.TextReader;
import utils.TextWriter;
import utils.Tools;
import data.Example;
import data.Feature;
import data.FeatureDB;


public class GraphSim extends Ranker {
	
	//protected MetaGraphDB gdb;
	protected FeatureDB fdb;
	protected double[] w;
	protected double mu = 5;
	
	public class F implements DiffFunc<Example> {
		
		@Override
		public int numDimensions() {
			return fdb.numDimensions();
		}

		protected double rawValueAt(Example ex, double[] w) {
			double pos = computePair(ex.qnF, ex.qF, ex.nF, w);
			double neg = computePair(ex.qmF, ex.qF, ex.mF, w);
			return pos - neg;
		}
		
		@Override
		public double valueAt(List<Example> examples, double[] w) {
			double result = 0;
			for (Example ex : examples) {
				result += Math.log(Tools.sigmoid(rawValueAt(ex, w) * mu));
			}
			
			return result;
		}
		
		protected double[] getDelta(Feature qaF, Feature qF, Feature aF, double[] w) {
			double[] result = new double[w.length];
			
			double f = 2 * qaF.dotProd(w);
			double g = qF.dotProd(w) + aF.dotProd(w);
			double lower = g * g;
			
			for (int i = 0; i < w.length; i++) {
				double _f = 2 * qaF.get(i);
				double _g = qF.get(i) + aF.get(i);
				double upper = _f * g - f * _g;
				result[i] = Tools.div(upper, lower);
			}
			
			return result;
		}
		
		@Override
		public double[] gradAt(List<Example> examples, double[] w) {
			double[] result = new double[w.length];
			
			
			for (Example ex : examples) {
					double[] pos = getDelta(ex.qnF, ex.qF, ex.nF, w);
					double[] neg = getDelta(ex.qmF, ex.qF, ex.mF, w);
					double v = Tools.sigmoid(rawValueAt(ex, w) * mu);
					for (int i = 0; i < w.length; i++) 
						result[i] += (pos[i] - neg[i]) * mu * (1 - v);
				}
			
			
			return result;
		}


		
		
	};

	
	public GraphSim(FeatureDB fdb) {
		this.fdb = fdb;
	}
	
	public GraphSim(FeatureDB fdb, double[] w) {
		this(fdb);
		this.w = w;
	}
	
	public void setMu(double mu) {
		this.mu = mu;
	}
	
	public void logFeatures() {
		this.fdb.transform();
	}
	
	@Override
	public double getErrorRate(List<Example> l) {
		
		double sum = 0;
		
		for (Example ex : l) {
			ex.updateFeatures(fdb);
			double neg = computePair(ex.qmF, ex.qF, ex.mF, w);
			double pos = computePair(ex.qnF, ex.qF, ex.nF, w);
			if (pos <= neg)
				sum++;
		}
		
		return sum / l.size();
		
		
	
	}
	
	@Override
	public void learnWeights(List<Example> l) throws Exception {
		for (Example ex : l) 
			ex.updateFeatures(fdb);
		
		GradAscd<Example> sgd = new GradAscd<Example>(new F());
		w = sgd.maximize(l).value;
	}
	
	@Override 
	public void outputWeights(String filename) throws Exception {
		List<Score> l = new ArrayList<Score>();
		for (int i = 0; i < w.length; i++) {
			l.add(new Score(i, w[i]));
		}
		Collections.sort(l);
		TextWriter out = new TextWriter(filename);
		for (Score s : l)
			if (s.score > 0)
				out.writeln(s.toString());
		out.close();
	}
	
	/*
	@Override 
	public void outputTime(String filename) throws Exception {
		double mined = 0;
		for (MetaGraph g : gdb.getMetaGraphs())
			mined += g.getTime();
	
		mined = mined / 1000.0 / 3600.0;
		TextWriter out = new TextWriter(filename);
		out.writeln(mined);
		out.close();
	}
	*/
	
	@Override 
	public void readWeights(String filename) throws Exception {
		w = new double[fdb.numDimensions()];
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			int fid = Integer.parseInt(line.split("\\t")[0]);
			double weight = Double.parseDouble(line.split("\\t")[1]);
			w[fid] = weight;
		}
		in.close();
	}
	
	public double[] getWeight() {
		return w;
	}
	
	
	protected double computePair(Feature ab, Feature a, Feature b, double[] w) {
		double upper = 2 * ab.dotProd(w);
		double lower = a.dotProd(w) + b.dotProd(w);
		return Tools.div(upper, lower);
	}
	
	
	@Override
	protected ScoreVector computeResult(int qid, Collection<Integer> candidates) {
		ScoreVector result = new ScoreVector();
		
		for (Integer nid : candidates) {
			Feature a = fdb.get(qid, -1);
			Feature b = fdb.get(-1, nid);
			Feature ab = fdb.get(qid, nid);
			
			//if (a == null) a = new Feature(qid, -1, fdb.isSym());
			//if (b == null) b = new Feature(-1, nid, fdb.isSym());
			//if (ab == null) ab = new Feature(qid, nid, fdb.isSym());
			
			double v = computePair(ab, a, b, w);
			result.put(nid, v);
		}

		return result;
	}

	@Override
	public String getName() {
		return "GSim";
	}

	
}

