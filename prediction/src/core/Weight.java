package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.TextReader;
import utils.TextWriter;

public class Weight {
	
	private Map<Short, Double> map;
	
	public Weight() {
		map = new HashMap<Short, Double>();
	}
	
	public void assign(short gid, double weight) {
		map.put(gid, weight);
	}
	
	public double get(short gid) {
		return map.get(gid);
	}
	
	public void save(String filename) throws Exception {
		List<Score> l = new ArrayList<Score>();
		for (short gid : map.keySet()) {
			double w = map.get(gid);
			l.add(new Score(gid, w));
		}
		
		Collections.sort(l);
		TextWriter out = new TextWriter(filename);
		for (Score s : l)
			if (s.score > 0)
				out.writeln(s.toString());
		out.close();
	}
	
	public void read(String filename) throws Exception {
		TextReader in = new TextReader(filename);
		String line;
		while ( (line = in.readln()) != null) {
			short gid = Short.parseShort(line.split("\\t")[0]);
			double w = Double.parseDouble(line.split("\\t")[1]);
			assign(gid, w);
		}
		in.close();
	}
}
