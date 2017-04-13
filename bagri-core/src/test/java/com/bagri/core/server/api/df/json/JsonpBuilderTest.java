package com.bagri.core.server.api.df.json;

import static com.bagri.core.Constants.pn_log_level;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.json.stream.JsonGenerator;
import javax.xml.xquery.XQItemType;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class JsonpBuilderTest {

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
		//System.setProperty(pn_log_level, "trace");
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
	public void testBuildManual() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		// to prepare model:
		//XmlStaxParser parser = new XmlStaxParser(model);
		//List<Data> data = parser.parse(json);
		//data.clear();
		List<Data> data = new ArrayList<>();
		data.add(new Data(new Path("", 0, NodeKind.document, 1, 0, 25, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {}, null)));
		data.add(new Data(new Path("/firstName", 0, NodeKind.attribute, 2, 1, 2, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {1}, "John")));
		data.add(new Data(new Path("/lastName", 0, NodeKind.attribute, 3, 1, 3, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {2}, "Smith")));
		data.add(new Data(new Path("/age", 0, NodeKind.attribute, 4, 1, 4, XQItemType.XQBASETYPE_LONG, Occurrence.onlyOne), new Element(new int[] {3}, new Long(25))));
		data.add(new Data(new Path("/address", 0, NodeKind.element, 5, 1, 9, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {4}, null)));
		data.add(new Data(new Path("/address/streetAddress", 0, NodeKind.attribute, 6, 5, 6, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 1}, "21 2nd Street")));
		data.add(new Data(new Path("/address/city", 0, NodeKind.attribute, 7, 5, 7, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 2}, "New York")));
		data.add(new Data(new Path("/address/state", 0, NodeKind.attribute, 8, 5, 8, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 3}, "NY")));
		data.add(new Data(new Path("/address/postalCode", 0, NodeKind.attribute, 9, 5, 9, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 4}, "10021")));
		data.add(new Data(new Path("/phoneNumber", 0, NodeKind.array, 10, 1, 20, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne), new Element(new int[] {5}, null)));
		data.add(new Data(new Path("/phoneNumber/", 0, NodeKind.element, 11, 10, 13, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.oneOrMany), new Element(new int[] {5, 1}, null)));
		data.add(new Data(new Path("/phoneNumber/type", 0, NodeKind.attribute, 12, 11, 12, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 1, 1}, "home")));
		data.add(new Data(new Path("/phoneNumber/number", 0, NodeKind.attribute, 13, 11, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 1, 2}, "212 555-1234")));
		data.add(new Data(new Path("/phoneNumber/", 0, NodeKind.element, 11, 10, 13, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.oneOrMany), new Element(new int[] {5, 2}, null)));
		data.add(new Data(new Path("/phoneNumber/type", 0, NodeKind.attribute, 12, 11, 12, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 1}, "fax")));
		data.add(new Data(new Path("/phoneNumber/number", 0, NodeKind.attribute, 13, 11, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 2}, "646 555-4567")));
		data.add(new Data(new Path("/gender", 0, NodeKind.element, 14, 1, 15, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {6}, null)));
		data.add(new Data(new Path("/gender/type", 0, NodeKind.attribute, 15, 14, 15, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {6, 1}, "male")));
		//System.out.println(data);
		JsonpBuilder builder = new JsonpBuilder(model);
		Properties props = new Properties();
		props.setProperty(JsonGenerator.PRETTY_PRINTING, "true");
		builder.init(props);
		String content = builder.buildString(data);
		System.out.println(content);
		assertNotNull(content);
		// now compare content vs xml..
		//assertEquals(xml, content);
	}

	@Test
	public void testBuild() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		JsonpParser parser = new JsonpParser(model);
		List<Data> data = parser.parse(json);
		System.out.println(data);
		assertNotNull(data);
		assertEquals(18, data.size()); 
		JsonpBuilder builder = new JsonpBuilder(model);
		String content = builder.buildString(data);
		System.out.println("content: " + content);
		assertNotNull(content);
	}
		
}

	
/*

	{
.1	    "firstName": "John",
.2	    "lastName": "Smith",
.3	    "age": "25",
.4	    "address": {
.4.1	        "streetAddress": "21 2nd Street",
.4.2	        "city": "New York",
.4.3	        "state": "NY",
.4.4	        "postalCode": "10021"
    	},
.5	    "phoneNumbers": {
.5.1	        "phoneNumber": [{
.5.1.1	            "type": "home",
.5.1.2	            "number": "212 555-1234"
        		},
.5.2       		{
.5.2.1         		"type": "fax",
.5.2.2         		"number": "646 555-4567"
        }]
    },
.6	    "gender": {
.6.1	    "type": "male"
    	}
	}
	
*/
	
