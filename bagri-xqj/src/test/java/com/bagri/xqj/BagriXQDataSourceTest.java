package com.bagri.xqj;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQCancelledException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.xdm.api.XDMException;

import static com.bagri.common.util.FileUtils.readTextFile;

public class BagriXQDataSourceTest {
	
	private XQDataSource xqds;

    @Before
    public void setUp() throws XQException {
    	xqds = new BagriXQDataSource();
	    //xqds.setProperty(BagriXQDataSource.HOST, "127.0.0.1");
	    //xqds.setProperty(BagriXQDataSource.PORT, "5701");
	    xqds.setProperty(BagriXQDataSource.ADDRESS, "localhost:10500");
	    xqds.setProperty(BagriXQDataSource.SCHEMA, "default");
	    xqds.setProperty(BagriXQDataSource.USER, "guest");
	    xqds.setProperty(BagriXQDataSource.PASSWORD, "password");
	    //xqds.setProperty("hz.cache.mode", "client");
	    xqds.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
	    xqds.setProperty(BagriXQDataSource.XDM_REPOSITORY, "com.bagri.xdm.client.hazelcast.impl.RepositoryImpl");
    }
	
	@Test
	public void getConnectionTest() throws XQException {
		XQConnection conn = xqds.getConnection();
		assertNotNull(conn);
		assertFalse(conn.isClosed());
		conn.close();
		assertTrue(conn.isClosed());
	}

	@Test
	@Ignore
	public void getConnectionWithCredentialsTest() throws XQException {
    	String username = "test";
		String password = "test";
		XQConnection conn = xqds.getConnection(username, password);
		assertNull(conn);
		//assertFalse(conn.isClosed());
		//conn.close();
	}
	
	@Test
	@Ignore
	public void testLoginTimeout() throws XQException {
		xqds.setLoginTimeout(1);
		XQConnection conn = xqds.getConnection();
		assertNull(conn);
		xqds.setLoginTimeout(5);
		conn = xqds.getConnection();
		assertNotNull(conn);
		conn.close();
	}

	@Test
	public void testQueryCancel() throws XQException {

		XQConnection xqc = xqds.getConnection();
		assertNotNull(xqc);
		assertFalse(xqc.isClosed());
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $v external;\n" + 
			"for $sec in fn:collection(\"http://tpox-benchmark.com/security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$v\n" + 
			"return\n" +   
			"\t<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>\n";

	    final XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("v"), "IBM", null);
	    
		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(5);
					xqpe.cancel();
				} catch (Exception ex) {
					//
				}
			}
		});
		th1.start();

		try {
			XQResultSequence xqs = xqpe.executeQuery();
		    assertFalse(xqs.next());
		    xqs.close();
		} catch (XQCancelledException ex) {
			// unticipated ex
		}
		xqpe.close();
		xqc.close();
	}

	@Test
	public void testQueryTimeout() throws XQException {

		XQConnection xqc = xqds.getConnection();
		assertNotNull(xqc);
		assertFalse(xqc.isClosed());

		String dName = "..\\etc\\samples\\tpox\\";
		String xml;
		try {
			xml = readTextFile(dName + "security5621.xml");
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		
		String query = "declare namespace bgdm=\"http://bagridb.com/bagri-xdm\";\n" +
				"declare variable $xml external;\n" + 
				"let $id := bgdm:store-document($xml)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("xml"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQSequence xqs = xqpe.executeQuery();
	    assertTrue(xqs.next());
	    xqs.close();
	    xqpe.close();
		
		xqc.getStaticContext().setQueryTimeout(3); // 1 sec timeout
		query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sect external;\n" + 
				"declare variable $pemin external;\n" +
				"declare variable $pemax external;\n" + 
				"declare variable $yield external;\n" + 
				"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/Security\n" +
		  		"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
				"return	<Security>\n" +	
				"\t{$sec/Symbol}\n" +
				"\t{$sec/Name}\n" +
				"\t{$sec/SecurityType}\n" +
				"\t{$sec/SecurityInformation//Sector}\n" +
				"\t{$sec/PE}\n" +
				"\t{$sec/Yield}\n" +
				"</Security>";

		xqpe = xqc.prepareExpression(query);
		xqpe.bindString(new QName("sect"), "Technology", null);
		xqpe.bindFloat(new QName("pemin"), 25.0f, null);
		xqpe.bindFloat(new QName("pemax"), 28.0f, null);
		xqpe.bindFloat(new QName("yield"), 0.0f, null);
		
		try {
			xqs = xqpe.executeQuery();
			assertFalse(xqs.next());
		} catch (XQException ex) {
			// must be timeout exception
			assertTrue(XDMException.ecQueryTimeout == Integer.parseInt(ex.getVendorCode()));
		}
		xqs.close();
		xqpe.close();
		xqc.close();
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
	
	@Test
	public void testCreateItemFromDocumentString() throws XQException {

	    XQConnection xqc = xqds.getConnection();
		try {
			xqc.createItemFromDocument((String)null, null, null);
		    fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
		} catch (XQException e) {
			// Expect an XQException
		}    

		boolean failed = false;
		try {
			XQItem xqitem = xqc.createItemFromDocument("<e>Hello world!</e>", null, xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
		    // conversion succeeded, we're having implementation defined behaviour
		    // but at least the XDM instance must be of the right type.
		    if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC) {
		        failed = true;
		    }
		    if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN) {
		        failed = true;
		    }
		} catch (XQException e) {
		    // Expect an XQException
		}   
		if (failed) {
			fail("A-XQDF-1.3: The conversion is subject to the following constraints. " +
					"Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");
		}

		try {
			XQItem xqitem = xqc.createItemFromDocument("<e>", null, null);
		    fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
		} catch (XQException e) {
		    // Expect an XQException
		}    

		XQItem xqi = null;
		try {
		    xqi = xqc.createItemFromDocument("<e>Hello world!</e>", null, null);
		} catch (XQException e) {
		    fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
		}
		String result = xqi.getItemAsString(null);
		assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" 
					+ result + "'", result.indexOf("<e>Hello world!</e>") != -1);
	}
}
