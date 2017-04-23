package com.bagri.core.server.api;

import java.util.Properties;

import com.bagri.core.api.BagriException;

public interface ContentModeler {
	
    /**
     * Lifecycle method. Invoked at initialization phase. 
     * 
     * @param properties the environment context
     */
    void init(Properties properties);	

	/**
	 * registers bunch of node path's specified in the XML schema (XSD)   
	 * 
	 * @param model String; schema in plain text  
	 * @throws BagriException in case of any error
	 */
	void registerModel(String model) throws BagriException;
	
	/**
	 * registers bunch of schemas located in the schemaUri folder   
	 * 
	 * @param modelUri String; the folder containing schemas to register  
	 * @throws BagriException in case of any error
	 */
	void registerModelUri(String modelUri) throws BagriException;
	
}
