package com.bagri.core.api;

import java.io.InputStream;
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
	 * @return Collection&lt;String&gt; - matched Document uris
	 * @throws BagriException in case of any error
	 */
	Collection<String> getDocumentUris(String pattern) throws BagriException;
	
	// not sure we need it..
	//Collection<XDMDocument> getDocuments(String pattern);
	
	/**
	 * return Document uris which belongs to the collection
	 * 
	 * @param collection String; the schema collection name
	 * @return Collection&lt;String&gt; - Document uris belonging to the collection
	 * @throws BagriException in case of any error
	 */
	Collection<String> getCollectionDocumentUris(String collection) throws BagriException;

	/**
	 * return Collection names registered in Schema
	 * 
	 * @return Collection of XDM Collection names
	 * @throws BagriException in case of any error
	 */
	Collection<String> getCollections() throws BagriException;
	
	/**
	 * 
	 * @param uri the XDM document uri
	 * @return {@link Document} structure
	 * @throws BagriException in case of any error
	 */
	Document getDocument(String uri) throws BagriException;

	/**
	 * 
	 * @param uri the XDM document uri
	 * @param props result production properties
	 * @return XDM Document content as a plain text
	 * @throws BagriException in case of any error
	 */
	String getDocumentAsString(String uri, Properties props) throws BagriException;
	
	/**
	 * construct {@link InputStream} over XDMDocument content identified by the uri provided 
	 * 
	 * @param uri the XDM document uri
	 * @param props result production properties
	 * @return {@link InputStream} over the document's content
	 * @throws BagriException in case of any error
	 */
	InputStream getDocumentAsSream(String uri, Properties props) throws BagriException;
	
	/**
	 * 
	 * @param uri the XDM document uri
	 * @param props result production properties
	 * @return POJO representing the XDM document
	 * @throws BagriException in case of any error
	 */
	Object getDocumentAsBean(String uri, Properties props) throws BagriException;
	
	/**
	 * 
	 * @param uri the XDM document uri
	 * @param props result production properties
	 * @return Map&lt;String, Object&gt; representing the XDM document
	 * @throws BagriException in case of any error
	 */
	Map<String, Object> getDocumentAsMap(String uri, Properties props) throws BagriException;

	// TODO: add methods to return document as Document, Reader, Source, XMLStreamReader..?
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param uri String; the XDM document uri  
	 * @param content document's text (JSON, XML, ..) representation, can not be null
	 * @param props Properties; the document processing instructions
	 * @return XDMDocument created or overridden (versioned) document
	 * @throws BagriException in case of any error
	 */
	Document storeDocumentFromString(String uri, String content, Properties props) throws BagriException;
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param uri String; the XDM document uri  
	 * @param stream the {@link InputStream} over document's text (JSON, XML, ..) representation, can not be null
	 * @param props Properties; the document processing instructions
	 * @return XDMDocument created or overridden (versioned) document
	 * @throws BagriException in case of any error
	 */
	Document storeDocumentFromStream(String uri, InputStream stream, Properties props) throws BagriException;
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param uri String; the XDM document uri  
	 * @param bean the document's POJO representation, can not be null
	 * @param props Properties; the document processing instructions
	 * @return XDMDocument created or overridden (versioned) document
	 * @throws BagriException in case of any error
	 */
	Document storeDocumentFromBean(String uri, Object bean, Properties props) throws BagriException;
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param uri String; the XDM document uri  
	 * @param fields the document's structure represented as java {@link Map}, can not be null
	 * @param props Properties; the document processing instructions
	 * @return XDMDocument created or overridden (versioned) document
	 * @throws BagriException in case of any error
	 */
	Document storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws BagriException;

	// TODO: add methods to store document from Document, Reader, Source, XMLStreamReader..?
	
	/**
	 * removes document from XDM repository
	 * 
	 * @param uri String; the XDM document uri
	 * @throws BagriException in case of any error
	 */
	void removeDocument(String uri) throws BagriException;
	
	/**
	 * remove all documents belonging to the specified collection
	 * 
	 * @param collection the collection name
	 * @return the number of removed documents
	 * @throws BagriException in case of any error
	 */
	int removeCollectionDocuments(String collection) throws BagriException;
	
	/**
	 * adds document to the specified collections
	 * 
	 * @param uri String; the XDM document uri
	 * @param collections String[]; an array of collections to add document into. Collections must be registered in the current XDM repository.
	 * @return the number of additions happened
	 */
	int addDocumentToCollections(String uri, String[] collections);
	
	/**
	 * 
	 * @param uri String; the XDM document uri
	 * @param collections String[]; an array of collections to remove document from. Collections must be registered in the current XDM repository.
	 * @return the number of deletions happened
	 */
	int removeDocumentFromCollections(String uri, String[] collections);

}