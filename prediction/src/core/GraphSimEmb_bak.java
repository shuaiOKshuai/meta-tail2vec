package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import core.GradAscd.DiffFunc;
import utils.TextWriter;
import utils.Tools;
import data.Example;
import data.Feature;
import data.FeatureDB;



public class GraphSimEmb_bak extends Ranker {
	
	protected FeatureDB fdb;
	protected double[] w;
	
	
	public class F implements DiffFunc<Example> {
		
		@Override
		public int numDimensions() {
			return fdb.numDimensions();
		}

		protected double rawValueAt(Example ex, double[] w) {
			double pos = computePair(ex.qnF, w);
			double neg = computePair(ex.qmF, w);
			return pos - neg;
		}
		
		@Override
		public double valueAt(List<Example> examples, double[] w) {
			double result = 0;
			for (Example ex : examples) {
				result += Math.log(Tools.sigmoid(rawValueAt(ex, w)));
			}
			
			return result;
		}
		
		protected double[] getDelta(Feature qnF, double[] w) {
			double[] result = new double[w.length];
						
			for (int i = 0; i < w.length; i++) 
				result[i] = qnF.get(i);;
			
			return result;
		}
		
		@Override
		public double[] gradAt(List<Example> examples, double[] w) {
			double[] result = new double[w.length];
			
			
			for (Example ex : examples) {
					double[] pos = getDelta(ex.qnF, w);
					double[] neg = getDelta(ex.qmF, w);
					double v = Tools.sigmoid(rawValueAt(ex, w));
					for (int i = 0; i < w.length; i++) 
						result[i] += (pos[i] - neg[i]) * (1 - v);
				}
			
			
			return result;
		}


		
		
	};

	
	@Override
	public double getErrorRate(List<Example> l) {
		
		double sum = 0;
		
		for (Example ex : l) {
			double neg = computePair(ex.qmF, w);
			double pos = computePair(ex.qnF, w);
			if (pos <= neg)
				sum++;
		}
		
		return sum / l.size();
		
		
	
	}
	
	@Override
	public void learnWeights(List<Example> l) throws Exception {
		for (Example ex : l) 
			ex.updateFeatures(fdb);
		
		GradAscdEmb<Example> sgd = new GradAscdEmb<Example>(new F());
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
	
	
	public double[] getWeight() {
		return w;
	}
	
	
	protected double computePair(Feature ab, double[] w) {
		return ab.dotProd(w);
	}
	
	
	public GraphSimEmb_bak(FeatureDB fdb) {
		this.fdb = fdb;
	}
	
	@Override
	protected ScoreVector computeResult(int qid, Collection<Integer> candidates) {
		ScoreVector result = new ScoreVector();
		
		for (Integer nid : candidates) {
			Feature qn = fdb.get(qid, nid);
			double v = computePair(qn, w);
			result.put(nid, v);
		}

		return result;
	}

	@Override
	public String getName() {
		return "GSimEmb";
	}

	
}



