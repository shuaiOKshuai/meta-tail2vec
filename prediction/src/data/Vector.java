package data;




public class Vector {

	private double[] vector;
		
	public Vector(int dim) {
		vector = new double[dim];
	}
	
	public Vector(double[][] emb, int qid, int nid) {
		this(emb[0].length * 2);
		int half_d = emb[0].length;
		for (int i = 0; i < half_d; i++) {
			vector[i] = emb[qid][i];
			vector[i + half_d] = emb[nid][i];
		}
	}
	
	public double get(int i) {
		return vector[i];
	}
	
	public void set(int i, double value) {
		vector[i] = value;
	}
	
	public double dotProd(double[] w) {
		double sum = 0;
		for (int i = 0; i < vector.length; i++) 
			sum += vector[i] * w[i];
		
		return sum;
	}
	
	
	
}
