package prep;

import java.util.Map;


import gnu.trove.map.hash.THashMap;
import utils.Config;
import utils.TextReader;
import utils.TextWriter;

public class BuildCSVFromEmb {
	
	private static String generateHeader(String prefix, int n) {
		String result = "";
		for (int i = 0; i < n; i++)
			result += prefix + i + ",";
		
		return result;
	}
	
	private static Map<Integer, double[]> loadEmbeddings_mg2vec(String filename) throws Exception {
		TextReader in = new TextReader(filename);
		String[] header = in.readln().split(" ");
		int dims = Integer.parseInt(header[1]);
		Map<Integer, double[]> embMap_h = new THashMap<Integer, double[]>();
		Map<Integer, double[]> embMap_t = new THashMap<Integer, double[]>();
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split(" ");
			if (parts[0].equals("</s>") || parts[0].startsWith("m"))
				continue;
			int nid = Integer.parseInt(parts[0].substring(0, parts[0].length() - 2));
			double[] emb = new double[dims];
			for (int d = 0; d < dims; d ++)
				emb[d] = Double.parseDouble(parts[d + 1]) ;
			
			if (parts[0].endsWith("_h")) 
				embMap_h.put(nid, emb);
			else if (parts[0].endsWith("_t")) 
				embMap_t.put(nid, emb);			
		}
		in.close();
		
		Map<Integer, double[]> embMap = new THashMap<Integer, double[]>();
		for (int nid : embMap_h.keySet()) {
			double[] emb_h = embMap_h.get(nid);
			double[] emb_t = embMap_t.get(nid);
			//double[] emb = new double[dims];
			//for (int d = 0; d < dims; d ++)
			//	emb[d] = emb_h[d] + emb_t[d];
			
			
			double[] emb = new double[dims * 2];
			for (int d = 0; d < dims; d ++)
				emb[d] = emb_h[d];
			for (int d = 0; d < dims; d ++)
				emb[d + dims] = emb_t[d];
			
			embMap.put(nid, emb);
		}
		return embMap;
	}
	
	private static Map<Integer, double[]> loadEmbeddings(String filename) throws Exception {
		TextReader in = new TextReader(filename);
		String[] header = in.readln().split(" ");
		//int nodes = Integer.parseInt(header[0]);
		int dims = Integer.parseInt(header[1]);
		Map<Integer, double[]> embMap = new THashMap<Integer, double[]>();
		String line;
		while ((line = in.readln()) != null) {
			String[] parts = line.split(" ");
			if (parts[0].equals("</s>") || parts[0].startsWith("m"))
				continue;
			int nid = Integer.parseInt(parts[0]);
			double[] emb = new double[dims];
			embMap.put(nid, emb);
			for (int d = 0; d < dims; d ++)
				emb[d] = Double.parseDouble(parts[d + 1]) ;
			
		}
		in.close();
		
		return embMap;
	}
	
	
		
		
	private static void writeCSVLink(Map<Integer, double[]> embMap, String method) throws Exception {
	
		TextWriter out = new TextWriter(Config.getCSVLink(method));
		
		int dims = 0;
		for (double[] v : embMap.values()) {
			dims = v.length;
			break;
		}
		
		//header
		out.write("Id,");
		out.write(generateHeader("D", dims * 2));
		out.writeln("Label");
		//end of header
		
		TextReader in = new TextReader(Config.getCSVLabelsLink());
		in.readln(); // ignore header
		String line;
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split(",");
			int a1 = Integer.parseInt(splits[0]);
			int a2 = Integer.parseInt(splits[1]);
			String label = splits[2];
			out.write(a1 + "-" + a2 + ",");
			double[] emb1 = embMap.get(a1);
			double[] emb2 = embMap.get(a2);
			
			
			//for (int i = 0; i < dims; i++) 
			//	out.write((emb1 == null ? 0 : emb1[i]) + (emb2 == null ? 0 : emb2[i]) + ","); 
			if (emb1 == null || emb2 == null)
				System.out.print("+");
			
			
			for (int i = 0; i < dims; i++)
				out.write((emb1 == null ? 0 : emb1[i]) + ","); 
			for (int i = 0; i < dims; i++)
				out.write((emb2 == null ? 0 : emb2[i]) + ","); 
			
			
			out.write(label);
			out.writeln();
		}
			
			in.close();
			out.close();
	}
	
	private static void writeCSVNode(Map<Integer, double[]> embMap, String method) throws Exception {
		
		TextWriter out = new TextWriter(Config.getCSVNode(method));
		
		int dims = 0;
		for (double[] v : embMap.values()) {
			dims = v.length;
			break;
		}
		
		//header
		out.write("Id,");
		out.write(generateHeader("D", dims));
		out.writeln("Label");
		//end of header
		
		TextReader in = new TextReader(Config.getCSVLabelsNode());
		in.readln(); // ignore header
		String line;
		while ( (line = in.readln()) != null ) {
			String[] splits = line.split(",");
			int a = Integer.parseInt(splits[0]);
			String label = splits[1];
			out.write(a + ",");
			
			double[] emb = embMap.get(a);
			if (emb == null)
				System.out.print("+");
			for (int i = 0; i < dims; i++)
				out.write((emb == null ? 0 : emb[i]) + ","); 
			
			out.write(label);
			out.writeln();
		}
			
		in.close();
		out.close();
	}
	
	
		
	
	public static void main(String[] args) throws Exception {
		String method = args[0];
		String filename = Config.DIR_CSV + "/" + args[0] + ".emb";
		
		
		
		
		System.out.println("Generating CSV ...");
		
		Map<Integer, double[]> embMap;
		//if (method.startsWith("m2v"))
		//	embMap = loadEmbeddings_mg2vec(filename);
		//else
			embMap = loadEmbeddings(filename);
		
		writeCSVLink(embMap, method); 
		//writeCSVNode(embMap, method); 
		
		System.out.println("Done.");
	}
}
