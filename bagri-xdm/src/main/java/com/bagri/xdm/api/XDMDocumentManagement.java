package com.bagri.xdm.api;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

/**
 * XDMDocumentManagement - client interface. Manages documents
 * 
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 */
public interface XDMDocumentManagement {

	Collection<XDMDocumentId> getDocumentIds(String pattern); // throws XDMException;
	//Collection<XDMDocument> getDocuments(String pattern);
	Collection<XDMDocumentId> getCollectionDocumentIds(String collection); // throws XDMException;
	
	XDMDocument getDocument(XDMDocumentId docId) throws XDMException;

	Object getDocumentAsBean(XDMDocumentId docId) throws XDMException;
	Map<String, Object> getDocumentAsMap(XDMDocumentId docId) throws XDMException;
	
	String getDocumentAsString(XDMDocumentId docId) throws XDMException;
	Source getDocumentAsSource(XDMDocumentId docId) throws XDMException;
	InputStream getDocumentAsSream(XDMDocumentId docId) throws XDMException;
	// todo: add methods to return document as Document, Reader, XMLStreamReader..
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param docId: long; creates new Document if docId = 0; overrides existing one if docId > 0 and docId exists in repository 
	 * @param uri: String; the Document's URI, can be null; if uri is not null it must be unique in XDM repository 
	 * @param xml: Document's XML representation, can not be null
	 * @return XDMDocument: created or overridden (versioned) Document
	 */
	XDMDocument storeDocumentFromBean(XDMDocumentId docId, Object bean, Properties props) throws XDMException;
	XDMDocument storeDocumentFromMap(XDMDocumentId docId, Map<String, Object> fields, Properties props) throws XDMException;

	XDMDocument storeDocumentFromString(XDMDocumentId docId, String content, Properties props) throws XDMException;
	XDMDocument storeDocumentFromSource(XDMDocumentId docId, Source source, Properties props) throws XDMException;
	XDMDocument storeDocumentFromStream(XDMDocumentId docId, InputStream stream, Properties props) throws XDMException;
	// todo: add methods to store document from Document, Reader, XMLStreamReader
	
	void removeDocument(XDMDocumentId docId) throws XDMException;
	int removeCollectionDocuments(String collection) throws XDMException;
	
	int addDocumentToCollections(XDMDocumentId docId, String[] collections);
	int removeDocumentFromCollections(XDMDocumentId docId, String[] collections);

}
