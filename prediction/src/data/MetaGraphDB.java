package data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import utils.Pair;
import utils.TextReader;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class MetaGraphDB {
	
	private Map<Integer, MetaGraph> db;	
	private long numInstances;
	//private double[][] sim;
	
	public MetaGraph getMetaGraph(int gid) {
		return db.get(gid);
	}
	
//	public double getSimilarity(int i, int j) {
//		return sim[i][j];
//	}
	
	public void printStats() {
		System.out.println("# All = " + db.size());

		int count = 0;
		for (MetaGraph g : db.values())
			if (g.isPath())
				count ++;
		System.out.println("# Path = " + count);
		
		for (int n = 3; n <= 10; n++) {
			count = 0;
			for (MetaGraph g : db.values())
				if (g.numNodes() == n)
					count ++;
			System.out.println("# " + n + "-node = " + count);
		}
	}
	
	/*
	public void readSim(String filename) throws Exception {
		sim = new double[db.size()][db.size()];
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] split = line.split("\t");
			int i = Integer.parseInt(split[0]);
			int j = Integer.parseInt(split[1]);
			double s = Double.parseDouble(split[2]);
			sim[i][j] = s;
			sim[j][i] = s;
		}
		in.close();	
	}
	*/
	
	public void readTime(String filename) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			String[] split = line.split("\t");
			int id = Integer.parseInt(split[1]);
			long time = Long.parseLong(split[2]);
			getMetaGraph(id).time = time;
		}
		in.close();	
	}
	
	public void readDB(String filename) throws Exception {
		db = new THashMap<Integer, MetaGraph>();
		
		TextReader in = new TextReader(filename);
		MetaGraph g;
		while ( (g = new MetaGraph()).read(in) ) {
			db.put(g.id, g);
			if (g.numInstances() != -1)
				numInstances += g.numInstances();
		}
		
		in.close();
	}
	
	public long numInstances() {
		return numInstances;
	}
	
	public int numMetaGraph() {
		return db.size();
	}
	
	public Collection<MetaGraph> getMetaGraphs() {
		return db.values();
	}
	
	public class InstEdge extends Pair<Integer, Integer> {
		public InstEdge(int key, int value) {
			super(key, value);
		}
		
		@Override
		public boolean equals(Object x) {
			if (! (x instanceof InstEdge))
				return false;
			
			InstEdge a = (InstEdge)x;
			return (a.key == this.key) && (a.value == this.value);
		}
		
		@Override
		public int hashCode() {
			return key.hashCode() + value.hashCode() * 1709;			
		}
	}

	
	
	public class MetaGraph extends Graph {

		private int id;
		private long numInstances;
		private String text;
		private long time;
		private boolean sym;
		
		public boolean isPath()  {
			// check if its a path
			int countOne = 0;
			for (Node n : nodes.values()) {
				if (n.neighbors.size() > 2)
					return false;
				else if (n.neighbors.size() == 1) {
					countOne ++;
				}
			}
			
			return (countOne == 2);
		}
		
		public boolean isSymmetric() {
			return sym;
		}
		
		public int numOfTypes() {
			Set<Integer> types = new THashSet<Integer>();
			for (int i = 0; i < this.numNodes(); i++) 
				types.add(this.getNodeTypeId(i));
			return types.size();
		}
		
		public int numOfCoreNodes(int coreType) {
			int result = 0;
			for (int i = 0; i < this.numNodes(); i++)
				if (this.getNodeTypeId(i) == coreType)  
					result ++;
			return result;
		}
		
		public boolean isCoreSymmetric(int coreType) {
			// at least two core types have same color
			for (int i = 0; i < this.numNodes(); i++) {
				if (this.getNodeTypeId(i) == coreType && this.hasOtherNodeOfSameColor(i))
					return true;
			}

			return false;
		}
		
		public boolean hasDanglingNode(int coreType, boolean sym) {
			for (int i = 0; i < this.numNodes(); i++) {
				// a useful core is a node of core type, AND either no symmetry is required, or must have other node with same color
				boolean usefulCore = this.getNodeTypeId(i) == coreType && (!sym || this.hasOtherNodeOfSameColor(i));
				
				if (this.getNodeDegree(i) < 2 && !usefulCore)
					return true;
			}
			
			return false;
		}
		
		
		
		public Set<InstEdge> getInstanceEdges(int[] inst) {
			Set<InstEdge> result = new THashSet<InstEdge>();
			
			for (Node n : this.getNodes()) {
				for (Edge e : n.neighbors) {
					Node m = e.otherEnd;
					result.add(new InstEdge(inst[n.getId()], inst[m.getId()]));
				}
			}
			return result;			
		}
		
		public long getTime() {
			return time;
		}
		
		public int getId() {
			return id;
		}
		
		public String getTextRepresentation() {
			return text;
		}
		
		public long numInstances() {
			return numInstances;
		}
		
		public int getNodeDegree(int nindex) {
			return getNode(nindex).neighbors.size();
		}
		
		public int getNodeColor(int nindex) {
			return getNode(nindex).getColor();
		}
		
		public int getNodeTypeId(int nindex) {
			return getNode(nindex).getTypeId();
		}
		
		public boolean hasOtherNodeOfSameColor(int nindex) {
			int color = getNodeColor(nindex);
			
			for (int i = 0; i < this.numNodes(); i++) {
				int mColor = getNodeColor(i);
				if (i != nindex && mColor == color)
					return true;
			}
			return false;
		}
		

		public int[] readInstance(TextReader in) throws Exception {
			String line = in.readln();
			if (line == null)
				return null;
			
			int[] result = new int[numNodes()];
			String[] parts = line.split("\t");
			for (int i = 0; i < numNodes(); i++) 
				result[i] = Integer.parseInt(parts[i]);
			
			return result;
		}
		
		private String readline(TextReader in) throws Exception {
			String line = in.readln();
			text += line + "\n";
			return line;
		}
		
		public void updateId(int newId) {			
			this.text = this.text.replaceFirst("#\t" + this.id + "\t", "#\t" + newId + "\t");
			this.id = newId;
		}
		
		/*
		public double computeEntropy(int coreType, TextReader in) throws Exception {
			Map<Integer, Integer> count = new THashMap<Integer, Integer>();
			int z = 0;
			int[] inst;
			while ( (inst = readInstance(in)) != null) {
				for (int k = 0; k < this.numNodes(); k++)
					if (this.getNodeTypeId(k) == coreType) {
						Integer c = count.get(inst[k]);
						if (c == null)
							c = 0;
						count.put(inst[k], c + 1);
						z ++;
					}
			}
			double result = 0;
			for (int nid : count.keySet()) {
				double p = (double)count.get(nid) / (double)z;
				result -= p * Math.log(p);
			}
			return result;
		}
		*/
		
		public boolean read(TextReader in) throws Exception {
			text = "";
			
			// id
			String line = readline(in);
			if (line == null)
				return false;
			
			String[] parts = line.split("\t");
			if (!parts[0].equals("#"))
				throw new Exception("Incorrect input!");
			
			this.id = Integer.parseInt(parts[1]);
			
			// nodes, edges, colors 
			String[] nline = readline(in).split("\t");
			String[] eline = readline(in).split("\t");
			sym = readline(in).split("\t")[1].equals("1"); // skip symmetry line
			String[] cline = readline(in).split("\t");
			
			for (int i = 1; i < nline.length; i++) {
				String type = nline[i];
				int typeId = Integer.parseInt(type);
				int color = Integer.parseInt(cline[i]);
				Node n = new Node(i - 1, type, typeId, color);
				
				this.putNode(n);
			}
			
			//edges
			for (int i = 1; i < eline.length; i+=2) {
				Node from = nodes.get(Integer.parseInt(eline[i]));
				Node to = nodes.get(Integer.parseInt(eline[i+1]));
				from.neighbors.add(new Edge(to, 1.0));
			}

			
			//this.update();

			// frequency
			String freq = readline(in).split("\t")[1];
			// wenqing's program: if no frequency (submatch mode=3 to generate metagraph db without matching)
			// frequency will set to the number below, but this number cannot be parsed by Long
			if (freq.equals("18446744073709551615"))   
				this.numInstances = 0;
			else
				this.numInstances = Long.parseLong(freq);

			return true;
		}
		
		
			
	}

}
