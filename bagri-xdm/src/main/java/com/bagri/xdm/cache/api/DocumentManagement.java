package com.bagri.xdm.cache.api;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;

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
	 * @param docKey the internal Document key represented as long 
	 * @param props result production properties
	 * @return XDM Document content
	 * @throws XDMException in case of any error
	 */
	String getDocumentAsString(long docKey, Properties props) throws XDMException;

	/**
	 * provides XDM Document content for the internal document key
	 * 
	 * @param docKey the internal Document key 
	 * @param props result production properties
	 * @return XDM Document content
	 * @throws XDMException in case of any error
	 */
	String getDocumentAsString(DocumentKey docKey, Properties props) throws XDMException;

	/**
	 * provides document's MIME type (xml/json as of now)
	 * 
	 * @param docKey the internal Document key 
	 * @return the Document's content MIME type
	 * @throws XDMException in case of any error
	 */
	String getDocumentContentType(long docKey) throws XDMException;
	
	/**
	 * Creates a new Document structure from the content provided
	 * 
	 * @param docKey the Document key
	 * @param uri the Document uri
	 * @param content the Document content
	 * @param dataFormat the Document format
	 * @param createdAt dateTiem of the Document creation
	 * @param createdBy the Document's owner
	 * @param txStart the Id of the transaction owning Document  
	 * @param collections the collection Ids to include document in 
	 * @param addContent to cache Document content or not
	 * @return the Document created
	 * @throws XDMException in case of any error happened
	 */
	Document createDocument(DocumentKey docKey, String uri, String content, String dataFormat, 
			Date createdAt, String createdBy, long txStart, int[] collections, boolean addContent) throws XDMException;
		
}
