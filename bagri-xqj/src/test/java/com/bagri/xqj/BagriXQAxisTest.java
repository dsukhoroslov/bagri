package com.bagri.xqj;

import static com.bagri.common.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class BagriXQAxisTest {

	private static XQDataSource xqds;
	private static long axisId;
	private XQConnection xqc;

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		String dName = "..\\..\\etc\\samples\\test\\";
		String xml;
		try {
			xml = readTextFile(dName + "axis.xml");
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}

		xqds = new BagriXQDataSource();
	    xqds.setProperty(BagriXQDataSource.ADDRESS, "localhost:10500");
	    xqds.setProperty(BagriXQDataSource.SCHEMA, "default");
	    xqds.setProperty(BagriXQDataSource.USER, "guest");
	    xqds.setProperty(BagriXQDataSource.PASSWORD, "password");
	    xqds.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
	    xqds.setProperty(BagriXQDataSource.XDM_REPOSITORY, "com.bagri.xdm.client.hazelcast.impl.SchemaRepositoryImpl");

		String query = "declare namespace bgdm=\"http://bagridb.com/bagri-xdm\";\n" +
				"declare variable $sec external;\n\n" + 
				"for $id in bgdm:store-document($sec)\n" +
				"return $id\n";

		XQConnection xqc = xqds.getConnection();
		try {
			XQPreparedExpression xqpe = xqc.prepareExpression(query);
		    xqpe.bindString(new QName("sec"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
		    XQSequence xqs = xqpe.executeQuery();
		    if (xqs.next()) {
		    	axisId = xqs.getLong();
			    xqpe.close();
		    } else {
		    	xqpe.close();
		    	throw new XQException("no response from store-document function");
		    }
		} finally {
			xqc.close();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		XQConnection xqc = xqds.getConnection();
		try {
			XQExpression xqe = xqc.createExpression();
			xqe.bindLong(new QName("docId"), axisId, xqc.createAtomicType(XQItemType.XQBASETYPE_LONG));
		    xqe.executeCommand("removeDocument($docId)");
		    xqe.close();
		} finally {
			xqc.close();
		}
	}

	@Before
	public void setUp() throws Exception {
		xqc = xqds.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		if (xqc != null)
			xqc.close();	
	}
	
	//@Test
	public void queryChildTest() throws XQException {
		String query = //"declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				//"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
				"for $e in fn:collection(\"/document\")/document\n" +
		  		//"where $sec/s:Symbol=$sym\n" + //'IBM'\n" +
				"return $e//child\n";
		
		XQExpression xqe = xqc.createExpression();
		//xqe.bindString(new QName("doc"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQResultSequence xqrs = xqe.executeQuery(query);

	    Assert.assertNotNull(xqrs);
		Assert.assertFalse(xqrs.isClosed());

	    boolean found = false;
	    while (xqrs.next()) {
			System.out.println(xqrs.getItemAsString(null));
			found = true;
	    }
	}

	@Test
	public void queryParentTest() throws XQException {
		String query = //"declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				//"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
				"for $e in fn:collection(\"/document\")\n" + // /document\n" +
		  		//"where $e//child/parent::*[fn:count(*)=3]\n" + 
		  		//"where fn:count($e//child/parent::*)=3\n" + 
		  		"where $e/document/root//*[.='IBM']\n" + 
				"return $e\n";
		
		// starts with /document/root;
		// descendants of document/root; kind = text; 
		
		XQExpression xqe = xqc.createExpression();
	    XQResultSequence xqrs = xqe.executeQuery(query);

	    Assert.assertNotNull(xqrs);
		Assert.assertFalse(xqrs.isClosed());

	    boolean found = false;
	    while (xqrs.next()) {
			System.out.println(xqrs.getItemAsString(null));
			found = true;
	    }
	}

}
