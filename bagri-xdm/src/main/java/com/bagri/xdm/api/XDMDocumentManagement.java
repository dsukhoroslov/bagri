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

	String getDocumentAsString(XDMDocumentId docId) throws XDMException;
	Source getDocumentAsSource(XDMDocumentId docId) throws XDMException;
	InputStream getDocumentAsSream(XDMDocumentId docId) throws XDMException;
	// todo: add methods to return document as Document, Reader, XMLStreamReader..
	Object getDocumentAsBean(XDMDocumentId docId) throws XDMException;
	Map<String, Object> getDocumentAsMap(XDMDocumentId docId) throws XDMException;
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param docId: XDMDocumentId - document ids container  
	 * @param content: Document's XML representation, can not be null
	 * @param props: Document store instructions
	 * @return XDMDocument: created or overridden (versioned) Document
	 */
	XDMDocument storeDocumentFromString(XDMDocumentId docId, String content, Properties props) throws XDMException;
	XDMDocument storeDocumentFromSource(XDMDocumentId docId, Source source, Properties props) throws XDMException;
	XDMDocument storeDocumentFromStream(XDMDocumentId docId, InputStream stream, Properties props) throws XDMException;
	// todo: add methods to store document from Document, Reader, XMLStreamReader
	XDMDocument storeDocumentFromBean(XDMDocumentId docId, Object bean, Properties props) throws XDMException;
	XDMDocument storeDocumentFromMap(XDMDocumentId docId, Map<String, Object> fields, Properties props) throws XDMException;

	
	void removeDocument(XDMDocumentId docId) throws XDMException;
	int removeCollectionDocuments(String collection) throws XDMException;
	
	int addDocumentToCollections(XDMDocumentId docId, String[] collections);
	int removeDocumentFromCollections(XDMDocumentId docId, String[] collections);

}
