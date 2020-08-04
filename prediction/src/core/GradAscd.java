package core;

import java.util.List;
import java.util.Random;

import utils.Config;
import utils.Pair;
import utils.Parallel;
import utils.Parallel.Operation;
import utils.Tools;


public class GradAscd<T> {

	protected double step;
	protected double epsilon;
	protected int numTrials;
	protected double factor_fw;
	protected double factor_bw;
	protected int max_iter;
	protected DiffFunc<T> f;
	protected long seed;
	protected boolean silent;
	
	public interface DiffFunc<T> {
		public int numDimensions();
		public double valueAt(List<T> examples, double[] w);
		public double[] gradAt(List<T> examples, double[] w);
	}
	
	
	public GradAscd(DiffFunc<T> f) {
		this.step = Config.GA_STEP;
		this.epsilon = Config.GA_EPSILON;
		this.numTrials = Config.GA_TRY;
		this.factor_fw = Config.GA_FW;
		this.factor_bw = Config.GA_BW;
		this.silent = Config.SILENT;
		this.max_iter = Config.GA_MAX_ITER;
		this.f = f;
		this.seed = 9251184432L;
	}
		
	protected double[] initW(Random rnd) {
		double[] w = new double[f.numDimensions()];
		for (int i = 0; i < w.length; i++)
			w[i] = rnd.nextDouble() * 10 - 5;

		return w;
	}
	
		
	protected double[] transform(double[] w) {
		double[] result = new double[w.length];
		for (int i = 0; i < w.length; i++)
			result[i] = Tools.sigmoid(w[i]);
		return result;
	}
	
	protected double[] invTransform(double[] w) {
		double[] result = new double[w.length];
		for (int i = 0; i < w.length; i++)
			result[i] = Tools.invSigmoid(w[i]);
		return result;
	}
	
	protected double[] transformGrad(double[] w) {
		double[] result = new double[w.length];
		for (int i = 0; i < w.length; i++) {
			double sig = Tools.sigmoid(w[i]);
			result[i] = (1 - sig) * sig;
		}
		return result;
	}
	
	
	public Pair<Double, double[]> maximizeOneTry(List<T> examples, double[] w0) {
		double[] w = w0.clone();
		double oldValue = - Double.MAX_VALUE;
		double newValue = - Double.MAX_VALUE;
		double[] oldW = null;
		
		int iter = 1;
		double factor = 1;
		while (iter <= max_iter) {
			double[] t = transform(w);
			double[] tg = transformGrad(w);

			newValue = f.valueAt(examples, t);
			if (!silent)
				System.out.printf("\tL = %15.5f, iter = %6d, factor = %10.5f\r", newValue, iter, factor);
			if (Math.abs(newValue - oldValue) / Math.abs(oldValue) < epsilon)
				break;
			
			//Bold Driver adaptable learning rate
			if (newValue > oldValue)
				factor *= factor_fw;
			else {
				factor *= factor_bw;
				w = oldW.clone();
				t = transform(w);
				tg = transformGrad(w);
			}
			double[] grad = f.gradAt(examples, t);
			oldW = w.clone();
			for (int j = 0; j < w.length; j++)
				w[j] = w[j] + step * grad[j] * tg[j] * factor; 
			
			oldValue = newValue;	
			iter ++;
		}
		if (!silent)
			System.out.printf("\tL = %15.5f, iter = %6d, factor = %10.5f\r\n", newValue, iter, factor);
		return new Pair<Double, double[]>(newValue, transform(w));
	}

	
	public Pair<Double, double[]> maximize(final List<T> examples) {
		final Object[] result = new Object[numTrials];
		
		try {
			int numCores = Math.min(numTrials, Config.MAX_THREADS);
			Parallel.forLoop(0, numTrials, numCores, new Operation<Integer>() {
				@Override
				public void perform(Integer trial) throws Exception {
					double[] w0 = initW(new Random(seed + trial * 7));
					result[trial] = maximizeOneTry(examples, w0);
				}
				
			});
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
		double maxValue = Double.NEGATIVE_INFINITY;
		double[] maxW = null;
		for (int i = 0; i < numTrials; i++) {
			@SuppressWarnings("unchecked")
			Pair<Double, double[]> vw = (Pair<Double, double[]>)result[i];
			double v = vw.key;
			double[] w = vw.value;
			if (v > maxValue) {
				maxValue = v;
				maxW = w;
			} 
		}		

		if (!silent)
			System.out.println("\tL = " + maxValue);
		return new Pair<Double, double[]>(maxValue, maxW);
		
	}

}
