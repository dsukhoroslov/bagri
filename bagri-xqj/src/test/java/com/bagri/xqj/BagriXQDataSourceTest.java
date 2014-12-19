package com.bagri.xqj;

import static org.junit.Assert.assertTrue;

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

import org.junit.Before;
import org.junit.Test;

import static com.bagri.common.util.FileUtils.readTextFile;

public class BagriXQDataSourceTest {
	
	private XQDataSource xqds;

    @Before
    public void setUp() throws XQException {
    	xqds = new BagriXQDataSource();
	    //xqds.setProperty(BagriXQDataSource.HOST, "127.0.0.1");
	    //xqds.setProperty(BagriXQDataSource.PORT, "5701");
	    xqds.setProperty(BagriXQDataSource.ADDRESS, "localhost:10500");
	    xqds.setProperty(BagriXQDataSource.SCHEMA, "TPoX2");
	    xqds.setProperty(BagriXQDataSource.PASSWORD, "TPoX2");
	    //xqds.setProperty("hz.cache.mode", "client");
	    xqds.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.BagriXQProcessorProxy");
	    xqds.setProperty(BagriXQDataSource.XDM_MANAGER, "com.bagri.xdm.access.hazelcast.impl.DocumentManagementClient");
    }
	
	@Test
	public void getConnectionTest() throws XQException {
		XQConnection conn = xqds.getConnection();
		Assert.assertNotNull(conn);
		Assert.assertFalse(conn.isClosed());
		conn.close();
		Assert.assertTrue(conn.isClosed());
	}

	//@Test
	public void getConnectionWithCredentialsTest() throws XQException {
    	String username = "test";
		String password = "test";
		XQConnection conn = xqds.getConnection(username, password);
		Assert.assertNotNull(conn);
		Assert.assertFalse(conn.isClosed());
	}

	@Test
	public void testQuerySecurity() throws XQException {
		XQConnection xqc = xqds.getConnection();
		Assert.assertNotNull(xqc);
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $v external;\n" + // $v\n" +
			//"for $sec in fn:doc(\"sdoc\")/s:Security\n" +
			"for $sec in fn:collection(\"http://tpox-benchmark.com/security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$v\n" + //'IBM'\n" +
			"return\n" +   
			"\t<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("v"), "IBM", null);
	    XQResultSequence xqs = xqpe.executeQuery();
	    Assert.assertTrue(xqs.next());
	}

	@Test
	public void testStoreSecurity() throws XQException {

		String dName = "..\\etc\\samples\\tpox\\";
		String xml;
		try {
			xml = readTextFile(dName + "security1500.xml");
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		
		XQConnection xqc = xqds.getConnection();
		XQExpression xqe = xqc.createExpression();
		//xqe.bindDocument(new QName("s"), xml, "http://tpox-benchmark.com/security", xqc.createDocumentType());
		xqe.bindString(new QName("s"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    xqe.executeCommand("storeDocument($s)");
	    //xqs.next();
	    //assertTrue("expected true but got false", xqs.getBoolean());
	    xqe.close();
	}
	
}
