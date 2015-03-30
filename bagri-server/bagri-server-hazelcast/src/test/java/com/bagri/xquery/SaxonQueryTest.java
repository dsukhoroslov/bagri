package com.bagri.xquery;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.bagri.xdm.client.common.impl.XDMRepositoryBase;
import com.bagri.xdm.cache.hazelcast.impl.QueryManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorServer;

public class SaxonQueryTest {

	private XQProcessorServer processor;

	@Before
	public void setUp() throws Exception {
		QueryManagementImpl qMgr = new QueryManagementImpl();
		RepositoryImpl xRepo = new RepositoryImpl();
		xRepo.setQueryManagement(qMgr);
		processor = new XQProcessorServer(xRepo);
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(processor);
		processor.setXQDataFactory(xqFactory);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseXQuery() throws XQException {
		String query = "<e>Hello world!</e>";
		Properties props = new Properties();
		Iterator itr = processor.executeXQuery(query, props);
		Object o = itr.next();
		Assert.assertNotNull(o);
		System.out.println(processor.convertToString(o));
	}

	@Test
	public void testBindNode() throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(new InputSource(new StringReader(
				"<e>Hello world!</e>")));
		
		String query = "declare variable $v external; $v";
		XQItem docItem = processor.getXQDataFactory().createItemFromNode(document, 
				processor.getXQDataFactory().createDocumentType()); //NodeType());
		processor.bindVariable(new QName("v"), docItem);
		Properties props = new Properties();
		Iterator itr = processor.executeXQuery(query, props);

		//String result = xqpe.executeQuery().getSequenceAsString(null);
		//assertTrue("A-XQDC-1.7: Expects serialized result contains '<e>Hello world!</e>', but it is '"
		//			+ result + "'",	result.indexOf("<e>Hello world!</e>") != -1);
		
		Object o = itr.next();
		Assert.assertNotNull(o);
		System.out.println(processor.convertToString(o));
	}
}
