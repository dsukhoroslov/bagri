package com.bagri.core.server.api.df.xml;

import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_schema_builder_pretty;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

import javax.json.stream.JsonGenerator;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.df.json.JsonpBuilder;
import com.bagri.core.server.api.df.json.JsonpParser;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class XmlStaxParserTest {
	
	private static String xml = 
			"<person>\n" +
	        "    <firstName>John</firstName>\n" +
	        "    <lastName>Smith</lastName>\n" +
	        "    <age>25</age>\n" +
	        "    <address>\n" +
	        "        <streetAddress>21 2nd Street</streetAddress>\n" +
	        "        <city>New York</city>\n" +
	        "        <state>NY</state>\n" +
	        "        <postalCode>10021</postalCode>\n" +
	        "    </address>\n" +
	        "    <phoneNumbers>\n" +
	        "        <phoneNumber>\n" +
	        "            <type>home</type>\n" +
	        "            <number>212 555-1234</number>\n" +
	        "        </phoneNumber>\n" +
	        "        <phoneNumber>\n" +
	        "            <type>fax</type>\n" +
	        "            <number>646 555-4567</number>\n" +
	        "        </phoneNumber>\n" +
	        "    </phoneNumbers>\n" +
	        "    <gender>\n" +
	        "        <type>male</type>\n" +
	        "    </gender>\n" +
	        "</person>";


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
		//System.setProperty(pn_log_level, "debug");
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
		XmlStaxParser parser = new XmlStaxParser(dict);
		List<Data> elts = parser.parse(xml);
		//System.out.println(elts);
		assertNotNull(elts);
		assertEquals(31, elts.size()); 
		Data data = elts.get(0);
		assertEquals("/", data.getPath());
		assertNull(data.getValue());
		data = elts.get(1);
		assertEquals("/person", data.getPath());
		assertNull(data.getValue());
		data = elts.get(2);
		assertEquals("/person/firstName", data.getPath());
		data = elts.get(3);
		assertEquals("/person/firstName/text()", data.getPath());
		assertEquals("John", data.getValue());
		//int typeId = 1;
		//String root = dict.getDocumentRoot(typeId);
		//assertEquals("", root); -> /firstName	
	}

	//@Test
	//public void testConversion() throws Exception {
	//	ModelManagement model = new ModelManagementImpl();
	//	XmlStaxParser parser = new XmlStaxParser(model);
	//	List<Data> data = parser.parse(xml);
	//	System.out.println(data);
	//	JsonpBuilder builder = new JsonpBuilder(model);
	//	Properties props = new Properties();
	//	props.setProperty(JsonGenerator.PRETTY_PRINTING, "true");
	//	builder.init(props);
	//	String content = builder.buildContent(data);
	//	System.out.println(content);
	//	assertNotNull(content);
	//	JsonpParser jParser = new JsonpParser(model);
	//	List<Data> data2 = jParser.parse(content);
		//
	//	assertNotNull(data2);
	//}
	
	

}

