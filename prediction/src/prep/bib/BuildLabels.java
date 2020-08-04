 package prep.bib;

import gnu.trove.map.hash.THashMap;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


import utils.Config;
import utils.TextReader;
import utils.TextWriter;
import data.Edge;
import data.Graph;
import data.Node;


public class BuildLabels {
	
	private static Graph g;
		
	private static Map<String, String> createAreaMap() {
		Map<String, String> result = new THashMap<String, String>();
		for (int i = 0; i < Bib.areas.length; i++) {
			for (String conf : Bib.confs[i])
				result.put(conf, Bib.areas[i]);
		}
		return result;
	}
	
	private static String getArea(Node n, Map<String, String> areaMap) {
		if (!n.getType().equals("author"))
			return null;
		
		Map<String, Integer> count = new THashMap<String, Integer>();
		for (String area : Bib.areas)
			count.put(area, 0);
		
		for (Edge e : n.neighbors) {
			Node m = e.otherEnd;
			for (Edge e2 : m.neighbors) {
				Node m2 = e2.otherEnd;
				if (m2.getType().equals("conf")) {
					String area = areaMap.get(m2.getContent());
					if (area != null)
						count.put(area, count.get(area) + 1);
					break;
				}
			}
		}
		
		//String maxArea = null;
		//int max = 0;
		//for (String area : Bib.areas) {
		//	if (count.get(area) > max) {
		//		max = count.get(area);
		//		maxArea = area;
		//	}
		//}
		
		//return maxArea;	
		for (String area : Bib.areas) {
			double frac = (double)count.get(area) / (double)n.neighbors.size();
			if (frac > 0.5)
				return area;
		}
		
		return null;
	}
	
	private static String formPair(int a, int b) {
		return a + "," + b;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("Loading graph...");
		g = new Graph();
		g.loadFromFile(Config.Z_FILE_GRAPH_NODE, Config.Z_FILE_GRAPH_EDGE);
		
		System.out.println("Generating labels...");
		Map<String, String> areaMap = createAreaMap();
		
		
		TextWriter out = new TextWriter(Config.getCSVLabelsNode());
		out.writeln("Author,Label");
		for (Node n : g.getNodes())
			if (n.getType().equals("author")) {
				String area = getArea(n, areaMap);
				if (area != null)
					out.writeln(n.getId() + "," + area);
			}
		out.close();
	
		System.out.println("Done.");
	}

}
