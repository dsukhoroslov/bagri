package com.bagri.xqj;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BagriXQSequenceTest {

	private XQConnection xqc;

	@Before
	public void setUp() throws Exception {
	    XQDataSource xqds = new BagriXQDataSource();
	    xqds.setProperty(BagriXQDataSource.ADDRESS, "localhost:10500");
	    xqds.setProperty(BagriXQDataSource.SCHEMA, "default");
	    xqds.setProperty(BagriXQDataSource.USER, "guest");
	    xqds.setProperty(BagriXQDataSource.PASSWORD, "password");
	    xqds.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
	    xqds.setProperty(BagriXQDataSource.XDM_REPOSITORY, "com.bagri.client.hazelcast.impl.SchemaRepositoryImpl");
		xqc = xqds.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		if (xqc != null)
			xqc.close();	
	}

	@Test
	public void testGetItem() throws XQException {
		
		XQExpression xqe = xqc.createExpression();
		XQSequence xqs = xqe.executeQuery("1,2,3,4");
	    xqs.next();
	    xqs.close();
	    try {
	    	xqs.getItem();
	    	fail("A-XQS-1.2: closed sequence supports getItem()");
	    } catch (XQException e) {
	    	// Expect an XQException
	    }
	    xqe.close();
	}
	
	@Test
	public void testWriteSequence() throws XQException {

		XQExpression xqe = xqc.createExpression();
		XQSequence xqs = xqe.executeQuery("<e>Hello world!</e>");

		StringWriter result = new StringWriter();
	    Properties prop = new Properties();
	    prop.setProperty("encoding", "UTF-8");

	    try {
	    	xqs.writeSequence(result, prop);
	    } catch (XQException e) {
	    	fail("A-XQS-24.1: writeSequence failed with message: " + e.getMessage());
	    }
	    assertTrue("A-XQS-24.1: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result.toString() + "'", result.toString().indexOf("<e>Hello world!</e>") != -1);
	    xqe.close();
	}
	
	@Test
	public void testGetBoolean() throws XQException {
		
	    XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("xs:boolean('true')");
	    xqs.next();
	    xqs.getItem();
	    try {
	    	xqs.getBoolean();
	    	fail("A-XQIA-1.4: SCROLLTYPE_FORWARD_ONLY sequence supports getting item twice()");
	    } catch (XQException e) {
	      // Expect an XQException
	    }
	    xqe.close();
	}

	@Test
	public void testBindSequence() throws XQException {

	    XQExpression xqe = xqc.createExpression();
	    boolean failed = false;
	    try {
	    	XQSequence xqs = xqe.executeQuery("<e>Hello world!</e>");
	    	XQItem xqitem = xqc.createItemFromDocument(xqs.getSequenceAsStream(), xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
	    	// conversion succeeded, we're having implementation defined behaviour
	    	// but at least the XDM instance must be of the right type.
	    	if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC)
	    		failed = true;
	    	if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN)
	    		failed = true;
	    } catch (XQException e) {
	    	// Expect an XQException
	    }   
	    if (failed)
	    	fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");    

	    XQItem xqi = null;
	    try {
	    	XQSequence xqs = xqe.executeQuery("<e>Hello world!</e>");
	    	xqi = xqc.createItemFromDocument(xqs.getSequenceAsStream(), null);
	    } catch (XQException e) {
	    	fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
	    }
	    String result = xqi.getItemAsString(null);
	    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + "'", result.indexOf("<e>Hello world!</e>") != -1);
	    xqe.close();
	}
	
	@Test
	public void testBindSequence2() throws XQException {
		XQPreparedExpression xqpe;
		    
		// Create an XQSequence, which we will use subsequently to test bindSequence()
		XQExpression xqe = xqc.createExpression();
		XQSequence xqs = xqc.createSequence(xqe.executeQuery("'Hello world!'"));
		xqe.close();
		    
		xqs.beforeFirst();
		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		xqpe.close();
		try {
		    xqpe.bindSequence(new QName("v"), xqs);
		    fail("A-XQDC-1.1: bindSequence() throws an XQException when the dynamic context is in closed state.");
		} catch (XQException e) {
		    // Expect an XQException
		}

		xqs.beforeFirst();
		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
		    xqpe.bindSequence(null, xqs);
		    fail("A-XQDC-1.2: null argument is invalid and throws an XQException.");
		} catch (XQException e) {
		    // Expect an XQException
		}    
		xqpe.close();

		xqs.beforeFirst();
		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
		    xqpe.bindSequence(new QName("foo"), xqs);
		    fail("A-XQDC-1.5: The bound variable must be declared external in the prepared expression.");
		} catch (XQException e) {
		    // Expect an XQException
		}  
		xqpe.close();

		xqs.beforeFirst();
		xqpe = xqc.prepareExpression("declare variable $v as xs:decimal external; $v");
		try {
		    xqpe.bindSequence(new QName("v"), xqs);
		    xqpe.executeQuery().getSequenceAsString(null);
		    fail("A-XQDC-1.6: The dynamic type of the bound value is not compatible with the static type of the variable and must fail.");
		} catch (XQException e) {
		    // Expect an XQException
		}    
		xqpe.close();

		xqs.beforeFirst();
		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
		    xqpe.bindSequence(new QName("v"),xqs);
		} catch (XQException e) {
		    fail("A-XQDC-1.7: bindSequence() failed with message: " + e.getMessage());
		}
		XQSequence xqs2 = xqpe.executeQuery();
		xqs2.next();
		assertEquals("A-XQDC-1.7: Successful bindXXX.", "Hello world!", xqs2.getAtomicValue());
		xqpe.close();
		    
		xqs.close();
		xqpe = xqc.prepareExpression("declare variable $v as xs:decimal external; $v");
		try {
		    xqpe.bindSequence(new QName("v"), xqs);
		    fail("A-XQDC-1.8: Passing a closed XQItem or XQSequence object as argument must result in an XQException.");
		} catch (XQException e) {
		    // Expect an XQException
		}    
		xqpe.close();
	}

	@Test
	public void testCreateSequence() throws XQException {
		
	    XQPreparedExpression xqpe = xqc.prepareExpression("'Hello world!'");
	    
	    XQSequence xqsresult = null;
	    XQSequence xqs = xqpe.executeQuery();
	    try {
	    	xqsresult = xqc.createSequence(xqs);
	    } catch (XQException e) {
	    	fail("A-XQDF-1.5: createSequence() failed with message: " + e.getMessage());
	    }
	    String result = xqsresult.getSequenceAsString(null);
	    assertTrue("A-XQDF-1.5: Expects serialized result contains 'Hello world!', but it is '" + result + "'", result.indexOf("Hello world!") != -1);
	}
	
	@Test
	public void testFirst() throws XQException {
		XQExpression xqe;
		XQSequence xqs;
		boolean b = false;

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("1,2,3,4");
		try {
			xqs.first();
			fail("A-XQS-1.1: SCROLLTYPE_FORWARD_ONLY sequence supports first()");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		XQStaticContext xqsc = xqc.getStaticContext();
		xqsc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);
		xqc.setStaticContext(xqsc);

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("1,2,3,4");
		xqs.close();
		try {
			xqs.first();
			fail("A-XQS-1.2: closed sequence supports first()");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("1,2,3,4");
		try {
			b = xqs.first();
		} catch (XQException e) {
			fail("A-XQS-11.1: first() failed with message: " + e.getMessage());
		}
		assertEquals("A-XQS-11.1: first() failed", true,	b);
		assertEquals("A-XQS-11.1: first() failed", 1, xqs.getInt());
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("()");
		try {
			b = xqs.first();
		} catch (XQException e) {
			fail("A-XQS-11.2: first() failed with message: " + e.getMessage());
		}
		assertEquals("A-XQS-11.2: first() failed", false, b);
		xqe.close();
	}

	@Test
	public void testBindDocument() throws XQException {
		XQPreparedExpression xqpe;

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		xqpe.close();
		try {
			xqpe.bindDocument(new QName("v"), "<e>Hello world!</e>", null, null);
			fail("A-XQDC-1.1: bindDocument() throws an XQException when the dynamic context is in closed state.");
		} catch (XQException e) {
			// Expect an XQException
		}

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindDocument(null, "<e>Hello world!</e>", null, null);
			fail("A-XQDC-1.2: null argument is invalid and throws an XQException.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		boolean bindFailed = false;
		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindDocument(new QName("v"), "<e>Hello world!</e>", null,
					xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
		} catch (XQException e) {
			bindFailed = true;
			// Expect an XQException
		}
		if (!bindFailed) {
			XQSequence xqs = xqpe.executeQuery();
			xqs.next();
			if (xqs.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC) {
				fail("A-XQDC-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");
			}
			if (xqs.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN) {
				fail("A-XQDC-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");
			}
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindDocument(new QName("v"), "<e>", null, null);
			xqpe.executeQuery().getSequenceAsString(null);
			fail("A-XQDC-1.4: The conversion of the value to an XDM instance must fail.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindDocument(new QName("foo"), "<e>Hello world!</e>", null, null);
			fail("A-XQDC-1.5: The bound variable must be declared external in the prepared expression.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v as xs:decimal external; $v");
		try {
			xqpe.bindDocument(new QName("v"), "<e>Hello world!</e>", null, null);
			xqpe.executeQuery().getSequenceAsString(null);
			fail("A-XQDC-1.6: The dynamic type of the bound value is not compatible with the static type of the variable and must fail.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindDocument(new QName("v"), "<e>Hello world!</e>", null, null);
		} catch (XQException e) {
			fail("A-XQDC-1.7: bindDocument() failed with message: " + e.getMessage());
		}
		String result = xqpe.executeQuery().getSequenceAsString(null);
		assertTrue("A-XQDC-1.7: Expects serialized result contains '<e>Hello world!</e>', but it is '"
								+ result + "'",	result.indexOf("<e>Hello world!</e>") != -1);
		xqpe.close();
	}

	@Test
	public void testStaticTypingSupport() throws XQException {

		XQMetaData xqmd = xqc.getMetaData();
		boolean supportsStaticTyping = true;

		try {
			supportsStaticTyping = xqmd.isStaticTypingFeatureSupported();
		} catch (XQException e) {
			fail("A-XQMD-1.2: XQMetaData method failed with message: " + e.getMessage());
		}

		if (supportsStaticTyping) {
			try {
				// According to the XQuery Formal Semantics, the following query
				// must result in a static type error.
				xqc.prepareExpression("declare variable $v as item() external; $v + 1");
				fail("A-XQMD-4.1: XQMetaData reports support for static typing feature, successfully generate a static type error.");
			} catch (XQException e) {
				// Expect an xQException
			}
		}
	}

	@Test
	public void testWriteSequence_OutputStream() throws XQException, UnsupportedEncodingException {

		// We don't expect this method ever to throw
		// UnsupportedEncodingException, as we only request the "UTF-8"
		// encoding.
		// However, in order to make the compiler happy, and to play it safe,
		// add UnsupportedEncodingException to the throws clause.

		XQExpression xqe;
		XQSequence xqs;

		Properties prop = new Properties();
		prop.setProperty("encoding", "UTF-8");

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		xqs.close();
		try {
			xqs.writeSequence(new ByteArrayOutputStream(), prop);
			fail("A-XQS-1.2: closed sequence supports writeSequence()");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		xqs.next();
		xqs.getItem();
		try {
			xqs.writeSequence(new ByteArrayOutputStream(), prop);
			fail("A-XQS-21.2: SCROLLTYPE_FORWARD_ONLY sequence, getXXX() or write() method has been invoked already on the current item");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e a='Hello world!'/>/@*");
		try {
			xqs.writeSequence(new ByteArrayOutputStream(), prop);
			fail("A-XQS-21.1: serialization process fails when sequence contains a top-level attribute");
		} catch (XQException xq) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		try {
			xqs.writeSequence((OutputStream) null, prop);
			fail("A-XQS-24.3: writeSequence accepts a null buffer as first argument");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try {
			xqs.writeSequence(result, prop);
		} catch (XQException e) {
			fail("A-XQS-24.1: writeSequence failed with message: " + e.getMessage());
		}
		assertTrue("A-XQS-24.1: Expects serialized result contains '<e>Hello world!</e>', but it is '"
						+ result.toString("UTF-8") + "'",
				result.toString("UTF-8").indexOf("<e>Hello world!</e>") != -1);
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		ByteArrayOutputStream otherResult = new ByteArrayOutputStream();
		try {
			xqs.writeSequence(otherResult, prop);
		} catch (XQException e) {
			fail("A-XQS-24.2: writeSequence failed with message: " + e.getMessage());
		}
		assertEquals("A-XQS-24.2: null properties argument is equivalent to empty properties argument",
				result.toString("UTF-8"), otherResult.toString("UTF-8"));
		xqe.close();
	}

	@Test
	public void testWriteSequence_Writer() throws XQException {
		XQExpression xqe;
		XQSequence xqs;

		Properties prop = new Properties();
		prop.setProperty("encoding", "UTF-8");

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		xqs.close();
		try {
			xqs.writeSequence(new StringWriter(), prop);
			fail("A-XQS-1.2: closed sequence supports writeSequence()");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		xqs.next();
		xqs.getItem();
		try {
			xqs.writeSequence(new StringWriter(), prop);
			fail("A-XQS-21.2: SCROLLTYPE_FORWARD_ONLY sequence, getXXX() or write() method has been invoked already on the current item");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e a='Hello world!'/>/@*");
		try {
			xqs.writeSequence(new StringWriter(), prop);
			fail("A-XQS-21.1: serialization process fails when sequence contains a top-level attribute");
		} catch (XQException xq) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		try {
			xqs.writeSequence((Writer) null, prop);
			fail("A-XQS-24.3: writeSequence accepts a null buffer as first argument");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		StringWriter result = new StringWriter();
		try {
			xqs.writeSequence(result, prop);
		} catch (XQException e) {
			fail("A-XQS-24.1: writeSequence failed with message: " + e.getMessage());
		}
		assertTrue("A-XQS-24.1: Expects serialized result contains '<e>Hello world!</e>', but it is '"
						+ result.toString() + "'", result.toString().indexOf("<e>Hello world!</e>") != -1);
		xqe.close();

		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("<e>Hello world!</e>");
		StringWriter otherResult = new StringWriter();
		try {
			xqs.writeSequence(otherResult, prop);
		} catch (XQException e) {
			fail("A-XQS-24.2: writeSequence failed with message: " + e.getMessage());
		}
		assertEquals("A-XQS-24.2: null properties argument is equivalent to empty properties argument", result.toString(),
				otherResult.toString());
		xqe.close();
	}

}
