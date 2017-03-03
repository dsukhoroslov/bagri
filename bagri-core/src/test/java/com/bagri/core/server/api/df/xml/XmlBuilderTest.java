package com.bagri.core.server.api.df.xml;

import static com.bagri.core.server.api.impl.ContentBuilderBase.dataToElements;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.DataKey;
import com.bagri.core.model.Data;
import com.bagri.core.model.Elements;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class XmlBuilderTest {

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
	public void testBuild() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		//System.out.println(data);
		assertNotNull(data);
		assertEquals(31, data.size());
		XmlBuilder builder = new XmlBuilder(model);
		Map<DataKey, Elements> elements = dataToElements(data);
		System.out.println(elements);
		String content = builder.buildString(elements);
		System.out.println(content);
		// now compare content vs xml..
		assertNotNull(content);
	}
	
}

/*

p1;		<person>
p2; .1	    <firstName>John</firstName>
p3; .2	    <lastName>Smith</lastName>
p4; .3	    <age>25</age>
p5; .4	    <address>
p6; .4.1	    <streetAddress>21 2nd Street</streetAddress>
p7; .4.2	    <city>New York</city>
p8; .4.3	    <state>NY</state>
p9; .4.4	    <postalCode>10021</postalCode>
    		</address>
p10; .5	    <phoneNumbers>
p11; .5.1	    <phoneNumber>
p12; .5.1.1	        <type>home</type>
p13; .5.1.2	        <number>212 555-1234</number>
        		</phoneNumber>
p11; .5.2	    <phoneNumber>
p12; .5.2.1	        <type>fax</type>
p13; .5.2.2	        <number>646 555-4567</number>
        		</phoneNumber>
    		</phoneNumbers>
p14; .6		<gender>
p15; .6.1	   <type>male</type>
    		</gender>
		</person>

[Data(p2, .1), Data(p3, .2), Data(p4, .3), Data(p6, .4.1), Data(p7, .4.2), Data(p8, .4.3), Data(p9, .4.4),
 Data(p12, .5.1.1), Data(p13, .5.1.2), Data(p12, .5.2.1), Data(p13, .5.2.2), Data(p15, .6.1)]
*/
