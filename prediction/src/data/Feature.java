package data;




import java.util.Set;

import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.set.hash.THashSet;



public class Feature {

	private TIntFloatMap vector;
	private FK key;
	private boolean sym;
	
	public class FK {

		private int nid;
		private int mid;
		
		public FK(int nid, int mid) {
			if (!sym || nid >= mid) {
				this.nid = nid;
				this.mid = mid;
			}
			else {
				this.nid = mid;
				this.mid = nid;
			}
		}
		
		public int getNid() {
			return nid;
		}
		
		public int getMid() {
			return mid;
		}
		
		
		@Override
		public boolean equals(Object x) {
			if (! (x instanceof FK))
				return false;
			
			FK o = (FK)x;
			return o.nid == this.nid && o.mid == this.mid;
		}
		
		@Override
		public int hashCode() {
			return nid + mid * 7907;			
		}		
		
		@Override
		public String toString() {
			return nid + "\t" + mid;			
		}

	}
	
	public boolean isSym() {
		return sym;
	}
	
	public FK getKey() {
		return key;
	}
	
	public boolean isEmpty() {
		return vector.isEmpty();
	}
	
	private void init(int nid, int mid, boolean sym) {
		this.sym = sym;
		vector = new TIntFloatHashMap(4);
		this.key = new FK(nid, mid);
	}
	
	public Feature(int nid, int mid, boolean sym) {
		init(nid, mid, sym);
	}
	
	public Feature(FK fk, boolean sym) {
		init(fk.nid, fk.mid, sym);
	}
	
	public Feature(String line, boolean sym) {
		String[] splits = line.split("\t");
		init(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), sym); 
		
		for (int i = 2; i < splits.length; i++) {
			String s = splits[i];
			int colon = s.indexOf(":");
			int fid = Integer.parseInt(s.substring(0, colon));
			double v = Double.parseDouble(s.substring(colon + 1));
			set(fid, v);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(key.nid);
		sb.append("\t");
		sb.append(key.mid);
		
		for (int fid : vector.keys()) {
			sb.append("\t");
			sb.append(fid);
			sb.append(":");
			sb.append(get(fid));

		}
		return sb.toString();
	}
	
	public double get(int fid) {
		return vector.get(fid);
	}
	
	public void set(int fid, double value) {
		vector.put(fid, (float)value);
	}
	
	public void inc(int fid, double delta) {
		vector.adjustOrPutValue(fid, (float)delta, (float)delta);
	}
	
	public void inc(int fid) {
		vector.adjustOrPutValue(fid, 1, 1);
	}
	
	public Set<Integer> getNonEmptyFidSet() {
		Set<Integer> result = new THashSet<Integer>();
		for (int fid : vector.keys())
			result.add(fid);
		return result;
	}
	
	public double dotProd(double[] w) {
		double sum = 0;
		for (int fid : vector.keys()) 
			sum += get(fid) * w[fid];
		
		return sum;
	}
	
	public void transform() { 
		for (int fid : vector.keys()) 
			vector.put(fid, (float)Math.log1p(vector.get(fid)));
	}
	
}
