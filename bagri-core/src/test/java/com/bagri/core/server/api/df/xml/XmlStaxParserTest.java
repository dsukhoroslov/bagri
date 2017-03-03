package com.bagri.core.server.api.df.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.df.json.JsonApiParser;
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
		System.setProperty("logback.configurationFile", "test-logging.xml");
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
		System.out.println(elts);
		assertNotNull(elts);
		assertEquals(31, elts.size()); 
		Data data = elts.get(0);
		assertEquals("", data.getPath());
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
	
}

/*

	<person>
.1	    <firstName>John</firstName>
.2	    <lastName>Smith</lastName>
.3	    <age>25</age>
.4	    <address>
.4.1	    <streetAddress>21 2nd Street</streetAddress>
.4.2	    <city>New York</city>
.4.3	    <state>NY</state>
.4.4	    <postalCode>10021</postalCode>
    	</address>
.5	    <phoneNumbers>
.5.1	    <phoneNumber>
.5.1.1	        <type>home</type>
.5.1.2	        <number>212 555-1234</number>
        	</phoneNumber>
.5.2	    <phoneNumber>
.5.2.1	        <type>fax</type>
.5.2.2	        <number>646 555-4567</number>
        	</phoneNumber>
    	</phoneNumbers>
.6		<gender>
.6.1	   <type>male</type>
    	</gender>
	</person>

*/