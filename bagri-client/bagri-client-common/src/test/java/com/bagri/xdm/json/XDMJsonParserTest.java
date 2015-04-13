package com.bagri.xdm.json;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.client.json.XDMJsonParser;
import com.bagri.xdm.domain.XDMElement;

public class XDMJsonParserTest {

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
	public void testParse() {
		XDMModelManagement dict = null; //new ModelManagementImpl();
		XDMJsonParser parser = new XDMJsonParser(dict);
		//String json = 
		//"{\n" +
		//"\t\"name\":\"mkyong\",\n" +
		//"\t\"age\":29,\n" +
		//"\t\"messages\":[\"msg 1\",\"msg 2\",\"msg 3\"]\n" +
		//"}";
		try {
			List<XDMElement> elts = parser.parse(json);
			assertNull(elts);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

/*

<person>
  <firstName>John</firstName>
  <lastName>Smith</lastName>
  <age>25</age>
  <address>
    <streetAddress>21 2nd Street</streetAddress>
    <city>New York</city>
    <state>NY</state>
    <postalCode>10021</postalCode>
  </address>
  <phoneNumbers>
    <phoneNumber>
      <type>home</type>
      <number>212 555-1234</number>
    </phoneNumber>
    <phoneNumber>
      <type>fax</type>
      <number>646 555-4567</number>
    </phoneNumber>
  </phoneNumbers>
  <gender>
    <type>male</type>
  </gender>
</person>

{
  "person": {
    "firstName": "John",
    "lastName": "Smith",
    "age": "25",
    "address": {
      "streetAddress": "21 2nd Street",
      "city": "New York",
      "state": "NY",
      "postalCode": "10021"
    },
    "phoneNumbers": {
      "phoneNumber": [
        {
          "type": "home",
          "number": "212 555-1234"
        },
        {
          "type": "fax",
          "number": "646 555-4567"
        }
      ]
    },
    "gender": { "type": "male" }
  }
}
*/