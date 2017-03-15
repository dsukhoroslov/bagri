package com.bagri.core.server.api.df.json;

import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.server.api.impl.ContentBuilderBase.dataToElements;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.DataKey;
import com.bagri.core.model.Data;
import com.bagri.core.model.Elements;
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
	public void testParse() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		JsonpParser parser = new JsonpParser(model);
		List<Data> data = parser.parse(json);
		//System.out.println(data);
		assertNotNull(data);
		assertEquals(28, data.size()); 
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
	
