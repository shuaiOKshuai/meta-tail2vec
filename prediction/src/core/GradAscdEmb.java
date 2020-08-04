package core;


public class GradAscdEmb<T> extends GradAscd<T> {

	public GradAscdEmb(DiffFunc<T> f) {
		super(f);
	}
		
	@Override
	protected double[] transform(double[] w) {
		return w.clone();
	}
	
	@Override
	protected double[] invTransform(double[] w) {
		return w.clone();
	}
	
	@Override
	protected double[] transformGrad(double[] w) {
		double[] result = new double[w.length];
		for (int i = 0; i < w.length; i++) {
			result[i] = 1;
		}
		return result;
	}

}
