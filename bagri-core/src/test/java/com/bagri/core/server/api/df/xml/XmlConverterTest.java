package com.bagri.core.server.api.df.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.df.xml.XmlHandler;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class XmlConverterTest {

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
		//System.setProperty(pn_log_level, "trace");
	}

	@Test
	public void testConvertMap() throws Exception {
		ModelManagement dict = new ModelManagementImpl();
		XmlHandler handler = new XmlHandler(dict);
		ContentConverter<String, Map> cc = (ContentConverter<String, Map>) handler.getConverter(Map.class);
		Map<String, Object> map = new HashMap<>(); 
		map.put("firstName", "John");
		map.put("lastName", "Smith");
		map.put("age", 25);
		Map<String, Object> address = new HashMap<>();
		address.put("streetAddress", "21 2nd Street");
		address.put("city", "New York");
		address.put("state", "NY");
		address.put("postalCode", "10021");
		map.put("address", address);
		List<Map> numbers = new ArrayList<Map>();
		Map<String, Object> phone = new HashMap<>();
		phone.put("type", "home");
		phone.put("number", "212 555-1234");
		numbers.add(phone);
		phone = new HashMap<>();
		phone.put("type", "fax");
		phone.put("number", "646 555-4567");
		numbers.add(phone);
		map.put("phoneNumbers", numbers);
		Map<String, Object> gender = new HashMap<>();
		gender.put("type", "male");
		map.put("gender", gender);
		String result = cc.convertFrom(map);
		System.out.println(result);
		assertNotNull(result);
	}

	@Test
	public void testConvertBean() throws Exception {
		//
	}

}
