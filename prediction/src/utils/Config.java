package utils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Properties;

public class Config {
	
	
	public static String LIB_SUBMATCH = "lib/SubMatch.exe";
	public static String LIB_PROC = "lib/ProcDatasets.exe";
	public static String DIR_OUT = "dblp/out";	
	public static String DIR_SPLITS = "dblp/splits";	
	public static String DIR_CSV = "dblp/csv";
	public static String DIR_METAGRAPH_INSTANCE = "dblp/metagraph.inst";
	public static String Z_DIR_RAW = "dblp/raw";
	
	private static String dirHelper(String dir) {
		Tools.createDirs(dir); 
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		return dir;
	}
	
	public static String getDirOut() { return dirHelper(DIR_OUT); };
	public static String getDirSplits() { return dirHelper(DIR_SPLITS); };
	public static String getDirCSV() { return dirHelper(DIR_CSV); };
	public static String getDirMetagraphInstance() { return dirHelper(DIR_METAGRAPH_INSTANCE); };
	public static String getZDirRaw() { return dirHelper(Z_DIR_RAW); };
	
	public static String FILE_METAGRAPH_QUERY 	= "dblp/metagraph.q";
	public static String FILE_METAGRAPH_DB 		= "dblp/metagraph.db";
	public static String FILE_METAGRAPH_TIMELOG	= "dblp/metagraph.t";
	public static String FILE_METAGRAPH_SIM		= "dblp/metagraph.sim";
	public static String FILE_GRAPH 			= "dblp/graph.lg";
	public static String FILE_PAIR_DB			= "dblp/pair.db";
	public static String FILE_FEATURE_DB 		= "dblp/feature.db"; 
	public static String FILE_GROUNDTRUTH_DB 	= "dblp/groundtruth.db";
	
	public static String Z_FILE_GRAPH_NODE		= "dblp/graph.node";
	public static String Z_FILE_GRAPH_EDGE		= "dblp/graph.edge";
	public static String Z_FILE_TABLE			= "dblp/table";
	public static String Z_FILE_GRAPH_INFO		= "dblp/graph.info";
	
	public static boolean	LOG_FEATURE				= true;
	public static boolean 	SYMMETRY				= false;	
	public static int 		CORE_TYPE				= 0;
	public static String	CORE_TYPE_STR			= "";
	public static double 	FRACTION_TEST_QUERIES	= 0.8; 
	
	
	public static int		MAX_THREADS				= 5; 
	public static int 		MAX_FREQ				= 100_000_000;
	public static boolean	SILENT					= true;
		
	public static double 	MU 			= 5;
	public static double	LAMBDA		= 0.1;
	public static int		GA_MAX_ITER	= 10000;
	public static double 	GA_STEP		= 1; 
	public static double 	GA_EPSILON 	= 1E-5;
	public static int 		GA_TRY		= 5; 
	public static double 	GA_FW		= 1.01;
	public static double 	GA_BW		= 0.5;
	
	public static String getFileMetagraphInstance(int gid) { return getDirMetagraphInstance() + gid; };
	public static String getFileMetagraphQueryPruned() { return FILE_METAGRAPH_QUERY + ".pruned"; };
	public static String getFileTrain(String simType, int split) {	return getDirSplits() + "train_" + simType.toLowerCase() + "_" + split; };
	public static String getFileTest(String simType, int split) { return getDirSplits() + "test_" + simType.toLowerCase() + "_" + split; };
	public static String getFileIdeal(String simType) {	return getDirSplits() + "ideal_" + simType.toLowerCase(); };
	public static String getCSVNodeClassification() {	return getDirCSV() + "node_classification.csv"; };
	public static String getCSVNode(String method) {	return getDirCSV() + "node_" + method + ".csv"; };
	public static String getCSVLink(String method) {	return getDirCSV() + "link_" + method + ".csv"; };
	//public static String getCSVLabels() { return getDirCSV() + "labels.csv"; };
	public static String getCSVLabelsNode() {	return getDirCSV() + "node_labels.csv"; };
	public static String getCSVLabelsLink() {	return getDirCSV() + "link_labels.csv"; };
	
	public static String getFilePrediction(String simType, int split, int numExamples) { 
		return getDirOut() + "pred_" + simType.toLowerCase() + "_train" + numExamples + "_" + split;
	};
	
	public static String getFileWeights(String simType, int split, int numExamples) { 
		return getDirOut() + "weights_" + simType.toLowerCase() + "_train" + numExamples + "_" + split; 
	};	
	
	
	
	static {
    	if (System.getProperty("config") != null) {
	        String filePath = System.getProperty("config").trim();
	        File f = new File(filePath);
	        if (f.exists()) {
	            Properties prop = new Properties();
		        try {
		            prop.load(new FileInputStream(f));
		            for (Field field : Config.class.getFields()) {
		                if (field.getType().getName().equals("int"))
		                    setInt(prop, field);
		                else if (field.getType().getName().equals("double")) 
		                    setDouble(prop, field);
		                else if (field.getType().getName().equals("boolean")) 
		                    setBoolean(prop, field);
		                else if (field.getType().equals(String.class))
		                    setString(prop, field);
		                else if (field.getType().equals(String[].class)) 
		                	setStringArray(prop, field);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
	        }
    	}
    }
    
    private static boolean hasValidProp(Properties prop, Field field) {
    	return prop.getProperty(field.getName()) != null
        	&& !prop.getProperty(field.getName()).trim().isEmpty();
    }
    
    private static String getProp(Properties prop, Field field) {
    	return prop.getProperty(field.getName()).trim();
    }

    private static void setInt(Properties prop, Field field) throws Exception {
        if (hasValidProp(prop, field))
            field.set(null, Integer.valueOf(getProp(prop, field)));
    }
    
    private static void setBoolean(Properties prop, Field field) throws Exception {
        if (hasValidProp(prop, field)) {
            field.set(null, Boolean.valueOf(getProp(prop, field)));
        }
    }

    private static void setDouble(Properties prop, Field field) throws Exception {
        if (hasValidProp(prop, field)) {
            field.set(null, Double.valueOf(getProp(prop, field)));
        }
    }

    private static void setString(Properties prop, Field field) throws Exception {
        if (hasValidProp(prop, field)) {
            field.set(null, getProp(prop, field));
        }
    }
    
    private static void setStringArray(Properties prop, Field field) throws Exception {
        if (hasValidProp(prop, field)) {
        	field.set(null, getProp(prop, field).split(";"));
        }
    }
    
    public static void print() {
    	try {
    		for (Field field : Config.class.getFields())
    			System.out.println(field.getName() + " = " + field.get(null));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
    	print();
    }
}
