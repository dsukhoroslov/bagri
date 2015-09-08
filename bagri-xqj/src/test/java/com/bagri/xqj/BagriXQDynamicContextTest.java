package com.bagri.xqj;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQSequence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class BagriXQDynamicContextTest {
	
	private XQConnection xqc;

	@Before
	public void setUp() throws Exception {
	    XQDataSource xqds = new BagriXQDataSource();
	    xqds.setProperty(BagriXQDataSource.ADDRESS, "localhost:10500");
	    xqds.setProperty(BagriXQDataSource.SCHEMA, "default");
	    xqds.setProperty(BagriXQDataSource.USER, "guest");
	    xqds.setProperty(BagriXQDataSource.PASSWORD, "password");
	    xqds.setProperty(BagriXQDataSource.XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
	    xqds.setProperty(BagriXQDataSource.XDM_REPOSITORY, "com.bagri.xdm.client.hazelcast.impl.RepositoryImpl");
		xqc = xqds.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		if (xqc != null)
			xqc.close();	
	}

	@Test
	public void testBindObject_AllTypes() throws Exception {
		XQExpression xqe;
		XQSequence xqs;

		String msg = "A-XQDC-4.1: bindObject implements casting rules of '14.2 Mapping a Java Data Type to an XQuery Data Type'";

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

}
