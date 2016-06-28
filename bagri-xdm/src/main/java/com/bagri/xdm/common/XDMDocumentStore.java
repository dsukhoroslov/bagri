package com.bagri.xdm.common;

import java.util.Collection;
import java.util.Map;

import com.bagri.xdm.domain.Document;

/**
 * Abstracts Document persistent store from the underlying storage system. Resides between XDM Document and Elements caches and persistent store. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMDocumentStore {
	
	/**
	 * Lifecycle method. Invoked when the store initialized. 
	 * 
	 * @param context the environment context
	 */
	public void init(Map<String, Object> context);
	
	/**
	 * Lifecycle method. Invoked when parent schema is closing
	 */
	public void close();

	/**
	 * Load document from persistent store
	 * 
	 * @param key the document key
	 * @return XDM Document instance if corresponding document found, null otherwise
	 */
	public Document loadDocument(XDMDocumentKey key);

	/**
	 * Load bunch of documents from persistent store
	 * 
	 * @param keys the collection of document keys to load
	 * @return the map of loaded documents with their keys
	 */
	public Map<XDMDocumentKey, Document> loadAllDocuments(Collection<XDMDocumentKey> keys);

	/**
	 * Load document keys. Can do it in synch or asynch way.
	 * 
	 * @return iterator over found document keys
	 */
	public Iterable<XDMDocumentKey> loadAllDocumentKeys();

	/**
	 * Stores document to persistent store.
	 * 
	 * @param key the document key
	 * @param value the XDM document instance
	 */
	public void storeDocument(XDMDocumentKey key, Document value);

	/**
	 * Stores bunch of documents to persistent store
	 * 
	 * @param entries the map of document keys and corresponding document instances
	 */
	public void storeAllDocuments(Map<XDMDocumentKey, Document> entries);

	/**
	 * Deletes document from persistent store
	 * 
	 * @param key the document key
	 */
	public void deleteDocument(XDMDocumentKey key);

	/**
	 * Deletes bunch o documents from persistent store 
	 * 
	 * @param keys the keys identifying documents to be deleted 
	 */
	public void deleteAllDocuments(Collection<XDMDocumentKey> keys);
		
}
