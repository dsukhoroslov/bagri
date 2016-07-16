package com.bagri.xdm.cache.api;

import java.io.InputStream;
import java.util.Properties;

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
	 * @param docKey the internal Document key 
	 * @param props result production properties
	 * @return XDM Document content
	 * @throws XDMException in case of any error
	 */
	String getDocumentAsString(long docKey, Properties props) throws XDMException;

	/**
	 * provides XDM Document content as {@link InputStream} for the internal document key
	 * 
	 * @param docKey the internal Document key 
	 * @param props result production properties
	 * @return XDM Document content as {@link InputStream}
	 * @throws XDMException in case of any error
	 */
	InputStream getDocumentAsStream(long docKey, Properties props) throws XDMException;

	/**
	 * provides document's MIME type (xml/json as of now)
	 * 
	 * @param docKey the internal Document key 
	 * @return the Document's content MIME type
	 * @throws XDMException in case of any error
	 */
	String getDocumentContentType(long docKey) throws XDMException;
	
}
