package data;

public class Example implements Comparable<Example> {

	
	public int qid;
	public int nid;
	public int mid;
	
	public Feature qF;
	public Feature nF;
	public Feature mF;
	public Feature qnF;
	public Feature qmF;
	
	public double nScore;
	public double mScore;
	
	public Example(int qid, int nid, int mid, FeatureDB fdb) {
		this.qid = qid;
		this.nid = nid;
		this.mid = mid;	
		if (fdb != null)
			updateFeatures(fdb);
	}
	
	public void updateFeatures(FeatureDB fdb) {
		this.qF = fdb.get(qid, -1);
		this.nF = fdb.get(-1, nid);
		this.mF = fdb.get(-1, mid);
		this.qnF = fdb.get(qid, nid);
		this.qmF = fdb.get(qid, mid);
		
		//if (qF == null) qF = new Feature(qid, -1, fdb.isSym());
		//if (nF == null) nF = new Feature(-1, nid, fdb.isSym());
		//if (mF == null) mF = new Feature(-1, mid, fdb.isSym());
		//if (qnF == null) qnF = new Feature(qid, nid, fdb.isSym());
		//if (qmF == null) qmF = new Feature(qid, mid, fdb.isSym());
	}
	


	@Override
	public int compareTo(Example x) {
		if (qid > x.qid) 
			return 1;
		else if (qid == x.qid && nid > x.nid)
			return 1;
		else if (qid == x.qid && nid == x.nid && mid > x.mid)
			return 1;
		else if (qid == x.qid && nid == x.nid && mid == x.mid)
			return 0;
		else
			return -1;
	}


	
}
