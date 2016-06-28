package com.bagri.xdm.cache.api;

import java.io.InputStream;
import java.util.Map;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.domain.Elements;

/**
 * Converts internal XDM data representation to plain text representation. A counterpart to XDMParser interface.
 *  
 * @see XDMParser  
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMBuilder {

	/**
	 * 
	 * @param elements a set of XDM elements to convert
	 * @return String content representation 
	 * @throws XDMException in case of any conversion error
	 */
    String buildString(Map<DataKey, Elements> elements) throws XDMException;
    
    /**
     * 
     * @param elements a set of XDM elements to convert
     * @return InputStream content representation 
     * @throws XDMException in case of any conversion error
     */
    InputStream buildStream(Map<DataKey, Elements> elements) throws XDMException; 
	
}
