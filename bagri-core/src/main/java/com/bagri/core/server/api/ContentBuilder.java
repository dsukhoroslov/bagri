package com.bagri.core.server.api;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.DataKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Elements;

/**
 * Converts internal XDM data representation to plain text representation. A counterpart to XDMParser interface.
 *  
 * @see ContentParser  
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface ContentBuilder {
	
    /**
     * Lifecycle method. Invoked at Schema initialization phase. 
     * 
     * @param properties the environment context
     */
    void init(Properties properties);	

	/**
	 * 
	 * @param elements a set of XDM elements to convert
	 * @return String content representation 
	 * @throws BagriException in case of any conversion error
	 */
    String buildString(Map<DataKey, Elements> elements) throws BagriException;
    
    /**
     * 
     * @param elements a set of XDM elements to convert
     * @return InputStream content representation 
     * @throws BagriException in case of any conversion error
     */
    InputStream buildStream(Map<DataKey, Elements> elements) throws BagriException; 
	
}
