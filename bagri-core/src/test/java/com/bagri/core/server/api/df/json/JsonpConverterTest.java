package com.bagri.core.server.api.df.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.server.api.ContentConverter;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class JsonpConverterTest {

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
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Map<String, Object> testPersonConversion(ContentConverter<String, Map> converter, String content) {
			Map<String, Object> map = converter.convertTo(content);
			assertNotNull(map);
			assertEquals(6, map.size()); 
			assertEquals("John", map.get("firstName"));
			assertEquals("Smith", map.get("lastName"));
			assertEquals(25, map.get("age"));
			Map address = (Map) map.get("address");
			assertEquals("21 2nd Street", address.get("streetAddress"));
			assertEquals("New York", address.get("city"));
			assertEquals("NY", address.get("state"));
			assertEquals("10021", address.get("postalCode"));
			List<Map> phones = (List<Map>) map.get("phoneNumber");
			assertEquals(2, phones.size());
			assertEquals("home", phones.get(0).get("type"));
			assertEquals("212 555-1234", phones.get(0).get("number"));
			assertEquals("fax", phones.get(1).get("type"));
			assertEquals("646 555-4567", phones.get(1).get("number"));
			Map gender = (Map) map.get("gender");
			assertEquals("male", gender.get("type"));
			return map;
		}

		@Test
		public void testConvertMap() throws Exception {
			ModelManagement dict = new ModelManagementImpl();
			JsonpHandler handler = new JsonpHandler(dict);
			ContentConverter<String, Map> cc = (ContentConverter<String, Map>) handler.getConverter(Map.class);
			Map<String, Object> map = testPersonConversion(cc, json);
			String result = cc.convertFrom(map);
			System.out.println(result);
			assertNotNull(result);
		}

		@Test
		public void testConvertBean() throws Exception {
			ModelManagement dict = new ModelManagementImpl();
			JsonpHandler handler = new JsonpHandler(dict);
			ContentConverter<String, Person> cc = (ContentConverter<String, Person>) handler.getConverter(Person.class);
			Person p = new Person();
			p.firstName = "John";
			p.lastName = "Smith";
			p.age = 25;
			p.address = new Address();
			p.address.streetAddress = "21 2nd Street";
			p.address.city = "New York";
			p.address.state = "NY";
			p.address.postalCode = "10021";
			p.phoneNumber = new ArrayList<>();
			PhoneNumber phone = new PhoneNumber();
			phone.type = PhoneNumber.PhoneType.home;
			phone.number = "212 555-1234";
			p.phoneNumber.add(phone);
			phone = new PhoneNumber();
			phone.type = PhoneNumber.PhoneType.fax;
			phone.number = "646 555-4567";
			p.phoneNumber.add(phone);
			p.gender = new Gender();
			p.gender.type = Gender.GenderType.male;
			String result = cc.convertFrom(p);
			System.out.println(result);
			assertNotNull(result);
			ContentConverter<String, Map> ccm = (ContentConverter<String, Map>) handler.getConverter(Map.class);
			testPersonConversion(ccm, result);
		}
		
		public static class Person {
			
			public String firstName;
			public String lastName;
			public int age;
			public Address address;
			public List<PhoneNumber> phoneNumber;
			public Gender gender;
		}
		
		public static class Address {
			
			public String streetAddress;
			public String city;
			public String state;
			public String postalCode;
		}
		
		public static class PhoneNumber {
			
			public enum PhoneType {home, fax};
			
			public PhoneType type;
			public String number;
		}
		
		public static class Gender {
			
			public enum GenderType {male, female};
			
			public GenderType type;
		}
}
