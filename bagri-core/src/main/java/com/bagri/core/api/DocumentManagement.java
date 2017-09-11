package com.bagri.core.api;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.model.Document;

/**
 * Document management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface DocumentManagement {

	/**
	 * search Document attributes by pattern provided
	 * 
	 * @param pattern String; the query string conforming to syntax: Document attribute CMP value,
	 * for instance: createdBy = admin, bytes &gt; 3000, uri like security%
	 * @param props contains query processing instructions
	 * @return Iterable&lt;String&gt; over matched Document uris
	 * @throws BagriException in case of any error
	 */
	Iterable<String> getDocumentUris(String pattern, Properties props) throws BagriException;

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
	 * @param uri
	 * @param content
	 * @param props
	 * @return
	 * @throws BagriException
	 */
	<T> DocumentAccessor storeDocument(String uri, T content, Properties props) throws BagriException;

	/**
	 * stores many Documents in Repository
	 * 
	 * @param documents
	 * @param props
	 * @return
	 * @throws BagriException
	 */
	<T> Iterable<DocumentAccessor> storeDocuments(Map<String, T> documents, Properties props) throws BagriException;
	
	/**
	 * removes Document from Repository
	 * 
	 * @param uri String; the Document uri
	 * @param props
	 * @throws BagriException in case of any error
	 */
	DocumentAccessor removeDocument(String uri, Properties props) throws BagriException;
	
	/**
	 * remove all documents matching the pattern provided
	 * 
	 * @param pattern
	 * @param props
	 * @return
	 * @throws BagriException
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
