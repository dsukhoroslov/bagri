package com.bagri.test.ycsb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ResultParser {
	
	private static String[] ab_patterns = new String[] {"[OVERALL]", "[OVERALL]", "[TOTAL_GCs]", "[TOTAL_GC_TIME]", "[TOTAL_GC_TIME_%]", 
			"[CLEANUP]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[UPDATE]", "[UPDATE]", "[UPDATE]", "[UPDATE]", 
			"[UPDATE]",	"[UPDATE]"};

	private static String[] c_patterns = new String[] {"[OVERALL]", "[OVERALL]", "[TOTAL_GCs]", "[TOTAL_GC_TIME]", "[TOTAL_GC_TIME_%]", 
			"[CLEANUP]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]"};

	private static String[] d_patterns = new String[] {"[OVERALL]", "[OVERALL]", "[TOTAL_GCs]", "[TOTAL_GC_TIME]", "[TOTAL_GC_TIME_%]", 
			"[CLEANUP]", "[INSERT]", "[INSERT]", "[INSERT]", "[INSERT]", "[INSERT]", "[INSERT]", "[READ]", "[READ]", "[READ]", "[READ]", 
			"[READ]", "[READ]"};

	private static String[] e_patterns = new String[] {"[OVERALL]", "[OVERALL]", "[TOTAL_GCs]", "[TOTAL_GC_TIME]", "[TOTAL_GC_TIME_%]", 
			"[CLEANUP]", "[SCAN]", "[SCAN]", "[SCAN]", "[SCAN]", "[SCAN]", "[SCAN]", "[INSERT]", "[INSERT]", "[INSERT]", "[INSERT]", 
			"[INSERT]", "[INSERT]"};
	
	private static String[] f_patterns = new String[] {"[OVERALL]", "[OVERALL]", "[TOTAL_GCs]", "[TOTAL_GC_TIME]", "[TOTAL_GC_TIME_%]", 
			"[CLEANUP]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ-MODIFY-WRITE]", "[READ-MODIFY-WRITE]",
			"[READ-MODIFY-WRITE]", "[READ-MODIFY-WRITE]", "[READ-MODIFY-WRITE]", "[READ-MODIFY-WRITE]", "[UPDATE]", "[UPDATE]", "[UPDATE]", 
			"[UPDATE]",	"[UPDATE]",	"[UPDATE]"};

	public static void main(String[] args) {
		//
		String profile = args[0];
		ResultParser parser = new ResultParser(profile);
		for (int i=1; i < args.length; i++) {
			String fName = args[i];
			parser.parseResult(fName);
		}
		
		//Map<Integer, String[]> csv = parser.getCsv();
		//for (Map.Entry<Integer, String[]> entry: csv.entrySet()) {
		//	System.out.println("   " + entry.getKey() + ": " + Arrays.toString(entry.getValue()));
		//}
		System.out.println(parser.getAsCsv());
	}
	
	private Map<Integer, String[]> csv;
	private String[] patterns;
	
	public ResultParser(String profile) {
		if (profile.equalsIgnoreCase("f")) {
			patterns = f_patterns;
		} else if (profile.equalsIgnoreCase("e")) {
			patterns = e_patterns;
		} else if (profile.equalsIgnoreCase("d")) {
			patterns = d_patterns;
		} else if (profile.equalsIgnoreCase("c")) {
			patterns = c_patterns;
		} else {
			patterns = ab_patterns;
		}
		csv = new TreeMap<>();
	}
	
	public Map<Integer, String[]> getCsv() {
		return csv;
	}
	
	public String getAsCsv() {
		String EOL = System.getProperty("line.separator");
		StringBuilder buff = new StringBuilder("metrics, 1 thread, 2 threads, 4 threads, 8 threads, 16 threads, 32 threads, 64 threads, 128 threads").append(EOL);
		for (String[] entry: csv.values()) {
			for (String value: entry) {
				buff.append(value).append(",");
			}
			buff.delete(buff.length() - 1, buff.length());
			buff.append(EOL);
		}
		return buff.toString();
	}
	
	public void parseResult(String fileName) {
		Map<Integer, String[]> results = new HashMap<>(patterns.length);
	    File file = new File(fileName);
	    int thCount = 0;
	    try (Scanner sc = new Scanner(file)) {
	        int idx = 0;
	        while (idx < patterns.length && sc.hasNextLine()) {
	        	String line = sc.nextLine();
	        	String pattern = patterns[idx];
	        	if (line.startsWith(pattern)) {
	        		String[] parts = line.split(", ");
	        		results.put(idx, parts);
	        		if (idx == 5) {
	        			thCount = Integer.parseInt(parts[2]);
	        		}
	            	idx++;
	        	}
	        }
	        sc.close();
	    }  catch (FileNotFoundException ex) {
	        ex.printStackTrace();
	        return;
	    }
	    
	    int idx = 1;
	    while (thCount % 2 == 0) {
	    	thCount = thCount / 2;
	    	idx++;
	    }
		//System.out.println(fileName + "; " + idx);
	    for (Map.Entry<Integer, String[]> entry: results.entrySet()) {
	    	int key = entry.getKey();
	    	if (key == 5) {
	    		continue;
	    	} 
	    	if (key > 5) {
	    		key--;
	    	}
    		String[] value = entry.getValue();
	    	String[] line = csv.get(key);
	    	if (line == null) {
	    		line = new String[9];
	    		line[0] = value[0] + " " + value[1];
	    		csv.put(key, line);
	    	}
	    	line[idx] = value[2];
	    }
	}
	
}

