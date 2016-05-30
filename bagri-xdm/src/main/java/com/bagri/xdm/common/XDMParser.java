package com.bagri.xdm.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMData;

public interface XDMParser {
	
	List<XDMData> parse(String source) throws XDMException; 
	List<XDMData> parse(File file) throws XDMException;
	List<XDMData> parse(InputStream stream) throws XDMException;
	List<XDMData> parse(Reader reader) throws XDMException;
	
}
