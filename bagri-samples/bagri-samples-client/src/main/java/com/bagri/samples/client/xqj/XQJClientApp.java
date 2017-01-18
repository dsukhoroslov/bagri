package com.bagri.samples.client.xqj;

import static com.bagri.core.Constants.pn_schema_address;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_password;
import static com.bagri.core.Constants.pn_schema_user;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.xqj.BagriXQDataSource.ADDRESS;
import static com.bagri.xqj.BagriXQDataSource.PASSWORD;
import static com.bagri.xqj.BagriXQDataSource.SCHEMA;
import static com.bagri.xqj.BagriXQDataSource.USER;
import static com.bagri.xqj.BagriXQDataSource.XDM_REPOSITORY;
import static com.bagri.xqj.BagriXQDataSource.XQ_PROCESSOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import com.bagri.samples.client.BagriClientApp;
import com.bagri.xqj.BagriXQDataSource;

public class XQJClientApp implements BagriClientApp {

	private XQConnection xqConn;
	    
	public static void main(String[] args) throws Exception {
		
		if (args.length < 4) {
			throw new XQException("wrong number of arguments passed. Expected: schemaAddress schemaName userName password");
		}
		
		Properties props = new Properties();
	    props.setProperty(pn_schema_address, args[0]);
	    props.setProperty(pn_schema_name, args[1]);
	    props.setProperty(pn_schema_user, args[2]);
	    props.setProperty(pn_schema_password, args[3]);

	    XQJClientApp client = new XQJClientApp(props); 
		tester.testClient(client);
	}
	
	public XQJClientApp(Properties props) throws XQException {
		XQDataSource xqds = new BagriXQDataSource();
		xqds.setProperty(ADDRESS, props.getProperty(pn_schema_address));
		xqds.setProperty(SCHEMA, props.getProperty(pn_schema_name));
		xqds.setProperty(USER, props.getProperty(pn_schema_user));
		xqds.setProperty(PASSWORD, props.getProperty(pn_schema_password));
		xqds.setProperty(XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
		xqds.setProperty(XDM_REPOSITORY, "com.bagri.client.hazelcast.impl.SchemaRepositoryImpl");
		xqConn = xqds.getConnection();
	}

	public XQJClientApp(XQConnection xqConn) {
		this.xqConn = xqConn;
	}
	
	@Override
	public void close() throws XQException {
		xqConn.close();
	}
	
	@Override
	public boolean createDocument(String uri, String content) throws XQException {
		
		String result = storeDocument(uri, content);
    	return result != null;
	}
	
	@Override
	public String readDocument(String uri) throws XQException {
		
		String query = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
				"declare variable $uri external;\n" + 
				"let $doc := bgdb:get-document-content($uri)\n" +
				"return $doc\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    xqpe.bindString(new QName("uri"), uri, xqConn.createAtomicType(XQItemType.XQBASETYPE_ANYURI));
	    XQResultSequence xqs = xqpe.executeQuery();
	    String result = null;
	    if (xqs.next()) {
			result = xqs.getItemAsString(null);
	    }
	    return result;
	}
	
	@Override
	public String queryDocumentByUri(String uri) throws XQException {

		String query = "for $doc in fn:doc(\"" + uri + "\")\n" +
				"return $doc\n";

	    XQExpression xqe = xqConn.createExpression();
	    XQResultSequence xqs = xqe.executeQuery(query);
	    String result = null;
	    if (xqs.next()) {
			result = xqs.getItemAsString(null);
	    }
	    return result;
	}
	
	@Override
	public String queryDocumentFromCollection() throws XQException {

		String query = "for $doc in fn:collection()\n" +
				"return $doc\n";

	    XQExpression xqe = xqConn.createExpression();
	    XQResultSequence xqs = xqe.executeQuery(query);
	    String result = null;
	    if (xqs.next()) {
			result = xqs.getItemAsString(null);
	    }
	    return result;
	}
	
	@Override
	public boolean updateDocument(String uri, String content) throws XQException {

		String result = storeDocument(uri, content);
    	// document version must be > 1
    	return uri.equals(result);
	}
	
	@Override
	public void deleteDocument(String uri) throws XQException {

		String query = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
				"declare variable $uri external;\n" + 
				"let $uri := bgdb:remove-document($uri)\n" + 
				"return $uri\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    xqpe.bindString(new QName("uri"), uri, xqConn.createAtomicType(XQItemType.XQBASETYPE_ANYURI));
	    XQSequence xqs = xqpe.executeQuery();
	    String result = null;
	    try {
		    if (xqs.next()) {
		    	result = xqs.getAtomicValue();
		    }
		    if (!uri.equals(result)) {
		    	throw new XQException("got no result from bgdb:remove-document function");
		    }
	    } finally {
	    	xqpe.close();
	    	xqs.close();
	    }
	}

	private String storeDocument(String uri, String content) throws XQException {
		
		String query = "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
				"declare variable $uri external;\n" + 
				"declare variable $xml external;\n" + 
				"declare variable $props external;\n" + 
				"let $uri := bgdb:store-document($uri, $xml, $props)\n" +
				"return $uri\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    xqpe.bindString(new QName("uri"), uri, xqConn.createAtomicType(XQItemType.XQBASETYPE_ANYURI));
	    xqpe.bindString(new QName("xml"), content, xqConn.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    List<String> props = new ArrayList<>(2);
	    props.add(pn_document_data_format + "=xml");
	    // 
	    xqpe.bindSequence(new QName("props"), xqConn.createSequence(props.iterator()));
	    XQSequence xqs = xqpe.executeQuery();
	    String result = null;
	    try {
	    	if (xqs.next()) {
	    		result = xqs.getAtomicValue();
	    	}
	    } finally {
	    	xqpe.close();
	    	xqs.close();
	    }
    	return result;
	}
	
}
