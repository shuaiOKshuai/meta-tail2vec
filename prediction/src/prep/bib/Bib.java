package prep.bib;

import java.util.Set;

import gnu.trove.set.hash.THashSet;
import utils.Config;

public class Bib {
	
	public static String getFileNode() { return Config.getZDirRaw() + "bibNodes.full"; };
	public static String getFileEdge() { return Config.getZDirRaw() + "bibEdges.full"; };
	
	public static String getFileNodeSub() { return Config.getZDirRaw() + "bibNodes.sub"; };
	public static String getFileEdgeSub() { return Config.getZDirRaw() + "bibEdges.sub"; };
	
	public static String[] areas = new String[] {"AI", "DM", "DB"};
	
	public static String[][] confs = new String[][] {
		new String[] {"IJCAI", "NIPS", "AAAI", "ECAI", "ICML", "COLT", "ECML"},
		new String[] {"KDD", "WWW", "ICDM", "PAKDD", "PKDD", "SDM", "WSDM"},
		new String[] {"ICDE", "VLDB", "SIGMOD Conference", "PODS", "EDBT", "ICDT"},
	};
	
	public static Set<String> confSet;
	
	static {
		confSet = new THashSet<String>();
		for (int i = 0; i < confs.length; i++)
			for (String conf : confs[i])
				confSet.add(conf);
	};
}
