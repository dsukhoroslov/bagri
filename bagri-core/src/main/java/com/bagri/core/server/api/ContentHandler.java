package com.bagri.core.server.api;

import java.util.Properties;

public interface ContentHandler {
	
	/**
	 *  
	 * @return the corresponding DataFormat abbreviation
	 */
	String getDataFormat();
	
	/**
	 * 
	 * @return true if content is a String, false otherwise
	 */
	boolean isStringFormat();
	
	/**
	 * 
	 * @return Builder which is used to build content with corresponding data format 
	 */
	ContentBuilder<?> getBuilder();
	
	/**
	 * 
	 * @return Modeler which process schemas for the data format
	 */
	ContentModeler getModeler();
	
	/**
	 * 
	 * @return Parser which is used to parse original documents in corresponding data format
	 */
	ContentParser<?> getParser();
	
	/**
	 * 
	 * @param props properties for all internal components
	 */
	void init(Properties props);

}
