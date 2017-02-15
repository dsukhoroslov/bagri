package com.bagri.core.server.api;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;

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
     * Lifecycle method. Invoked at Schema initialization phase. 
     * 
     * @param properties the environment context
     */
    void init(Properties properties);	

	/**
	 * 
	 * @param source the document's content
	 * @return the list of parsed data elements 
	 * @throws BagriException in case of parsing error
	 */
	List<Data> parse(String source) throws BagriException;
	
	/**
	 * 
	 * @param file the document's content
	 * @return the list of parsed data elements
	 * @throws BagriException in case of parsing error
	 */
	List<Data> parse(File file) throws BagriException;
	
	/**
	 * 
	 * @param stream the document's content
	 * @return the list of parsed data elements
	 * @throws BagriException in case of parsing error
	 */
	List<Data> parse(InputStream stream) throws BagriException;
	
	/**
	 * 
	 * @param reader the document's content
	 * @return the list of parsed data elements
	 * @throws BagriException in case of parsing error
	 */
	List<Data> parse(Reader reader) throws BagriException;
	
}
