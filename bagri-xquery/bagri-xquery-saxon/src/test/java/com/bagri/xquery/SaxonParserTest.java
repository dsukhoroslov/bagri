package com.bagri.xquery;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;

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

//import net.sf.saxon.xqj.SaxonXQDataSource;


import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.bagri.xquery.saxon.XQProcessorServer;
import com.saxonica.xqj.SaxonXQDataSource;

//@Ignore
public class SaxonParserTest {
	
	//private XQProcessorServer parser;

	@Before
	public void setUp() throws Exception {
		//parser = new XQProcessorServer(null);
	}

	@After
	public void tearDown() throws Exception {
	}

	//@Test
	//public void testParseXQuery() throws XQException {
	//	String query = "for $i in 1 to 10 return ($i * $i)";
	//	parser.parseXQuery(query);
	//}

	@Test
    public void testSaxon() throws XQException {
        SaxonXQDataSource xqds = new SaxonXQDataSource();
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

        SaxonXQDataSource xqds = new SaxonXQDataSource();
        XQConnection xqc = xqds.getConnection();
	    XQExpression xqe = xqc.createExpression();
	    XQSequence xqs = xqe.executeQuery("xs:int('1'), 10.0");
	    xqs.next();
	    try {
	    	assertEquals("A-XQIA-1.6: getInt on xs:int failed", 1, xqs.getInt());
	    } catch (XQException e) {
	    	fail("A-XQIA-1.6: getInt on xs:int failed with message: " + e.getMessage());
	    }
	    xqs.next();
	    try {
	    	assertEquals("A-XQIA-1.6: getInt on xs:decimal failed", 10, xqs.getInt());
	    } catch (XQException e) {
	    	fail("A-XQIA-1.6: getInt on xs:decimal failed with message: " + e.getMessage());
	    }
	    xqe.close();
	}
	
	@Test
	public void testGetBoolean() throws XQException {

        SaxonXQDataSource xqds = new SaxonXQDataSource();
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
		
        SaxonXQDataSource xqds = new SaxonXQDataSource();
        XQConnection xqc = xqds.getConnection();
		XQExpression xqe = xqc.createExpression();
		XQSequence xqs = xqe.executeQuery("xs:byte('1')");
	    xqs.next();
	    Object o = xqs.getObject();
        assertTrue(o instanceof Byte);
	    xqe.close();
	}
	
	@Test
	public void testBinding() throws XQException {
        SaxonXQDataSource xqds = new SaxonXQDataSource();
        XQConnection xqc = xqds.getConnection();
	    XQPreparedExpression xqpe = xqc.prepareExpression("declare variable $v external; $v");
	    xqpe.bindString(new QName("v"), "Hello world!", xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQSequence xqs = xqpe.executeQuery();
	    assertTrue(xqs.next());
	    assertEquals("Hello world!", xqs.getAtomicValue());
	    xqpe.close();
	    
	    xqpe = xqc.prepareExpression("declare variable $v external; $v");
	    xqpe.bindString(new QName("v"), "Hello", xqc.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
	    xqs = xqpe.executeQuery();
	    xqs.next();
	    assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQITEMKIND_ATOMIC, xqs.getItemType().getItemKind());
	    assertEquals("A-XQDC-1.7: Successful bindXXX.", XQItemType.XQBASETYPE_NCNAME, xqs.getItemType().getBaseType());
	    assertEquals("A-XQDC-1.7: Successful bindXXX.", "Hello", xqs.getObject());
	    xqpe.close(); 
	}
	
	@Test
	public void testBindNode() throws XQException, IOException, SAXException, ParserConfigurationException {
		XQPreparedExpression xqpe;
        SaxonXQDataSource xqds = new SaxonXQDataSource();
        XQConnection xqc = xqds.getConnection();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(new InputSource(new StringReader("<e>Hello world!</e>")));

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
	public void testBindObject_AllTypes() throws Exception {
		XQExpression xqe;
		XQSequence xqs;

		String msg = "A-XQDC-4.1: bindObject implements casting rules of '14.2 Mapping a Java Data Type to an XQuery Data Type'";

        SaxonXQDataSource xqds = new SaxonXQDataSource();
        XQConnection xqc = xqds.getConnection();
		
		xqe = xqc.createExpression();

		xqe.bindObject(new QName("v"), Boolean.valueOf(true), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:boolean");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new Byte((byte) 1), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:byte");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new Float(1), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:float");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new Double(1), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:double");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new Integer(1), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:int");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new Long(1), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:long");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new Short((short) 1), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:short");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), "Hello world!", null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:string");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new BigDecimal("1"), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:decimal");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), new BigInteger("1"), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:integer");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		XQExpression xqe_temp;
		XQSequence xqs_temp;

		xqe_temp = xqc.createExpression();
		xqs_temp = xqe_temp.executeQuery("xs:dayTimeDuration('PT5H'), "
				+ "xs:yearMonthDuration('P1M'), " + "xs:date('2000-12-31'),"
				+ "xs:dateTime('2000-12-31T12:00:00')," + "xs:gDay('---11'),"
				+ "xs:gMonth('--11')," + "xs:gMonthDay('--01-01'),"
				+ "xs:gYear('2000')," + "xs:gYearMonth('2000-01'),"
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

		xqe.bindObject(new QName("v"), new QName("abc"), null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of xs:QName");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element element = document.createElement("e");
		document.appendChild(element);
		DocumentFragment documentFragment = document.createDocumentFragment();
		Attr attribute = document.createAttribute("a");
		Comment comment = document.createComment("comment");
		ProcessingInstruction pi = document.createProcessingInstruction("target", "data");
		Text text = document.createTextNode("text");

		xqe.bindObject(new QName("v"), document, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of document-node()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), documentFragment, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of document-node()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), element, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of element()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), attribute, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of attribute()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), comment, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of comment()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), pi, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of processing-instruction()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.bindObject(new QName("v"), text, null);
		xqs = xqe.executeQuery("declare variable $v external; $v instance of text()");
		xqs.next();
		assertTrue(msg, xqs.getBoolean());

		xqe.close();
	}

	
	@Test
	public void testWriteSequence_Writer() throws XQException {
		XQExpression xqe;
		XQSequence xqs;

		Properties prop = new Properties();
		prop.setProperty("encoding", "UTF-8");

        SaxonXQDataSource xqds = new SaxonXQDataSource();
        XQConnection xqc = xqds.getConnection();
		
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
