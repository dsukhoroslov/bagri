package com.bagri.common.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;


public class FileUtils {
	
	public final static Charset cs_encoding = StandardCharsets.UTF_8;
	public final static String def_encoding = cs_encoding.name();
	
	public static String readTextFile(String fileName) throws IOException {
	    Path path = Paths.get(fileName);
	    StringBuilder text = new StringBuilder();
	    try (Scanner scanner = new Scanner(path, def_encoding)){
	    	while (scanner.hasNextLine()) {
	    		text.append(scanner.nextLine()).append("\n");
	    	}      
	   	}
	    return text.toString();
	}
	
	public static void writeTextFile(String fileName, String content) throws IOException {
		
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(fileName), def_encoding))) {
		    writer.write(content);
		}		
	}

    public static void appendTextFile(String fileName, String content) throws IOException {
        
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName, true), def_encoding))) {
            writer.write(content);
        }           
    }
    
    public static String uri2Path(String uri) {
		if (uri.startsWith("file:/")) {
			return Paths.get(URI.create(uri)).toString();
		}
		return uri;
	}
	
	public static String path2Uri(String path) {
		if (!path.startsWith("file:///")) {
			if (path.startsWith("file:/")) {
				path = Paths.get(URI.create(path)).toString();
			}
			return Paths.get(path).toUri().toString();
		}
		return path;
	}
	
}
