package com.bagri.xdm.common;

/**
 * A factory to produce various key instances. The key implementation and coresponding factory depends on the underlying cache platform
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface KeyFactory {
	
	/**
	 * 
	 * @param documentKey the internal document key identifier
	 * @return the document key instance
	 */
	DocumentKey newDocumentKey(long documentKey);
	
	/**
	 * 
	 * @param documentId the uri hash and key revision 
	 * @param version the document version
	 * @return the document key instance
	 */
	DocumentKey newDocumentKey(long documentId, int version);
	
	/**
	 * 
	 * @param documentUri the document uri
	 * @param revision the key revision
	 * @param version the document version
	 * @return the document key instance
	 */
	DocumentKey newDocumentKey(String documentUri, int revision, int version);
	
	/**
	 * 
	 * @param documentKey the internal document key
	 * @param pathId the path identifier
	 * @return the data key instance
	 */
	DataKey newDataKey(long documentKey, int pathId);
	
	/**
	 * 
	 * @param pathId the path identifier
	 * @param value the indexed path value
	 * @return the index key instance
	 */
	IndexKey newIndexKey(int pathId, Object value);

}
