package com.bagri.xdm.api;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.domain.XDMDocument;

/**
 * XDMDocumentManagement - client interface. Manages documents
 * 
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 */
public interface XDMDocumentManagement {

	Collection<String> getDocumentUris(String pattern); // throws XDMException;
	//Collection<XDMDocument> getDocuments(String pattern);
	Collection<String> getCollectionDocumentUris(String collection) throws XDMException;
	
	XDMDocument getDocument(String uri) throws XDMException;

	String getDocumentAsString(String uri) throws XDMException;
	//Source getDocumentAsSource(String uri) throws XDMException;
	InputStream getDocumentAsSream(String uri) throws XDMException;
	// todo: add methods to return document as Document, Reader, XMLStreamReader..
	Object getDocumentAsBean(String uri) throws XDMException;
	Map<String, Object> getDocumentAsMap(String uri) throws XDMException;
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param uri: String - document uri  
	 * @param content: Document's XML representation, can not be null
	 * @param props: Document store instructions
	 * @return XDMDocument: created or overridden (versioned) Document
	 */
	XDMDocument storeDocumentFromString(String uri, String content, Properties props) throws XDMException;
	//XDMDocument storeDocumentFromSource(String uri, Source source, Properties props) throws XDMException;
	XDMDocument storeDocumentFromStream(String uri, InputStream stream, Properties props) throws XDMException;
	// todo: add methods to store document from Document, Reader, XMLStreamReader
	XDMDocument storeDocumentFromBean(String uri, Object bean, Properties props) throws XDMException;
	XDMDocument storeDocumentFromMap(String uri, Map<String, Object> fields, Properties props) throws XDMException;

	
	void removeDocument(String uri) throws XDMException;
	int removeCollectionDocuments(String collection) throws XDMException;
	
	int addDocumentToCollections(String uri, String[] collections);
	int removeDocumentFromCollections(String uri, String[] collections);

}
