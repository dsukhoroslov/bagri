package com.bagri.xdm.domain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.bagri.xdm.api.XDMException;

public interface XDMParser {
	
	public static final String df_json = "JSON"; 
	public static final String df_xml = "XML"; 

	List<XDMData> parse(String source) throws XDMException; 
	List<XDMData> parse(File file) throws XDMException;
	List<XDMData> parse(InputStream stream) throws XDMException;
	List<XDMData> parse(Reader reader) throws XDMException;
	
}
