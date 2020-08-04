package prep;

import data.Graph;
import utils.Config;

public class ExportGraph {

	public static void main(String[] args) throws Exception {
		Graph g = new Graph(); 
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
		g.saveToNode2Vec(Config.Z_FILE_GRAPH_EDGE + ".node2vec");
		g.saveToHin2Vec(Config.Z_FILE_GRAPH_EDGE + ".hin2vec");

	}

}
