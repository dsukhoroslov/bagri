package com.bagri.xdm.schema;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.xdm.client.xml.XDMSchemaProcessor;
import com.bagri.xdm.domain.XDMPath;

public class XDMSchemaProcessorTest {

	//@BeforeClass
	//public static void setUpBeforeClass() throws Exception {
	//}

	//@AfterClass
	//public static void tearDownAfterClass() throws Exception {
	//}

	//@Before
	//public void setUp() throws Exception {
	//}

	//@After
	//public void tearDown() throws Exception {
	//}


	@Test
	public void testParse() throws IOException {
		String sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		String fileName = sampleRoot + "security.xsd";
		XDMSchemaProcessor proc = new XDMSchemaProcessor();
		List<XDMPath> xpl = proc.parse(fileName);
		assertNotNull(xpl);
	}

}
