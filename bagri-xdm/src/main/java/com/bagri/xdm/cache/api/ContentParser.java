package com.bagri.xdm.cache.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.Data;

/**
 * Convert Document's content to its internal XDM data representation. A counterpart to XDMBuilder interface.
 *  
 * @see ContentBuilder  
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface ContentParser {
	
	/**
	 * 
	 * @param source the document's content
	 * @return the list of parsed data elements 
	 * @throws XDMException in case of parsing error
	 */
	List<Data> parse(String source) throws XDMException;
	
	/**
	 * 
	 * @param file the document's content
	 * @return the list of parsed data elements
	 * @throws XDMException in case of parsing error
	 */
	List<Data> parse(File file) throws XDMException;
	
	/**
	 * 
	 * @param stream the document's content
	 * @return the list of parsed data elements
	 * @throws XDMException in case of parsing error
	 */
	List<Data> parse(InputStream stream) throws XDMException;
	
	/**
	 * 
	 * @param reader the document's content
	 * @return the list of parsed data elements
	 * @throws XDMException in case of parsing error
	 */
	List<Data> parse(Reader reader) throws XDMException;
	
}
