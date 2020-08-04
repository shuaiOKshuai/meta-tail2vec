package utils;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.stat.inference.TTest;

public class Stats extends ArrayList<Double> {

	private static final long serialVersionUID = -8067861098264515463L;

	public double getMean() {
		double sum = 0;
		for (double d : this)
			sum += d;
		return sum / this.size();
	}
	
	public double getPairedTestPValue(Stats s2) throws Exception {
		if (this.size() != s2.size())
			throw new Exception("Different array length!");
		
		TTest t = new TTest();
		return t.pairedTTest(this.toDoubleArray(), s2.toDoubleArray());
	}
	
	public double getPearsonCorr(Stats s2) throws Exception {
		if (this.size() != s2.size())
			throw new Exception("Different array length!");
		
		double m1 = this.getMean();
		double m2 = s2.getMean();
		double sd1 = this.getStdDev();
		double sd2 = s2.getStdDev();
		
		Stats prod = new Stats();
		for (int i = 0; i < this.size(); i++)
			prod.add(this.get(i) * s2.get(i));
		double m12 = prod.getMean();
		
		return (m12 - m1 * m2) / (sd1 * sd2);
		
	}
	
	public Stats duplicate() {
		Stats result = new Stats();
		for (double d : this) 
			result.add(d);
		return result;
	}
	
	public double getStdDev() {
		Stats sq = new Stats();
		for (double d : this)
			sq.add(d * d);
		double m = getMean();
		return Math.sqrt(sq.getMean() - m * m);
	}
	
	public double getMax() {
		return Collections.max(this);
	}
	
	public double getMin() {
		return Collections.min(this);
	}
	
	public double[] toDoubleArray() {
		double[] result = new double[this.size()];
		for (int i = 0; i < this.size(); i++)
			result[i] = this.get(i);
		return result;
	}
}

