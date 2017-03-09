package com.bagri.core.server.api.df.json;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.df.json.JsonApiParser;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class JsonApiParserTest {

	private static String json = 
		"{\n" +
		"  \"firstName\": \"John\",\n" +
		"  \"lastName\": \"Smith\",\n" +
		"  \"age\": 25,\n" +
		"  \"address\": {\n" +
		"    \"streetAddress\": \"21 2nd Street\",\n" +
		"    \"city\": \"New York\",\n" +
		"    \"state\": \"NY\",\n" +
		"    \"postalCode\": \"10021\"\n" +
		"  },\n" +
		"  \"phoneNumber\": [\n" +
		"    {\n" +
		"      \"type\": \"home\",\n" +
		"      \"number\": \"212 555-1234\"\n" +
		"    },\n" +
		"    {\n" +
		"      \"type\": \"fax\",\n" +
		"      \"number\": \"646 555-4567\"\n" +
		"    }\n" +
		"  ],\n" +
		"  \"gender\": {\n" +
		"    \"type\": \"male\"\n" +
		"  }\n" +
		"}";
			
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
	}

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
	public void testParse() throws Exception {
		ModelManagement dict = new ModelManagementImpl();
		JsonApiParser parser = new JsonApiParser(dict);
		List<Data> elts = parser.parse(json);
		//System.out.println(elts);
		assertNotNull(elts);
		assertEquals(28, elts.size()); 
		Data data = elts.get(0);
		assertEquals("", data.getPath());
		assertNull(data.getValue());
		data = elts.get(1);
		assertEquals("/firstName", data.getPath());
		assertNull(data.getValue());
		data = elts.get(2);
		assertEquals("/firstName/text()", data.getPath());
		assertEquals("John", data.getValue());
		//int typeId = 1;
		//String root = dict.getDocumentRoot(typeId);
		//assertEquals("", root); -> /firstName	
	}

	@Test
	public void testParseSecurity() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		JsonApiParser parser = new JsonApiParser(model);
		File f = new File("..\\etc\\samples\\json\\security1500.json");
		List<Data> data = parser.parse(f);
		assertNotNull(data);
		assertEquals(74, data.size());
		FileReader fr = new FileReader("..\\etc\\samples\\json\\security5621.json");
		data = parser.parse(fr);
		assertNotNull(data);
		assertEquals(68, data.size()); 
		InputStream fis = new FileInputStream("..\\etc\\samples\\json\\security9012.json");
		data = parser.parse(fis);
		assertNotNull(data);
		assertEquals(76, data.size()); 
	}
	
}

