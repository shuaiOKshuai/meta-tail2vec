package prep;

import java.io.File;

import utils.Config;
import utils.Tools;

public class ProcGraph {
	
	private static void callLib(String fIdMap, String fNames) throws Exception {
		// prepare and run process
		ProcessBuilder pb = new ProcessBuilder(Config.LIB_PROC, 
				"55", 
				Config.Z_FILE_GRAPH_NODE,
				Config.Z_FILE_GRAPH_EDGE,
				Config.FILE_GRAPH,
				Config.Z_FILE_GRAPH_INFO,
				fIdMap,
				fNames
				);
		Tools.startProcessAndWaitFor(pb, null, true);
	}
	
	public static void main(String[] args) throws Exception {
		String fIdMap = Config.Z_FILE_GRAPH_INFO + ".idmap";
		String fNames = Config.Z_FILE_GRAPH_INFO + ".names";
		callLib(fIdMap, fNames);
		
		new File(fIdMap).delete();
		new File(fNames).delete();
		System.out.println("Done.");	
		
	}

}
