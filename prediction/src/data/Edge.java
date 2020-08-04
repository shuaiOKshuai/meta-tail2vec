package data;


public class Edge {
	
	
	public Node otherEnd;
	public double weight;
	//public boolean hidden;
	
	public Edge(Node otherEnd, double weight) {
		this.otherEnd = otherEnd;
		this.weight = weight;
		//this.hidden = false; 
	}

}
