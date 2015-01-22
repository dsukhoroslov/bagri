package com.bagri.xdm.access.api;

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
	XDMDocument getDocument(long docId);

	Long getDocumentId(String uri);
	//String getDocumentAsString(String uri);
	String getDocumentAsString(long docId);
	// todo: add methods to return document as Source, Reader, XMLStreamReader, InputStream
	Source getDocumentAsSource(long docId);
	
	/**
	 * Creates a new Document and stores it in XDM repository
	 * 
	 * @param xml: Document's XML representation, can not be null
	 * @return XDMDocument: created Document
	 */
	XDMDocument storeDocument(String xml);
	
	/**
	 * Creates a new Document or overrides an existing one in XDM repository
	 * 
	 * @param docId: long; creates new Document if docId = 0; overrides existing one if docId > 0 and docId exists in repository 
	 * @param uri: String; the Document's URI, can be null; if uri is not null it must be unique in XDM repository 
	 * @param xml: Document's XML representation, can not be null
	 * @return XDMDocument: created or overridden Document
	 */
	XDMDocument storeDocument(long docId, String uri, String xml);
	// todo: add methods to store document from Source, Reader, XMLStreamReader, InputStream

	XDMDocument storeDocumentSource(long docId, Source source);
	
	//void removeDocument(String uri);
	void removeDocument(long docId);

	void close();

	XDMSchemaDictionary getSchemaDictionary();
	Collection<Long> getDocumentIDs(ExpressionContainer query);
	Collection<String> getDocumentURIs(ExpressionContainer query);
	Collection<String> getXML(ExpressionContainer query, String template, Map params);
	
	Object executeXCommand(String command, Map bindings, Properties props);
	Object executeXQuery(String query, Map bindings, Properties props);
	
}
