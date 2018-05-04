package com.bagri.support.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class JSONUtilsTest {
	
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
					
	@Test
	public void testMapFromJSON() throws Exception {
		Map map = JSONUtils.mapFromJSON(json);
		assertNotNull(map);
		assertEquals(6, map.size());
		assertEquals("John", map.get("firstName"));
		assertEquals("Smith", map.get("lastName"));
		assertEquals(25, map.get("age"));
		assertTrue(map.get("address") instanceof Map);
		assertTrue(map.get("phoneNumber") instanceof List);
		assertEquals(2, ((List) map.get("phoneNumber")).size());
		assertTrue(map.get("gender") instanceof Map);
		assertEquals("male", ((Map) map.get("gender")).get("type"));
	}
	

}
