package com.bagri.xdm.cache.api;

import java.util.Collection;
import java.util.Map;

import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;

/**
 * Abstracts Document persistent store from the underlying storage system. Resides between XDM Document and Elements caches and persistent store. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface DocumentStore {
	
	/**
	 * Lifecycle method. Invoked when the store initialized. 
	 * 
	 * @param context the environment context
	 */
	void init(Map<String, Object> context);
	
	/**
	 * Lifecycle method. Invoked when parent schema is closing
	 */
	void close();

	/**
	 * Load document from persistent store
	 * 
	 * @param key the document key
	 * @return XDM Document instance if corresponding document found, null otherwise
	 */
	Document loadDocument(DocumentKey key);

	/**
	 * Load bunch of documents from persistent store
	 * 
	 * @param keys the collection of document keys to load
	 * @return the map of loaded documents with their keys
	 */
	Map<DocumentKey, Document> loadAllDocuments(Collection<DocumentKey> keys);

	/**
	 * Load document keys. Can do it in synch or asynch way.
	 * 
	 * @return iterator over found document keys
	 */
	Iterable<DocumentKey> loadAllDocumentKeys();

	/**
	 * Stores document to persistent store.
	 * 
	 * @param key the document key
	 * @param value the XDM document instance
	 */
	void storeDocument(DocumentKey key, Document value);

	/**
	 * Stores bunch of documents to persistent store
	 * 
	 * @param entries the map of document keys and corresponding document instances
	 */
	void storeAllDocuments(Map<DocumentKey, Document> entries);

	/**
	 * Deletes document from persistent store
	 * 
	 * @param key the document key
	 */
	void deleteDocument(DocumentKey key);

	/**
	 * Deletes bunch o documents from persistent store 
	 * 
	 * @param keys the keys identifying documents to be deleted 
	 */
	void deleteAllDocuments(Collection<DocumentKey> keys);
		
}
