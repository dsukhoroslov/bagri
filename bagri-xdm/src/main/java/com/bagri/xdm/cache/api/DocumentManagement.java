package com.bagri.xdm.cache.api;

import com.bagri.xdm.api.XDMException;

/**
 * XDM Document Management server-side implementation. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface DocumentManagement extends com.bagri.xdm.api.DocumentManagement {

	/**
	 * provides XDM Document content for the internal document key
	 * 
	 * @param docKey internal Document key form
	 * @return XDM Document content
	 * @throws XDMException in case of any error
	 */
	String getDocumentAsString(long docKey) throws XDMException;
	
}
