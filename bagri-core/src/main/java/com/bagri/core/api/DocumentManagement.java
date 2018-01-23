package com.bagri.core.api;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Document management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface DocumentManagement {

	/**
	 * 
	 * @param uri the Document uri
	 * @param props contains Document retrieve instructions
	 * @return {@link DocumentAccessor} structure
	 * @throws BagriException in case of any error
	 */
	DocumentAccessor getDocument(String uri, Properties props) throws BagriException;

	/**
	 * return contents of Documents matching provided pattern
	 * 
	 * @param pattern String; the query string conforming to syntax: Document attribute CMP value,
	 * for instance: createdBy = admin, bytes &gt; 3000, uri like security%, collections.contains(securities)
	 * @param props contains query processing instructions
	 * @return Iterable over the contents of matched documents
	 * @throws BagriException in case of any error
	 */
	Iterable<DocumentAccessor> getDocuments(String pattern, Properties props) throws BagriException;
	
	/**
	 * Creates a new Document or overrides an existing one in Repository
	 * 
	 * @param uri Document uri
	 * @param content Document content
	 * @param props Document processing instructions
	 * @param <T> the type of Document content
	 * @return Document accessor
	 * @throws BagriException in case of any error
	 */
	<T> DocumentAccessor storeDocument(String uri, T content, Properties props) throws BagriException;

	/**
	 * stores many Documents in Repository
	 * 
	 * @param documents Map of pairs Document uri/content
	 * @param props documents processing instructions
	 * @param <T> the type of Document content
	 * @return Iterable over stored documents
	 * @throws BagriException in case of any error
	 */
	<T> Iterable<DocumentAccessor> storeDocuments(Map<String, T> documents, Properties props) throws BagriException;
	
	/**
	 * removes Document from Repository
	 * 
	 * @param uri String; the Document uri
	 * @param props document remove instructions
	 * @return Document accessor
	 * @throws BagriException in case of any error
	 */
	DocumentAccessor removeDocument(String uri, Properties props) throws BagriException;
	
	/**
	 * remove all documents matching the pattern provided
	 * 
	 * @param pattern the pattern to delete matching documents
	 * @param props documents remove instructions
	 * @return Iterable over removed documents
	 * @throws BagriException in case of any error
	 */
	Iterable<DocumentAccessor> removeDocuments(String pattern, Properties props) throws BagriException;
	
	/**
	 * return Collection names registered in Repository
	 * 
	 * @return Collection of Document Collection names
	 * @throws BagriException in case of any error
	 */
	Collection<String> getCollections() throws BagriException;
	
	/**
	 * adds Document to the specified Document Collections
	 * 
	 * @param uri String; the Document uri
	 * @param collections String[]; an array of collections to add document into. 
	 * Collections must be registered in the current Repository.
	 * @return the number of additions happened
	 */
	int addDocumentToCollections(String uri, String[] collections);
	
	/**
	 * removes Document from the the specified Document Collections
	 * 
	 * @param uri String; the Document uri
	 * @param collections String[]; an array of collections to remove document from. 
	 * Collections must be registered in the current Repository.
	 * @return the number of deletions happened
	 */
	int removeDocumentFromCollections(String uri, String[] collections);

}
