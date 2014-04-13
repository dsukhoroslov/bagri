package com.bagri.common.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class FileUtils {
	
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	
	public static String readTextFile(String fileName) throws IOException {
	    Path path = Paths.get(fileName);
	    StringBuilder text = new StringBuilder();
	    try (Scanner scanner = new Scanner(path, ENCODING.name())){
	    	while (scanner.hasNextLine()) {
	    		text.append(scanner.nextLine()).append("\n");
	    	}      
	   	}
	    return text.toString();
	}
	

}
