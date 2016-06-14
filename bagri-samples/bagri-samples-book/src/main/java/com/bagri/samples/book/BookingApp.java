package com.bagri.samples.book;

import static com.bagri.common.util.FileUtils.readTextFile;
import static com.bagri.xdm.common.XDMConstants.xdm_document_collections;
import static com.bagri.xdm.common.XDMConstants.xdm_document_data_format;
import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;
import static com.bagri.xquery.api.XQUtils.getBaseTypeForTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;

import com.bagri.xdm.domain.XDMDocument;

public class BookingApp {
	
    private XQConnection xqc;

	public static void main(String[] args) throws XQException {
		
		//System.setProperty("xdm.schema.members", "localhost:10600");
		System.setProperty("logback.configurationFile", "hz-client-logging.xml");
		System.setProperty("xdm.log.level", "trace");
		//
		XQConnection xqc = null; //context.getBean("xqConnection", XQConnection.class);
		BookingApp client = new BookingApp(xqc);
		//HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
				
		boolean found = false;
		try {
			//client.storeSecCommand();
			long id = client.storeSecQuery();
			found = client.runSecQuery("IBM");
		} catch (XQException e) {
			e.printStackTrace();
		}

	    if (!found) {
	    	throw new XQException("result is empty");
	    }

	    //context.close();
	}
	
	public BookingApp(XQConnection xqc) {
		this.xqc = xqc;
	}
	
	public boolean runSecQuery(String symbol) throws XQException {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return $sec\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sym"), symbol, null);
	    XQResultSequence xqs = xqpe.executeQuery();
	    boolean found = false;
	    while (xqs.next()) {
			System.out.println(xqs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}

	public long storeSecQuery() throws XQException {
		return storeXmlDocument("security5621.xml");
	}
	
	private long storeXmlDocument(String fileName) throws XQException {
		
		String dName = "..\\..\\etc\\samples\\tpox\\";
		String xml;
		try {
			xml = readTextFile(dName + fileName);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}

		String query = "declare namespace bgdm=\"http://bagridb.com/bagri-xdm\";\n" +
				"declare variable $xml external;\n" + 
				"declare variable $docIds external;\n" + 
				"declare variable $props external;\n" + 
				//"declare option bgdm:document-format \"JSON\";\n\n" + 
				"let $id := bgdm:store-document($xml, $docIds, $props)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("xml"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    List docIds = new ArrayList(4);
	    docIds.add(new Long(0));
	    //docIds.add(new Long(1));
	    //docIds.add(new Integer(1));
	    docIds.add("65538.xml");
	    xqpe.bindSequence(new QName("docIds"), xqc.createSequence(docIds.iterator()));
	    List<String> props = new ArrayList<>(4);
	    props.add(xdm_document_data_format + "=xml");
	    props.add(xdm_document_collections + "=CLN_Custom, CLN_Security");
	    xqpe.bindSequence(new QName("props"), xqc.createSequence(props.iterator()));
	    XQSequence xqs = xqpe.executeQuery();
	    if (xqs.next()) {
	    	long id = xqs.getLong();
		    xqpe.close();
		    xqs.close();
		    return id;
	    } else {
	    	xqpe.close();
	    	xqs.close();
	    	throw new XQException("no response from store-document function");
	    }
	}

}

