package com.bagri.core.api;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.model.Document;

/**
 * XDM document management interface; provided for the client side
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
	 * @return Collection&lt;String&gt; - matched Document uris
	 * @throws BagriException in case of any error
	 */
	Iterable<String> getDocumentUris(String pattern, Properties props) throws BagriException;

	/**
	 * return contents of Documents matching provided pattern
	 * 
	 * @param pattern String; the query string conforming to syntax: Document attribute CMP value,
	 * for instance: createdBy = admin, bytes &gt; 3000, uri like security%, collections.contains(securities)
	 * @param props contains query processing instructions
	 * @return Iterable over the contents of matched documents
	 * @throws BagriException in case of any error
	 */
	<T> Iterable<T> getDocuments(String pattern, Properties props) throws BagriException;
	
	/**
	 * 
	 * @param uri the Document uri
	 * @return {@link Document} structure
	 * @throws BagriException in case of any error
	 */
	Document getDocument(String uri) throws BagriException;

	// TODO: add methods to return document as Document, Reader, Source, XMLStreamReader..?
	/**
	 * return document content as requested in properties. 
	 * 
	 * @param uri
	 * @param props
	 * @return
	 * @throws BagriException
	 */
	<T> T getDocumentAs(String uri, Properties props) throws BagriException;

	// TODO: add methods to store document from Document, Reader, Source, XMLStreamReader..?
	/**
	 * Creates a new Document or overrides an existing one in Repository
	 * 
	 * @param uri
	 * @param content
	 * @param props
	 * @return
	 * @throws BagriException
	 */
	<T> Document storeDocumentFrom(String uri, T content, Properties props) throws BagriException;

	/**
	 * stores many Documents in Repository
	 * 
	 * @param documents
	 * @param props
	 * @return
	 * @throws BagriException
	 */
	<T> Iterable<Document> storeDocuments(Map<String, T> documents, Properties props) throws BagriException;
	
	/**
	 * removes Document from Repository
	 * 
	 * @param uri String; the Document uri
	 * @throws BagriException in case of any error
	 */
	void removeDocument(String uri, Properties props) throws BagriException;
	
	/**
	 * remove all documents matching the pattern provided
	 * 
	 * @param collection the collection name
	 * @return the number of removed documents
	 * @throws BagriException in case of any error
	 */
	int removeDocuments(String pattern, Properties props) throws BagriException;
	
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
