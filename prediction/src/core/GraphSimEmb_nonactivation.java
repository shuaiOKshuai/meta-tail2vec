package core;

import java.util.List;

import core.GradAscd.DiffFunc;
import utils.Tools;
import data.Example;
import data.Feature;
import data.FeatureDB;



public class GraphSimEmb_nonactivation extends GraphSim {
	
	protected double lambda = 0;//0.0001;
	
	public class F implements DiffFunc<Example> {
		
		@Override
		public int numDimensions() {
			return fdb.numDimensions();
		}

		protected double rawValueAt(Example ex, double[] w) {
			double pos = computePair(ex.qnF, null, null, w);
			double neg = computePair(ex.qmF, null, null, w);
			return pos - neg;
		}
		
		@Override
		public double valueAt(List<Example> examples, double[] w) {
			double result = 0;
			for (Example ex : examples) {
				result += Math.log(Tools.sigmoid(rawValueAt(ex, w)));
			}
			
			double reg = 0;
			for (int i = 0; i < w.length; i++) 
				reg += w[i] * w[i];
			//	reg += Math.abs(w[i]);
			
			return result / examples.size() - reg * lambda;
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
			
			
			for (int i = 0; i < w.length; i++) { 
				result[i] /= examples.size();
				result[i] -= lambda * 2 * w[i]; 
				/*
				if (w[i] > 0) 
					result[i] -= lambda;
				else if (w[i] < 0)
					result[i] -= -lambda;
				*/
			}
			
			return result;
		}


		
		
	};

	
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}
	
	
	@Override
	public void learnWeights(List<Example> l) throws Exception {
		for (Example ex : l) 
			ex.updateFeatures(fdb);
		
		GradAscdEmb<Example> sgd = new GradAscdEmb<Example>(new F());
		w = sgd.maximize(l).value;
	}
	
	
	@Override
	protected double computePair(Feature ab, Feature a, Feature b, double[] w) {
		return ab.dotProd(w);
	}
	
	
	public GraphSimEmb_nonactivation(FeatureDB fdb) {
		super(fdb);
	}
	

	@Override
	public String getName() {
		return "GSimEmb";
	}

	
}



