package com.bagri.common.util;

import static com.bagri.common.config.XDMConfigConstants.xdm_node_instance;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_population_buffer_size;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_data_path;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
	
	public static URL path2url(String path) throws IOException {
        File f = new File(path);
        return f.toURI().toURL();
    }

	public static String getPathName(String path) {
		return Paths.get(URI.create(path)).getFileName().toString();
	}

	public static String buildStoreFileName(String dataPath, String nodeNum, String storeName) {
		if (dataPath == null) {
			dataPath = "";
		} else {
			dataPath += "/";
		}
		if (nodeNum == null) {
			nodeNum = "0";
		}
		return dataPath + storeName + nodeNum + ".xdb";
	}
	
	public static String buildSectionFileName(String dataPath, String nodeNum, String storeName, int section) {
		if (dataPath == null) {
			dataPath = "";
		} else {
			dataPath += "/";
		}
		if (nodeNum == null) {
			nodeNum = "0";
		}
		return dataPath + storeName + nodeNum + "_" + section + ".xdb";
	}

	public static String getStoreFileMask(String nodeNum, String storeName) {
		return "regex:" + storeName + nodeNum + "_\\d{1,2}\\.xdb"; 
	}
	
}
