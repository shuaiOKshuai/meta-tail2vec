package data;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.Feature.FK;
import data.MetaGraphDB.MetaGraph;
import utils.TextReader;
import utils.TextWriter;

public class FeatureDB {
	
	private boolean sym;
	private Map<FK, Feature> db;
	private Map<FIDKey, Integer> id;
	private Map<Integer, FIDKey> reverseId;
	private int autoId;
	//private String lastFilename = null;
	
	
	public class FIDKey {

		private int gid;
		private int qColor;
		private int nColor;
		
		public FIDKey(int gid, int qColor, int nColor) {
			this.gid = gid;
			if (!sym || qColor >= nColor) {
				this.qColor = qColor;
				this.nColor = nColor;
			}
			else {
				this.qColor = nColor;
				this.nColor = qColor;
			}
		}
		
		
		@Override
		public boolean equals(Object x) {
			if (! (x instanceof FIDKey))
				return false;
			
			FIDKey o = (FIDKey)x;
			return o.gid == this.gid && o.qColor == this.qColor && o.nColor == this.nColor;
		}
		
		@Override
		public int hashCode() {
			return gid + qColor * 107 + nColor * 7907;			
		}
		
		@Override
		public String toString() {
			return gid + "-" + qColor + "-" + nColor;
		}
		
	}
	
	public boolean isSym() {
		return sym;
	}
	
	
	public boolean isEmpty() {
		return db.isEmpty();
	}
	
	public int size() {
		return db.size();
	}
	
	public void clear() {
		db.clear();
	}
	
	public Collection<Feature> getAllFeatures() {
		return Collections.unmodifiableCollection(db.values());
	}
	
	public int getGid(int fid) {
		return reverseId.get(fid).gid;
	}
	
	public int numDimensions() {
		return id.size();
	}
	
	public List<Integer> getGidList() {
		Set<Integer> gSet = new THashSet<Integer>();
		for (FIDKey k : reverseId.values())
			gSet.add(k.gid);
		List<Integer> l = new ArrayList<Integer>();
		l.addAll(gSet);
		Collections.sort(l);
		return Collections.unmodifiableList(l);
	}
	
	public boolean containsFIDKey(FIDKey fidKey) {
		return id.containsKey(fidKey);
	}
	
	public boolean containFeature(Feature f) {
		return db.containsKey(f.getKey());
	}
	
	public FeatureDB(boolean sym) {
		db = new THashMap<FK, Feature>();
		this.sym = sym;
		this.autoId = 0;
		id = new THashMap<FIDKey, Integer>();
		reverseId = new THashMap<Integer, FIDKey>();
	}
	
	public void add(Feature f) {
		db.put(f.getKey(), f);
	}
	
	public void transform() {
		for (Feature f : db.values())
			f.transform();
	}
	
	/*
	public FIDKey getFIDKey(int fid) {
		return reverseId.get(fid);
	}
	*/
	
	public Set<Integer> getFidSet() {
		return Collections.unmodifiableSet(reverseId.keySet());
	}
	
	
	public FeatureDB generateNewRetainPathOnly(MetaGraphDB gdb) {
		Set<Integer> gSet = new THashSet<Integer>();
		for (MetaGraph g : gdb.getMetaGraphs()) 
			if (g.isPath())
				gSet.add(g.getId());
		return generateNewRetainOnly(gSet);
	}
	
	public FeatureDB generateNewRetainOnly(Set<Integer> gSet) {
		FeatureDB newFdb = new FeatureDB(sym);
		
		Map<Integer, Integer> conversion = new THashMap<Integer, Integer>(); // old fid, new fid
		for (FIDKey fidKey : id.keySet()) {
			if (gSet.contains(fidKey.gid)) {
				int oldFid = id.get(fidKey);
				int newFid = newFdb.autoId;
				conversion.put(oldFid, newFid);
				newFdb.autoId ++;
				newFdb.id.put(fidKey, newFid);
				newFdb.reverseId.put(newFid, fidKey);
			}
		}

		for (FK fk : db.keySet()) {
			Feature oldF = db.get(fk);
			Feature newF = new Feature(fk, sym);
			for (int oldFid : oldF.getNonEmptyFidSet()) 
				if (conversion.containsKey(oldFid)) {
					int newFid = conversion.get(oldFid);
					double value = oldF.get(oldFid);
					newF.set(newFid, value);
				}	
			newFdb.db.put(fk, newF);
		}
		return newFdb;
	}
	
	public FeatureDB generateNewNonAnchoredMetagraph() {
		FeatureDB newFdb = new FeatureDB(sym);
		
		Map<Integer, Integer> map = new THashMap<Integer, Integer>(); // gid, newFid
		for (int gid : this.getGidList()) {
			int newFid = newFdb.autoId;
			map.put(gid, newFid);
			newFdb.autoId ++;
			FIDKey fidKey = new FIDKey(gid, -1, -1);
			newFdb.id.put(fidKey, newFid);
			newFdb.reverseId.put(newFid, fidKey);
		}
		
		for (FK fk : db.keySet()) {
			Feature oldF = db.get(fk);
			Feature newF = new Feature(fk, sym);
			for (int oldFid : oldF.getNonEmptyFidSet()) {
				int gid = this.getGid(oldFid);
				int newFid = map.get(gid);
				double value = oldF.get(oldFid);
				newF.inc(newFid, value);
			}
			newFdb.db.put(fk, newF);
		}		
		
		return newFdb;
	}
	
	
	public Feature get(int nid, int mid) {
		Feature dummy = new Feature(nid, mid, sym);
		return db.get(dummy.getKey());
	}
	
	
	public boolean hasDanglingNode(MetaGraph g, int i, int j) {
		for (int k = 0; k < g.numNodes(); k++)  
			if (k != i & k != j & g.getNodeDegree(k) < 2)
				return true;
		return false;
	}
	
	
	private void addFid(int gid, int qColor, int nColor) {
		FIDKey fidKey = new FIDKey(gid, qColor, nColor);
		Integer fid = id.get(fidKey);		
		if (fid == null) {
			fid = autoId;
			id.put(fidKey, fid);
			reverseId.put(fid, fidKey);
			autoId ++;
		}
	}
	
	
	public void removeEmptyFid() {
		Set<Integer> remove = new THashSet<Integer>();
		for (int fid : id.values()) {
			boolean empty = true;
			for (Feature f : db.values())
				if (f.get(fid) > 0) {
					empty = false;
					break;
				}
			if (empty)
				remove.add(fid);
		}
		
		for (int fid : remove) {
			FIDKey fidKey = reverseId.get(fid);
			reverseId.remove(fid);
			id.remove(fidKey);
		}
	}
	
	
	public void createAllFid(MetaGraphDB gdb, int coreType) {
		for (MetaGraph g : gdb.getMetaGraphs()) {
			// pair
			for (int i = 0; i < g.numNodes() - 1; i ++) {
				if (g.getNodeTypeId(i) != coreType)
					continue;
				int ci = g.getNodeColor(i);
				for (int j = i + 1; j < g.numNodes(); j++) {
					if (g.getNodeTypeId(j) != coreType)
						continue;	
					if (hasDanglingNode(g, i, j))
						continue;
					int cj = g.getNodeColor(j);
					if (sym) {
						if (ci == cj)
							addFid(g.getId(), ci, cj);
					} 
					else {
						addFid(g.getId(), ci, cj);
						addFid(g.getId(), cj, ci);
					}			
				}
			}
				
			// single node
			for (int i = 0; i < g.numNodes(); i++) {
				if (g.getNodeTypeId(i) != coreType)
					continue;
				int ci = g.getNodeColor(i);
				boolean symI = false;
				Set<Integer> unsymColors = new THashSet<Integer>();
				
				for (int j = 0; j < g.numNodes(); j++) {
					if (g.getNodeTypeId(j) != coreType)
						continue;
					if (i == j)
						continue;
					if (hasDanglingNode(g, i, j))
						continue;
					
					int cj = g.getNodeColor(j);
					if (sym) {
						if (ci == cj)
							symI = true;
					}
					else 
						unsymColors.add(cj);
				}
				
				if (sym) {
					if (symI)
						addFid(g.getId(), ci, ci);
				}
				else {
					for (int cj : unsymColors) {
						addFid(g.getId(), ci, cj);
						addFid(g.getId(), cj, ci);
					}					
				}
			}
		}
	}
	
	public void fill(MetaGraph g, int[] instance, int coreType) throws Exception {
		
		// for pair of nodes
		for (int i = 0; i < instance.length - 1; i++) {
			if (g.getNodeTypeId(i) != coreType)
				continue;
			int ci = g.getNodeColor(i);
			for (int j = i + 1; j < instance.length; j++) {
				if (g.getNodeTypeId(j) != coreType)
					continue;				
				if (hasDanglingNode(g, i, j))
					continue;
				
				int cj = g.getNodeColor(j);
				if (sym) {
					if (ci == cj)
						fill(g.getId(), instance[i], instance[j], ci, cj);
				} 
				else {
					fill(g.getId(), instance[i], instance[j], ci, cj);
					fill(g.getId(), instance[j], instance[i], cj, ci);
				}			
			}
		}
		
		// for single node 
		for (int i = 0; i < instance.length; i++) {
			if (g.getNodeTypeId(i) != coreType)
				continue;
			int ci = g.getNodeColor(i);
			boolean symI = false;
			Set<Integer> unsymColors = new THashSet<Integer>();
			
			for (int j = 0; j < instance.length; j++) {
				if (g.getNodeTypeId(j) != coreType)
					continue;
				if (i == j)
					continue;
				if (hasDanglingNode(g, i, j))
					continue;
				
				int cj = g.getNodeColor(j);
				if (sym) {
					if (ci == cj)
						symI = true;
				}
				else 
					unsymColors.add(cj);
			}
			
			if (sym) {
				if (symI)
					fill(g.getId(), instance[i], -1, ci, ci);
			}
			else {
				for (int cj : unsymColors) {
					fill(g.getId(), instance[i], -1, ci, cj);
					fill(g.getId(), -1, instance[i], cj, ci);
				}					
			}
		}
	}
	
	private void fill(int gid, int qid, int nid, int qColor, int nColor) {
		Feature feat = this.get(qid, nid);
		if (feat == null) 
			return;		
		
		FIDKey fidKey = new FIDKey(gid, qColor, nColor);
		Integer fid = id.get(fidKey);		
		
		synchronized (feat) { 
			feat.inc(fid);
		}
		
	}
	
	/*
	private synchronized void fill(int gid, int qid, int nid, int qColor, int nColor) {
		Feature feat = this.get(qid, nid);
		if (feat == null) 
			return;		
		
		FIDKey fidKey = new FIDKey(gid, qColor, nColor);
		Integer fid = id.get(fidKey);
				
		
		if (fid == null) {
			fid = autoId;
			id.put(fidKey, fid);
			reverseId.put(fid, fidKey);
			autoId ++;
		}			
	
		feat.inc(fid);
		
	}
	*/
	
	public void initEmbAsFeatures(int dims) {
		for (int d = 0; d < dims; d++) {
			FIDKey fidKey = new FIDKey(d, 0, 0);
			id.put(fidKey, d);
			reverseId.put(d, fidKey);
		}
	}
	
	
	/*
	public void save(String filename) throws Exception {
		TextWriter out = new TextWriter(filename);
		
		// need to save FIDKey
		out.writeln(numDimensions());
		for (FIDKey fidKey : id.keySet()) {
			int fid = id.get(fidKey);
			out.writeln(fid + "\t" + fidKey.gid + "\t" + fidKey.qColor + "\t" + fidKey.nColor);
		}
		
		// save features
		for (Feature f : db.values())
			out.writeln(f.toString());
		
		out.close();
	}
	*/
	
	public void save(String filename, boolean append) throws Exception {
		TextWriter out = new TextWriter(filename, append);
		
		if (!append) {
			// need to save FIDKey
			out.writeln(numDimensions());
			for (FIDKey fidKey : id.keySet()) {
				int fid = id.get(fidKey);
				out.writeln(fid + "\t" + fidKey.gid + "\t" + fidKey.qColor + "\t" + fidKey.nColor);
			}
		}
		
		// save features
		for (Feature f : db.values())
			out.writeln(f.toString());
		
		out.close();
	}
	
	public void load(String filename) throws Exception {
		db.clear();
		id.clear();
		reverseId.clear();
		
		TextReader in = new TextReader(filename);
		
		int numDim = Integer.parseInt(in.readln());
		for (int i = 0; i < numDim; i++) {
			String[] splits = in.readln().split("\t");
			int fid = Integer.parseInt(splits[0]);
			int gid = Integer.parseInt(splits[1]);
			int nPos = Integer.parseInt(splits[2]);
			int mPos = Integer.parseInt(splits[3]);
			FIDKey fidKey = new FIDKey(gid, nPos, mPos);
			id.put(fidKey, fid);
			reverseId.put(fid, fidKey);
			if (fid >= autoId)
				autoId = fid + 1;
		}
		
		String line;
		while ( (line = in.readln()) != null) 
			add(new Feature(line, sym));
		
		in.close();
		
		//lastFilename = filename;
	}
	
	/*
	public void reload() throws Exception {
		load(lastFilename);
	}
		*/
}
