package com.bagri.xdm.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMData;

/**
 * Convert Document's content to its internal XDM data representation. A counterpart to XDMBuilder interface.
 *  
 * @see XDMBuilder  
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMParser {
	
	/**
	 * 
	 * @param source the document's content
	 * @return the list of parsed data elements 
	 * @throws XDMException in case of parsing error
	 */
	List<XDMData> parse(String source) throws XDMException;
	
	/**
	 * 
	 * @param file the document's content
	 * @return the list of parsed data elements
	 * @throws XDMException in case of parsing error
	 */
	List<XDMData> parse(File file) throws XDMException;
	
	/**
	 * 
	 * @param stream the document's content
	 * @return the list of parsed data elements
	 * @throws XDMException in case of parsing error
	 */
	List<XDMData> parse(InputStream stream) throws XDMException;
	
	/**
	 * 
	 * @param reader the document's content
	 * @return the list of parsed data elements
	 * @throws XDMException in case of parsing error
	 */
	List<XDMData> parse(Reader reader) throws XDMException;
	
}
