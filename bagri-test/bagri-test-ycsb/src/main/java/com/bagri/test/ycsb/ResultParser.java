package com.bagri.test.ycsb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class ResultParser {
	
	private static String[] patterns = new String[] {"[OVERALL]", "[OVERALL]", "[TOTAL_GCs]", "[TOTAL_GC_TIME]", "[TOTAL_GC_TIME_%]", 
			"[CLEANUP]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[READ]", "[UPDATE]", "[UPDATE]", "[UPDATE]", "[UPDATE]", 
			"[UPDATE]",	"[UPDATE]"};
	
	public static void main(String[] args) {
		//
		ResultParser parser = new ResultParser();
		for (String fName: args) {
			parser.parseResult(fName);
		}
	}
	
	public void parseResult(String fileName) {
		
	    File file = new File(fileName);
	    try (Scanner sc = new Scanner(file)) {
	        int idx = 0;
	        while (idx < patterns.length && sc.hasNextLine()) {
	        	String line = sc.nextLine();
	        	String pattern = patterns[idx];
	        	if (line.startsWith(pattern)) {
	        		parseLine(pattern, line);
	            	idx++;
	        	}
	        }
	        sc.close();
	    }  catch (FileNotFoundException ex) {
	        ex.printStackTrace();
	    }		
	}
	
	private void parseLine(String pattern, String line) {
		String[] parts = line.split(", ");
		System.out.println(Arrays.toString(parts));
	}

}

/*
+[OVERALL], RunTime(ms), 390981
+[OVERALL], Throughput(ops/sec), 2557.6690427412072
+[TOTAL_GCs], Count, 169
+[TOTAL_GC_TIME], Time(ms), 266
+[TOTAL_GC_TIME_%], Time(%), 0.06803399653691612
+[CLEANUP], Operations, 1
+[READ], Operations, 499988
+[READ], AverageLatency(us), 387.140651375633
+[READ], MinLatency(us), 182
+[READ], MaxLatency(us), 1924095
+[READ], 95thPercentileLatency(us), 407
+[READ], 99thPercentileLatency(us), 448
+[UPDATE], Operations, 500012
+[UPDATE], AverageLatency(us), 390.682257625817
+[UPDATE], MinLatency(us), 263
+[UPDATE], MaxLatency(us), 590847
+[UPDATE], 95thPercentileLatency(us), 407
+[UPDATE], 99thPercentileLatency(us), 529
*/
