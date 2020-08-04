package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



public class ScoreVector extends HashMap<Integer, Double> {

	private static final long serialVersionUID = 1L;

	/*
	public double getSum() {
		double sum = 0;
		for (Double d : this.values())
			sum += d;
		return sum;
	}
	*/
	
	public List<Score> toSortedScoreList() {
		List<Score> l = new ArrayList<Score>();
		for (int id : this.keySet()) 
			l.add(new Score(id, this.get(id)));
		
		Collections.sort(l);
		return l;
	}
}
