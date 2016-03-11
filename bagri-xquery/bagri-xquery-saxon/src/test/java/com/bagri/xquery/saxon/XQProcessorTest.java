package com.bagri.xquery.saxon;

import static com.bagri.common.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import javax.xml.xquery.XQException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.bagri.common.util.XMLUtils;

public class XQProcessorTest {

	private XQProcessorServer parser;

	@Before
	public void setUp() throws Exception {
		parser = new XQProcessorServer();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvertToDocument() throws XQException {
		
		String xml = "<e>Hello World!</e>";
		Document doc = parser.convertToDocument(xml);
		assertNotNull(doc);
		assertEquals("Hello World!", doc.getDocumentElement().getTextContent());
	}
	
	@Test
	public void testConvertionSpeed() throws Exception {
		
		//String xml = "<e>Hello World!</e>";
		String fileName = "..\\..\\etc\\samples\\tpox\\security1500.xml";
		String xml = readTextFile(fileName);

		int count = 1000;
		long stamp = System.currentTimeMillis();
		for (int i=0; i < count; i++) {
			Document doc = parser.convertToDocument(xml);
			assertNotNull(doc);
		}
		stamp = System.currentTimeMillis() - stamp;
		System.out.println("Saxon conversion time: " + stamp);

		stamp = System.currentTimeMillis();
		for (int i=0; i < count; i++) {
			Document doc = XMLUtils.textToDocument(xml);
			assertNotNull(doc);
		}
		stamp = System.currentTimeMillis() - stamp;
		System.out.println("Xerces conversion time: " + stamp);
	}
}
