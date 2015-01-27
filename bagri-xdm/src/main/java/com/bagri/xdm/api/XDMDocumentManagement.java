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

	//XDMDocument getDocument(String uri);
	//String getDocumentAsString(String uri);
	//void removeDocument(String uri);

	Iterator<Long> getDocumentIds(String pattern);
	//Iterator<XDMDocument> getDocuments(String pattern);
	
	XDMDocument getDocument(long docId);

	String getDocumentAsString(long docId);
	Source getDocumentAsSource(long docId);
	InputStream getDocumentAsSream(long docId);
	// todo: add methods to return document as Document, Reader, XMLStreamReader..
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param docId: long; creates new Document if docId = 0; overrides existing one if docId > 0 and docId exists in repository 
	 * @param uri: String; the Document's URI, can be null; if uri is not null it must be unique in XDM repository 
	 * @param xml: Document's XML representation, can not be null
	 * @return XDMDocument: created or overridden (versioned ?) Document
	 */
	XDMDocument storeDocumentFromString(long docId, String uri, String xml);
	XDMDocument storeDocumentFromSource(long docId, String uri, Source source);
	XDMDocument storeDocumentFromStream(long docId, String uri, InputStream stream);
	// todo: add methods to store document from Document, Reader, XMLStreamReader
	
	void removeDocument(long docId);

}
