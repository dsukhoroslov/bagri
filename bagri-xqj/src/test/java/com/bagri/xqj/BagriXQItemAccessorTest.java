package com.bagri.xqj;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BagriXQItemAccessorTest {
	
	private XQConnection xqc;

	@Before
	public void setUp() throws Exception {
	    XQDataSource xqds = new BagriXQDataSource();
	    //xqds.setProperty(BagriXQDataSource.HOST, "127.0.0.1");
	    //xqds.setProperty(BagriXQDataSource.PORT, "5701");
	    xqds.setProperty(BagriXQDataSource.ADDRESS, "localhost:10500");
	    xqds.setProperty(BagriXQDataSource.SCHEMA, "TPoX2");
	    xqds.setProperty(BagriXQDataSource.PASSWORD, "TPoX2");
	    xqds.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.BagriXQProcessorProxy");
	    xqds.setProperty(BagriXQDataSource.XDM_MANAGER, "com.bagri.xdm.client.hazelcast.impl.DocumentManagementClient");
		xqc = xqds.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		if (xqc != null) {
			xqc.close();
		}
	}

	@Test
	//@Ignore
	public void testGetByte() throws XQException {
		
		XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("'1'");
	    xqs.next();
		try {
			xqs.getByte();
			Assert.fail("A-XQIA-1.1: conversion to byte should fail");
		} catch (XQException e) {
		    // Expect an XQException
		}
		xqe.close();

	    xqe = xqc.createExpression();
	    xqs = xqe.executeQuery("xs:byte('1')");
	    //try {
	    	//xqs.getByte();
	    	//Assert.fail("A-XQIA-1.2: getXXX() should fail when not positioned on an item");
	    xqs.next();
	    Object o = xqs.getObject();
	        Assert.assertTrue("expected Byte but got " + o.getClass().getName(), o instanceof Byte);
	    //} catch (XQException e) {
	    	// Expect an XQException
	    //}
	    xqe.close();
	    
	    XQPreparedExpression xqpe = xqc.prepareExpression("declare variable $v external; $v");
	    try {
	    	xqpe.bindByte(new QName("v"), (byte)1, xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
	    } catch (XQException e) {
	    	Assert.fail("A-XQDC-1.7: bindByte() failed with message: " + e.getMessage());
	    }
	    xqs = xqpe.executeQuery();
	    xqs.next();
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQITEMKIND_ATOMIC, xqs.getItemType().getItemKind());
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQBASETYPE_INTEGER, xqs.getItemType().getBaseType());
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", "1", xqs.getAtomicValue());
	    xqpe.close(); 
	}

	@Test
	public void testGetInt() throws XQException {

	    XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("xs:int('1'), 10.0");
	    xqs.next();
	    try {
	    	Assert.assertEquals("A-XQIA-1.6: getInt on xs:int failed", 1, xqs.getInt());
	    } catch (XQException e) {
	    	Assert.fail("A-XQIA-1.6: getInt on xs:int failed with message: " + e.getMessage());
	    }
	    xqs.next();
	    try {
	    	Assert.assertEquals("A-XQIA-1.6: getInt on xs:decimal failed", 10, xqs.getInt());
	    } catch (XQException e) {
	    	Assert.fail("A-XQIA-1.6: getInt on xs:decimal failed with message: " + e.getMessage());
	    }
	    xqe.close();
	}

	@Test
	public void testGetDouble() throws XQException {
		
	    XQPreparedExpression xqpe = xqc.prepareExpression("declare variable $v external; $v");
	    try {
	    	xqpe.bindDouble(new QName("v"), 1d, xqc.createAtomicType(XQItemType.XQBASETYPE_DOUBLE));
	    } catch (XQException e) {
	    	Assert.fail("A-XQDC-1.7: bindDouble() failed with message: " + e.getMessage());
	    }
	    XQSequence xqs = xqpe.executeQuery();
	    xqs.next();
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQITEMKIND_ATOMIC, xqs.getItemType().getItemKind());
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQBASETYPE_DOUBLE, xqs.getItemType().getBaseType());
	    Assert.assertTrue("A-XQDC-1.7: Successful bindXXX.", 1d == xqs.getDouble());
	    xqpe.close(); 
	}

	@Test
	public void testGetBoolean() throws XQException {

		XQExpression xqe = xqc.createExpression();
	    xqe.bindObject(new QName("v"), Boolean.valueOf(true), null);
		xqe.bindBoolean(new QName("v"), true, xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
	    XQSequence xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:boolean");
	    xqs.next();
	    assertTrue("expected true but got false", xqs.getBoolean());
	    //xqe.close();
	    
	    //xqe = xqc.createExpression();
	    xqe.bindObject(new QName("v"), new Byte((byte)1), null);
	    xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:byte");
	    xqs.next();
	    assertTrue("expected true (byte) but got false", xqs.getBoolean());
	}
	
	@Test
	public void testSequence() throws XQException {
		
		String query = "<e>Hello world!</e>";
		XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery(query);
	    Assert.assertTrue(xqs.next());
	    String value = xqs.getItemAsString(null);
		xqe.close();
		Assert.assertEquals(query, value);
	}

	@Test
	public void testNamespace() throws XQException {
		XQStaticContext xqsc = xqc.getStaticContext(); 
	    xqsc.declareNamespace("foo", "http://www.foo.com");
	    XQExpression  xqe = xqc.createExpression(xqsc);
	    XQSequence xqs = xqe.executeQuery("<foo:e/>");
	    xqe.close();
	}
	
	@Test
	public void testBinding() throws XQException {
	    XQPreparedExpression xqpe = xqc.prepareExpression("declare variable $v external; $v");
	    xqpe.bindString(new QName("v"), "Hello world!", xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQSequence xqs = xqpe.executeQuery();
	    Assert.assertTrue(xqs.next());
	    Assert.assertEquals("Hello world!", xqs.getAtomicValue());
	    xqpe.close();
	    
	    xqpe = xqc.prepareExpression("declare variable $v external; $v");
	    xqpe.bindString(new QName("v"), "Hello", xqc.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
	    xqs = xqpe.executeQuery();
	    xqs.next();
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQITEMKIND_ATOMIC, xqs.getItemType().getItemKind());
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQBASETYPE_NCNAME, xqs.getItemType().getBaseType());
	    Assert.assertEquals("A-XQDC-1.7: Successful bindXXX.", "Hello", xqs.getObject());
	    xqpe.close(); 
	}
	
	@Test
	public void testBasicType() throws XQException {
		
	    XQExpression  xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("1,2");
	    boolean b = xqs.next();
	    Assert.assertEquals("A-XQS-18.1: next() failed", true, b);
	    int type = xqs.getInt();
	    Assert.assertEquals("A-XQS-18.1: next() failed", 1, type);
	}
	
	@Test
	public void testGetNode() throws XQException, ParserConfigurationException, SAXException, IOException {
	    
		Node node = null;
		XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("<e/>, <e a=''/>/@*");
	    xqs.next();
	    try {
	    	node = xqs.getNode();
	    } catch (XQException e) {
	    	Assert.fail("A-XQIA-3.1: getNode on element() failed with message: " + e.getMessage());
	    }
	    Assert.assertEquals("A-XQIA-3.1: getNode on element() failed", true, node instanceof Element);
	    Assert.assertEquals("A-XQIA-3.1: getNode on element() failed", "e", node.getLocalName());
	    xqs.next();
	    try {
	    	node = xqs.getNode();
	    } catch (XQException e) {
	    	Assert.fail("A-XQIA-3.1: getNode on attribute() failed with message: " + e.getMessage());
	    }
	    Assert.assertEquals("A-XQIA-3.1: getNode on attribute() failed", true, node instanceof Attr);
	    Assert.assertEquals("A-XQIA-3.1: getNode on attribute() failed", "a", node.getLocalName());
	    xqe.close();
	    
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document document = parser.parse(new InputSource(new StringReader("<e>Hello world!</e>")));
	    //Assert.assertEquals("getLocalName on element() failed", "e", document.getDocumentElement().getLocalName());

        XQItem xqi = null;
        try {
        	xqi = xqc.createItemFromNode(document, null);
        } catch (XQException e) {
        	Assert.fail("A-XQDF-1.5: createItemFromNode() failed with message: " + e.getMessage());
        }
        String result = xqi.getItemAsString(null);
        Assert.assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + "'", result.indexOf("<e>Hello world!</e>") != -1);
	}
	
	@Test
	public void testInstanceOf() throws XQException {
		
	    XQItemType xqstringtype = xqc.createAtomicType(XQItemType.XQBASETYPE_STRING);
	    XQItemType xqinttype = xqc.createAtomicType(XQItemType.XQBASETYPE_INT);
		
	    XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("'Hello world!'");
	    xqs.next();
	    try {
	    	Assert.assertEquals("A-XQIA-7.3: instanceOf failed", true, xqs.instanceOf(xqstringtype));
	    } catch (XQException e) {
	    	Assert.fail("A-XQIA-7.3: instanceOf() failed with message: " + e.getMessage());
	    }
	    try {
	    	Assert.assertEquals("A-XQIA-7.4: instanceOf failed", false, xqs.instanceOf(xqinttype));
	    } catch (XQException e) {
	    	Assert.fail("A-XQIA-7.4: instanceOf() failed with message: " + e.getMessage());
	    }
	    xqe.close();
	}
	
	@Test
	public void testItemType() throws XQException {
	    XQItemType xqtype = xqc.createDocumentType() ;
	    Assert.assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
	    Assert.assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
	    //xqtype.getPIName();
	}
	
	@Test
	public void testGetString() throws XQException {
		XQExpression xqe = xqc.createExpression();
		XQSequence xqs = xqe.executeQuery("<e a='Hello world!'/>/@*");
	    xqs.next();
	    try {
	      xqs.getItemAsString(new Properties());
	      junit.framework.Assert.fail("A-XQIA-8.1: serialization process fails when sequence contains a top-level attribute");
	    } catch (XQException xq) {
	      // Expect an XQException
	    }
	    xqe.close();
	}
	
	@Test
	public void testBindNode() throws XQException, IOException, SAXException, ParserConfigurationException {
		XQPreparedExpression xqpe;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(new InputSource(new StringReader(
				"<e>Hello world!</e>")));

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		xqpe.close();
		try {
			xqpe.bindNode(new QName("v"), document, null);
			fail("A-XQDC-1.1: bindNode() throws an XQException when the dynamic context is in closed state.");
		} catch (XQException e) {
			// Expect an XQException
		}

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindNode(null, document, null);
			fail("A-XQDC-1.2: null argument is invalid and throws an XQException.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		boolean bindFailed = false;
		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindNode(new QName("v"), document, xqc.createCommentType());
		} catch (XQException e) {
			bindFailed = true;
			// Expect an XQException
		}
		if (!bindFailed) {
			XQSequence xqs = xqpe.executeQuery();
			xqs.next();
			if (xqs.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT) {
				fail("A-XQDC-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");
			}
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindNode(new QName("foo"), document, null);
			fail("A-XQDC-1.5: The bound variable must be declared external in the prepared expression.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v as xs:decimal external; $v");
		try {
			xqpe.bindNode(new QName("v"), document, null);
			xqpe.executeQuery().getSequenceAsString(null);
			fail("A-XQDC-1.6: The dynamic type of the bound value is not compatible with the static type of the variable and must fail.");
		} catch (XQException e) {
			// Expect an XQException
		}
		xqpe.close();

		xqpe = xqc.prepareExpression("declare variable $v external; $v");
		try {
			xqpe.bindNode(new QName("v"), document, null);
		} catch (XQException e) {
			fail("A-XQDC-1.7: bindNode() failed with message: "	+ e.getMessage());
		}
		String result = xqpe.executeQuery().getSequenceAsString(null);
		assertTrue("A-XQDC-1.7: Expects serialized result contains '<e>Hello world!</e>', but it is '"
					+ result + "'",	result.indexOf("<e>Hello world!</e>") != -1);
		xqpe.close();
	}
	
	@Test
	public void testBindDate() throws XQException {
		XQExpression xqe;
		XQExpression xqe_temp;
		XQSequence xqs;
		XQSequence xqs_temp;

		String msg = "A-XQDC-4.1: bindObject implements casting rules of '14.2 Mapping a Java Data Type to an XQuery Data Type'";

		xqe = xqc.createExpression();
		xqe_temp = xqc.createExpression();
		//xqs_temp = xqe_temp.executeQuery("xs:dayTimeDuration('PT5H')");
		xqs_temp = xqe_temp.executeQuery("xs:dayTimeDuration('PT5H'), "
										+ "xs:yearMonthDuration('P1M'), " 
										+ "xs:date('2000-12-31'),"
										+ "xs:dateTime('2000-12-31T12:00:00')," 
										+ "xs:gDay('---11'),"
										+ "xs:gMonth('--11')," 
										+ "xs:gMonthDay('--01-01'),"
										+ "xs:gYear('2000')," 
										+ "xs:gYearMonth('2000-01'),"
										+ "xs:time('12:12:12')");

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:dayTimeDuration");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:yearMonthDuration");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:date");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:dateTime");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:gDay");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:gMonth");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:gMonthDay");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:gYear");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:gYearMonth");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqs_temp.next();
		xqe.bindObject(new QName("v"), xqs_temp.getObject(), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:time");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe_temp.close();
		xqe.close();
	}

	@Test
	public void testGetObject() throws XQException {
		XQExpression xqe;
		XQSequence xqs;
		    
		xqe = xqc.createExpression();
		xqs = xqe.executeQuery("xs:anyURI('http://www.foo.org')," +
		                       "xs:base64Binary('AAAA')," +
		                       "xs:boolean('true')," +
		                       "xs:byte('1')," +
		                       "xs:date('2000-12-31')," +
		                       "xs:dateTime('2000-12-31T12:00:00')," +
		                       "xs:decimal('1')," +
		                       "xs:double('1')," +
//		                       "xs:duration()" +
		                       "xs:ENTITY('AAA')," +
		                       "xs:float('1')," +
		                       "xs:gDay('---11')," +
		                       "xs:gMonth('--11')," +
		                       "xs:gMonthDay('--01-01')," +
		                       "xs:gYear('2000')," +
		                       "xs:gYearMonth('2000-01')," +
		                       "xs:hexBinary('AA')," +
		                       "xs:ID('AA')," +
		                       "xs:IDREF('AA')," +
		                       "xs:int('1')," +
		                       "xs:integer('1')," +
		                       "xs:language('en-US')," +
		                       "xs:long('1')," +
		                       "xs:Name('AAA')," +
		                       "xs:NCName('AAA')," +
		                       "xs:negativeInteger('-1')," +
		                       "xs:NMTOKEN('AAA')," +
		                       "xs:nonNegativeInteger('1')," +
		                       "xs:nonPositiveInteger('-1')," +
		                       "xs:normalizedString('AAA')," +
//		                       "xs:NOTATION()," +
		                       "xs:positiveInteger('1')," +
		                       "xs:QName('AAA')," +
		                       "xs:short('1')," +
		                       "xs:string('AAA')," +
		                       "xs:time('12:12:12')," +
		                       "xs:token('AAA')," +
		                       "xs:unsignedByte('1')," +
		                       "xs:unsignedInt('1')," +
		                       "xs:unsignedLong('1')," +
		                       "xs:unsignedShort('1')," +
		                       "xs:dayTimeDuration('PT5H')," +
		                       "xs:untypedAtomic('AAA')," +
		                       "xs:yearMonthDuration('P1M')," +
		                       "<e a=\"{''}\"/>/@a," +
		                       "<!-- comment -->, " +
		                       "document{<e/>}," +
		                       "<e/>, " +
		                       "processing-instruction {'a'} {'b'}," +
		                       "<e>text</e>/text()");

		try {
		    String msg = "A-XQIA-4.1: getObject implements casting rules of '14.4 Mapping an XQuery Atomic Value to a Java Object Type' ";
		    xqs.next();
		    assertTrue(msg + "for xs:anyURI", xqs.getObject() instanceof String);
		    xqs.next();
		    assertTrue(msg + "for xs:base64Binary", xqs.getObject() instanceof byte[]);
		    xqs.next();
		    assertTrue(msg + "for xs:boolean", xqs.getObject() instanceof Boolean);
		    xqs.next();
		    assertTrue(msg + "for xs:byte", xqs.getObject() instanceof Byte);
		    xqs.next();
		    assertTrue(msg + "for xs:date", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:dateTime", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:decimal", xqs.getObject() instanceof BigDecimal);
		    xqs.next();
		    assertTrue(msg + "for xs:double", xqs.getObject() instanceof Double);
		    xqs.next();
		    assertTrue(msg + "for xs:ENTITY", xqs.getObject() instanceof String);
		    xqs.next();
		    assertTrue(msg + "for xs:float", xqs.getObject() instanceof Float);
		    xqs.next();
		    assertTrue(msg + "for xs:gDay", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:gMonth", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:MonthDay", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:Year", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:YearMonth", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:hexBinary", xqs.getObject() instanceof byte[]); 
		    xqs.next();
		    assertTrue(msg + "for xs:ID", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:IDREF", xqs.getObject() instanceof String);
		    xqs.next();
		    assertTrue(msg + "for xs:int", xqs.getObject() instanceof Integer);  
		    xqs.next();
		    assertTrue(msg + "for xs:integer", xqs.getObject() instanceof BigInteger); 
		    xqs.next();
		    assertTrue(msg + "for xs:language", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:long", xqs.getObject() instanceof Long); 
		    xqs.next();
		    assertTrue(msg + "for xs:Name", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:NCName", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:negativeInteger", xqs.getObject() instanceof BigInteger); 
		    xqs.next();
		    assertTrue(msg + "for xs:NMTOKEN", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:nonNegativeInteger", xqs.getObject() instanceof BigInteger); 
		    xqs.next();
		    assertTrue(msg + "for xs:nonPostiveInteger", xqs.getObject() instanceof BigInteger); 
		    xqs.next();
		    assertTrue(msg + "for xs:normalizedString", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:postiveInteger", xqs.getObject() instanceof BigInteger); 
		    xqs.next();
		    assertTrue(msg + "for xs:QName", xqs.getObject() instanceof QName); 
		    xqs.next();
		    assertTrue(msg + "for xs:short", xqs.getObject() instanceof Short);  
		    xqs.next();
		    assertTrue(msg + "for xs:string", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:time", xqs.getObject() instanceof XMLGregorianCalendar);
		    xqs.next();
		    assertTrue(msg + "for xs:token", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:unsignedByte", xqs.getObject() instanceof Short);
		    xqs.next();
		    assertTrue(msg + "for xs:unsignedInt", xqs.getObject() instanceof Long); 
		    xqs.next();
		    assertTrue(msg + "for xs:unsignedLong", xqs.getObject() instanceof BigInteger);    
		    xqs.next();
		    assertTrue(msg + "for xs:unsignedShort", xqs.getObject() instanceof Integer); 
		    xqs.next();
		    assertTrue(msg + "for xs:dayTimeDuration", xqs.getObject() instanceof Duration);
		    xqs.next();
		    assertTrue(msg + "for xs:untypedAtomic", xqs.getObject() instanceof String); 
		    xqs.next();
		    assertTrue(msg + "for xs:yearMonthDuration", xqs.getObject() instanceof Duration);
		    xqs.next();
		    assertTrue(msg + "for attribute", xqs.getObject() instanceof Attr);
		    xqs.next();
		    assertTrue(msg + "for comment", xqs.getObject() instanceof Comment);
		    xqs.next();
		    assertTrue(msg + "for document", xqs.getObject() instanceof Document);
		    xqs.next();
		    assertTrue(msg + "for element", xqs.getObject() instanceof Element);
		    xqs.next();
		    assertTrue(msg + "for processing instruction", xqs.getObject() instanceof ProcessingInstruction);
		    xqs.next();
		    assertTrue(msg + "for text", xqs.getObject() instanceof Text);
		} catch (XQException e) {
		    e.printStackTrace();
		    fail("A-XQIA-4.1: getObject on element() failed with message: " + e.getMessage());
		}
	}
		  
	@Test
	public void testGetNodeUri() throws XQException {
	    XQExpression xqe;
	    XQSequence xqs;

	    xqe = xqc.createExpression();
	    xqs = xqe.executeQuery("'1'");
	    xqs.next();
	    try {
	      xqs.getNodeUri();
	      Assert.fail("A-XQIA-5.1: getNodeUri() should fail if current item is not a node");
	    } catch (XQException e) {
	      // Expect an XQException
	    }
	    xqe.close();

	    xqe = xqc.createExpression();
	    xqs = xqe.executeQuery("<e/>");
	    try {
	      xqs.getNodeUri();
	      Assert.fail("A-XQIA-5.2: getNodeUri() should fail when not positioned on an item");
	    } catch (XQException e) {
	      // Expect an XQException
	    }
	    xqe.close();

	    xqe = xqc.createExpression();
	    xqs = xqe.executeQuery("<e/>");
	    xqs.next();
	    xqs.close();
	    try {
	      xqs.getNodeUri();
	      Assert.fail("A-XQIA-5.3: closed item accessor supports getNodeUri()");
	    } catch (XQException e) {
	      // Expect an XQException
	    }
	    xqe.close();
		    
	    xqe = xqc.createExpression();
	    xqs = xqe.executeQuery("<e/>");
	    xqs.next();
	    try {
	      xqs.getNodeUri(); // returned value is implementation defined
	    } catch (XQException e) {
	      Assert.fail("A-XQIA-5.4: getNodeUri() failed with message: " + e.getMessage());
	    }
	    xqe.close();
		    
	    xqe = xqc.createExpression();
	    xqs = xqe.executeQuery("<e/>");
	    xqs.next();
	    XQItem item = xqs.getItem();
	    try {
	      xqs.getNodeUri(); // returned value is implementation defined
	    } catch (XQException e) {
	      Assert.fail("A-XQIA-5.5: getNodeUri() failed with message: " + e.getMessage());
	    }
	    xqe.close();
	}
	
}
