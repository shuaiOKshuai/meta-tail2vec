package prep.linkedin;

import utils.Config;

public class LinkedIn {
	
	public static final String[] attrNameSet = new String[] {"college", "employer", "location"};
	
	
	public static String getFileAttr(String attrName) { return Config.getZDirRaw() + attrName + ".txt"; };
	public static String getFileLabel() { return Config.getZDirRaw() + "label.txt"; };
	public static String getFileConnections() { return Config.getZDirRaw() + "network.txt"; };
	
	public static int parseUserId(String s) { return Integer.parseInt(s.substring(1)); }; 
}
