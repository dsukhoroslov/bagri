package com.bagri.samples.xqj.client;

import static com.bagri.common.config.XDMConfigConstants.xdm_document_collections;
import static com.bagri.common.config.XDMConfigConstants.xdm_document_data_format;
import static com.bagri.xdm.common.XDMConstants.pn_client_dataFactory;
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
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import com.bagri.samples.xdm.client.XDMClientApp;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xqj.BagriXQDataSource;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.XQProcessorClient;

public class XQJClientApp {

	private XQConnection xqConn;
	    
	public static void main(String[] args) throws XQException {
		
		if (args.length < 4) {
			throw new XQException("wrong number of arguments passed. Expected: schemaAddress schemaName userName password");
		}
		
		Properties props = new Properties();
	    props.setProperty(pn_schema_address, args[0]);
	    props.setProperty(pn_schema_name, args[1]);
	    props.setProperty(pn_schema_user, args[2]);
	    props.setProperty(pn_schema_password, args[3]);
		XQJClientApp client = new XQJClientApp(props); 

		
		String uri = "test_document";
		try {
			String xml = "<content uri=\"" + uri + "\">XML Content</content>";
			long id = client.createDocument(uri, xml);
			if (id < 0) {
				System.out.println("ERROR: document was not created");
				return;
			}
			xml = "<content uri=\"" + uri + "\">Updated XML Content</content>";
			id = client.updateDocument(uri, id, xml);
			if (id < 0) {
				System.out.println("ERROR: document was not updated");
				return;
			}
			xml = client.readDocument(uri);
			if (xml != null) {
				System.out.println("got document: " + xml);
			} else {
				System.out.println("ERROR: document was not read");
				//return;
			}
			client.deleteDocument(uri, id);
			xml = client.readDocument(uri);
			if (xml != null) {
				System.out.println("ERROR: document still exists: " + xml);
			}
		} finally {
			client.xqConn.close();
		}
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
	
	public long createDocument(String uri, String content) throws XQException {
		
		String query = "declare namespace bgdm=\"http://bagri.com/bagri-xdm\";\n" +
				"declare variable $xml external;\n" + 
				"declare variable $docIds external;\n" + 
				"declare variable $props external;\n" + 
				"let $id := bgdm:store-document($xml, $docIds, $props)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    xqpe.bindString(new QName("xml"), content, xqConn.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    List docIds = new ArrayList();
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
	
	public String readDocument(String uri) throws XQException {
		
		String query = "declare variable $uri external;\n" + 
				"for $doc in fn:collection()/content\n" +
		  		"where $doc/@uri=$uri\n" + 
				"return $doc\n";

		    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
		    xqpe.bindString(new QName("uri"), uri, null);
		    XQResultSequence xqs = xqpe.executeQuery();
		    String result = null;
		    if (xqs.next()) {
				result = xqs.getItemAsString(null);
		    }
		    return result;
	}
	
	public long updateDocument(String uri, long id, String content) throws XQException {
		
		String query = "declare namespace bgdm=\"http://bagri.com/bagri-xdm\";\n" +
				"declare variable $xml external;\n" + 
				"declare variable $docIds external;\n" + 
				"declare variable $props external;\n" + 
				"let $id := bgdm:store-document($xml, $docIds, $props)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    xqpe.bindString(new QName("xml"), content, xqConn.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    List docIds = new ArrayList();
	    docIds.add(new Long(id));
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
	
	public void deleteDocument(String uri, long id) throws XQException {

		String query = "declare namespace bgdm=\"http://bagri.com/bagri-xdm\";\n" +
				"declare variable $docIds external;\n" + 
				"bgdm:remove-document($docIds)\n"; // +
				//"return\n";

	    XQPreparedExpression xqpe = xqConn.prepareExpression(query);
	    List docIds = new ArrayList();
	    docIds.add(new Long(id));
	    docIds.add(uri);
	    xqpe.bindSequence(new QName("docIds"), xqConn.createSequence(docIds.iterator()));
	    XQSequence xqs = xqpe.executeQuery();
	    xqs.next(); // must be false!
    	xqpe.close();
    	xqs.close();
	}

    
}
