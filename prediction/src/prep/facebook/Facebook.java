package prep.facebook;

import utils.Config;

public class Facebook {
	
	public static final int[] egoIdSet = new int[] {0, 107, 348, 414, 686, 698, 1684, 1912, 3437, 3980};
	//public static final String sufFull = ".full";
	
	public static String getFileAttrName(int id) { return Config.getZDirRaw() + id + ".featnames"; };
	public static String getFileAttr(int id) { return Config.getZDirRaw() + id + ".feat"; };
	public static String getFileEgoAttr(int id) { return Config.getZDirRaw() + id + ".egofeat"; };
	
	public static String getFileConnections() { return Config.getZDirRaw() + "facebook_combined.txt"; };
	public static String getFileCircle(int id) { return Config.getZDirRaw() + id + ".circles"; };
	
	
	

}
