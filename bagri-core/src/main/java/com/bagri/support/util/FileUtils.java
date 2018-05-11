package com.bagri.support.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * A set of static utility methods regarding files
 * 
 * @author Denis Sukhoroslov
 *
 */
public class FileUtils {
	
	public static final String EOL = System.getProperty("line.separator");
	
	public final static Charset cs_encoding = StandardCharsets.UTF_8;
	public final static String def_encoding = cs_encoding.name();
	
	/**
	 * Reads text file and return its content as a String
	 * 
	 * @param fileName the full path to a file to read
	 * @return the file content
	 * @throws IOException in case of read error
	 */
	public static String readTextFile(String fileName) throws IOException {
	    return readTextFile(fileName, def_encoding);
	}
	
	/**
	 * Reads text file and return its content as a String
	 * 
	 * @param fileName the full path to a file to read
	 * @param encoding the file encoding
	 * @return the file content
	 * @throws IOException in case of read error
	 */
	public static String readTextFile(String fileName, String encoding) throws IOException {
		Charset cs = Charset.forName(encoding);
	    StringBuilder text = new StringBuilder();
	    MappedByteBuffer buff = null;
	    try (FileInputStream fis = new FileInputStream(fileName)) {
			FileChannel ch = fis.getChannel();
			buff = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
			while (buff.hasRemaining()) {
				CharBuffer cb = cs.decode(buff);
				text.append(cb.toString());
			}
			ch.close();
	    }
	    unmap(buff);
	    return text.toString();
	}

	/**
	 * Writes String content to a file. Overrides file if it exists already
	 * 
	 * @param fileName the full path to a file to write
	 * @param content the String content to write
	 * @throws IOException in case of write error
	 */
	public static void writeTextFile(String fileName, String content) throws IOException {
		byte[] bytes = content.getBytes();
		MappedByteBuffer buff = null;
		try (RandomAccessFile raw = new RandomAccessFile(fileName, "rw")) {
			raw.setLength(bytes.length);
			FileChannel ch = raw.getChannel();
			buff = ch.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length); 
			buff.put(bytes);
		}
		unmap(buff);
	}

    private static void unmap(MappedByteBuffer buffer) {
		if (buffer != null) {
			sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
			if (cleaner != null) {
				cleaner.clean();
			}
		}
	}	
    
	/**
	 * Appends String content to a file. Throws FileNotFoundException if it not exists yet  
	 * 
	 * @param fileName the full path to a file to write to
	 * @param content the String content to append
	 * @throws IOException in case of write error
	 */
    public static void appendTextFile(String fileName, String content) throws IOException {
        // TODO: do append via NIO
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName, true), def_encoding))) {
            writer.write(content);
        }           
    }
    
    /**
     * Converts URI string to file Path 
     * 
     * @param uri the URI string to convert
     * @return the converted String if it starts from {@code file:/} scheme, or the original string otherwise 
     */
    public static String uri2Path(String uri) {
		if (uri.startsWith("file:/")) {
			return Paths.get(URI.create(uri)).toString();
		}
		return uri;
	}
	
    /**
     * Converts file path String to URI string
     * 
     * @param path the file Path string to convert
     * @return the converted URI string
     */
	public static String path2Uri(String path) {
		if (!path.startsWith("file:///")) {
			if (path.startsWith("file:/")) {
				path = Paths.get(URI.create(path)).toString();
			}
			return Paths.get(path).toUri().toString();
		}
		return path;
	}
	
	/**
	 * Converts Path string to URL string
	 * 
	 * @param path the path string to convert
	 * @return the url string
	 * @throws IOException in case of malformed path string
	 */
	public static URL path2url(String path) throws IOException {
        File f = new File(path);
        return f.toURI().toURL();
    }

	/**
	 * Extracts file name from full path name
	 * 
	 * @param path the full path string
	 * @return the last portion of the full path: the file name with extension
	 */
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
