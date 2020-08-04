package core;

public class Score implements Comparable<Score> {
	
	public int id;
	public double score;
	
	public Score(int id, double score) {
		this.id = id;
		this.score = score;
	}
	
	@Override
	public String toString() {
		return this.id + "\t" + this.score;
	}

	@Override
	public int compareTo(Score o) {
		//return - Double.compare(this.score, o.score);
		int compare = - Double.compare(this.score, o.score);
		if (compare != 0)
			return compare;
		else
			return Double.compare(this.id, o.id);
	}
		
}
