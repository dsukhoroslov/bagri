package com.bagri.xdm.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

public interface XDMParser {
	
	public static final String df_json = "JSON"; 
	public static final String df_xml = "XML"; 

	List<XDMData> parse(String source) throws IOException; 
	List<XDMData> parse(File file) throws IOException;
	List<XDMData> parse(InputStream stream) throws IOException;
	List<XDMData> parse(Reader reader) throws IOException;
	
}
