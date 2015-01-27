package com.bagri.xquery;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import junit.framework.Assert;
//import net.sf.saxon.xqj.SaxonXQDataSource;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.bagri.xquery.saxon.XQProcessorServer;

@Ignore
public class SaxonParserTest {
	
	private XQProcessorServer parser;

	@Before
	public void setUp() throws Exception {
		parser = new XQProcessorServer(null);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseXQuery() throws XQException {
		String query = "for $i in 1 to 10 return ($i * $i)";
		parser.parseXQuery(query);
	}

	@Test
    public void testSaxon() throws XQException {
        //SaxonXQDataSource xqds = new SaxonXQDataSource();
		XQDataSource xqds = null;
        XQConnection xqc = xqds.getConnection();
		XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("<e>Hello world!</e>");
	    //XQSequence xqs = xqe.executeQuery("<a b='c'>{5+2}</a>");
        while (xqs.next()) {
            System.out.println(xqs.getItemAsString(null));
        }
        //System.out.println(xqds.getSchemaValidationMode());
    }
	
	@Test
	public void testGetInt() throws XQException {

        //SaxonXQDataSource xqds = new SaxonXQDataSource();
		XQDataSource xqds = null;
        XQConnection xqc = xqds.getConnection();
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
	public void testGetBoolean() throws XQException {

        //SaxonXQDataSource xqds = new SaxonXQDataSource();
		XQDataSource xqds = null;
        XQConnection xqc = xqds.getConnection();
		XQExpression xqe = xqc.createExpression();
	    xqe.bindObject(new QName("v"), Boolean.valueOf(true), null);
	    XQSequence xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:boolean");
	    xqs.next();
	    assertTrue("expected true but got false", xqs.getBoolean());
	    
	    xqe.bindObject(new QName("v"), new Byte((byte) 1), null);
	    xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:byte");
	    xqs.next();
	    assertTrue("expected true (byte) but got false", xqs.getBoolean());
	    
	    xqs.close();
	}
	
	@Test
	public void testGetByte() throws XQException {
		
        //SaxonXQDataSource xqds = new SaxonXQDataSource();
		XQDataSource xqds = null;
        XQConnection xqc = xqds.getConnection();
		XQExpression xqe = xqc.createExpression();
		XQSequence xqs = xqe.executeQuery("xs:byte('1')");
	    xqs.next();
	    Object o = xqs.getObject();
        Assert.assertTrue(o instanceof Byte);
	    xqe.close();
	}
	
	@Test
	public void testBinding() throws XQException {
        //SaxonXQDataSource xqds = new SaxonXQDataSource();
		XQDataSource xqds = null;
        XQConnection xqc = xqds.getConnection();
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
	public void testBindNode() throws XQException, IOException, SAXException, ParserConfigurationException {
		XQPreparedExpression xqpe;
        //SaxonXQDataSource xqds = new SaxonXQDataSource();
		XQDataSource xqds = null;
        XQConnection xqc = xqds.getConnection();

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
	
}
