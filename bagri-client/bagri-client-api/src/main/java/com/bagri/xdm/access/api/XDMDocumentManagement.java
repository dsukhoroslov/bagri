package com.bagri.xdm.access.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.domain.XDMDocument;

public interface XDMDocumentManagement {

	//XDMDocument getDocument(String uri);
	XDMDocument getDocument(long docId);

	Long getDocumentId(String uri);
	//String getDocumentAsString(String uri);
	String getDocumentAsString(long docId);
	// todo: add methods to return document as Source, Reader, XMLStreamReader, InputStream
	XDMDocument storeDocument(String xml);
	XDMDocument storeDocument(long docId, String uri, String xml);
	// todo: add methods to store document from Source, Reader, XMLStreamReader, InputStream
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
