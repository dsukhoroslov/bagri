package com.bagri.samples.xqj.client;

import static com.bagri.common.config.XDMConfigConstants.xdm_document_data_format;
import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;
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
import com.bagri.xdm.api.XDMException;
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
		xqds.setProperty(XDM_REPOSITORY, "com.bagri.xdm.client.hazelcast.impl.RepositoryImpl");
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
		
		long result = storeDocument(uri, content);
    	return result > 0;
	}
	
	@Override
	public String readDocument(String uri) throws XQException {
		
		String query = "declare namespace bgdm=\"http://bagridb.com/bagri-xdm\";\n" +
				"declare variable $docIds external;\n" + 
				"let $doc := bgdm:get-document($docIds)\n" +
				"return $doc\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    List<Object> docIds = new ArrayList<>();
	    docIds.add(uri);
	    xqpe.bindSequence(new QName("docIds"), xqConn.createSequence(docIds.iterator()));
	    XQResultSequence xqs = xqpe.executeQuery();
	    String result = null;
	    if (xqs.next()) {
			result = xqs.getItemAsString(null);
	    }
	    return result;
	}
	
	@Override
	public String queryDocument() throws XQException {

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

		long result = storeDocument(uri, content);
    	// document version must be > 1
    	return result > 1;
	}
	
	@Override
	public void deleteDocument(String uri) throws XQException {

		String query = "declare namespace bgdm=\"http://bagridb.com/bagri-xdm\";\n" +
				"declare variable $docIds external;\n" + 
				"bgdm:remove-document($docIds)\n"; 

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    List<Object> docIds = new ArrayList<>();
	    docIds.add(uri);
	    xqpe.bindSequence(new QName("docIds"), xqConn.createSequence(docIds.iterator()));
	    XQSequence xqs = xqpe.executeQuery();
	    try {
		    if (xqs.next()) { 
		    	// must be false!
		    	throw new XQException("got unexpected result from bgdm:remove-document function");
		    }
	    } finally {
	    	xqpe.close();
	    	xqs.close();
	    }
	}

	private long storeDocument(String uri, String content) throws XQException {
		
		String query = "declare namespace bgdm=\"http://bagridb.com/bagri-xdm\";\n" +
				"declare variable $xml external;\n" + 
				"declare variable $docIds external;\n" + 
				"declare variable $props external;\n" + 
				"let $id := bgdm:store-document($xml, $docIds, $props)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    xqpe.bindString(new QName("xml"), content, xqConn.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    List<Object> docIds = new ArrayList<>();
	    docIds.add(uri);
	    xqpe.bindSequence(new QName("docIds"), xqConn.createSequence(docIds.iterator()));
	    List<String> props = new ArrayList<>(4);
	    props.add(xdm_document_data_format + "=xml");
	    xqpe.bindSequence(new QName("props"), xqConn.createSequence(props.iterator()));
	    XQSequence xqs = xqpe.executeQuery();
	    long result = -1;
	    if (xqs.next()) {
	    	result = xqs.getLong();
	    }
    	xqpe.close();
    	xqs.close();
    	return result;
	}
	
}
