package com.bagri.core.server.api.df.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.stream.JsonGenerator;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.ParseResults;
import com.bagri.core.server.api.df.json.JsonpHandler;
import com.bagri.core.server.api.df.json.JsonpParser;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class MapParserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
		//System.setProperty(pn_log_level, "trace");
	}

	@Test
	public void testParse() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		MapParser parser = new MapParser(model);
		Map<String, Object> content = new HashMap<>();
		content.put("firstName", "John");
		content.put("lastName", "Smith");
		content.put("age", 25L);
		ParseResults results = parser.parse(content); 
		List<Data> data = results.getResults();
		//System.out.println(elts);
		assertNotNull(data);
		assertEquals(4, data.size()); 
	}

	@Test
	public void testConversion() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		MapParser parser = new MapParser(model);
		Map<String, Object> content = new HashMap<>();
		content.put("firstName", "John");
		content.put("lastName", "Smith");
		content.put("age", 25L);
		ParseResults results = parser.parse(content); 
		List<Data> data = results.getResults();
		
		JsonpHandler jh = new JsonpHandler(model);
		Properties props = new Properties();
		props.setProperty(JsonGenerator.PRETTY_PRINTING, "true");
		jh.init(props);
		ContentBuilder<String> builder = jh.getBuilder();
		String json = builder.buildContent(data);
		System.out.println(json);
		assertNotNull(json);
	}
	
	@Test
	public void testByteArray() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		MapParser parser = new MapParser(model);
		Map<String, Object> content = new HashMap<>();
		content.put("firstName", "John".getBytes());
		content.put("lastName", "Smith".getBytes());
		content.put("age", new byte[] {25});
		ParseResults results = parser.parse(content); 
		assertEquals(4, results.getResultSize());
		List<Data> data = results.getResults();
		assertEquals(4, data.size());
		int length = 8 + // null at root Document
				"@firstName".length() + "John".getBytes().length + 
				"@lastName".length() + "Smith".getBytes().length + 
				"@age".length() + 1; 
		assertEquals(length, results.getContentLength());
	}
	
	@Test
	public void testParsePerson() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		MapParser parser = new MapParser(model);
		Map<String, Object> person = new HashMap<>();
		person.put("firstName", "John");
		person.put("lastName", "Smith");
		person.put("age", 25L);
		Map<String, Object> address = new HashMap<>();
		address.put("streetAddress", "21 2nd Street");
		address.put("city", "New York");
		address.put("state", "NY");
		address.put("postalCode", "10021");
		person.put("address", address);
		List<Map<String, Object>> phones = new ArrayList<>(2);
		Map<String, Object> phone1 = new HashMap<>();
		phone1.put("type", "home");
		phone1.put("number", "212 555-1234");
		phones.add(phone1);
		Map<String, Object> phone2 = new HashMap<>();
		phone2.put("type", "fax");
		phone2.put("number", "646 555-4567");
		phone2.put("comment", null);
		phones.add(phone2);
		person.put("phoneNumber", phones);
		Map<String, Object> gender = new HashMap<>();
		gender.put("type", GenderType.male);
		person.put("gender", gender);
		ParseResults results = parser.parse(person); 
		List<Data> data = results.getResults();
		//System.out.println(elts);
		assertNotNull(data);
		assertEquals(19, data.size());
		
		JsonpHandler jh = new JsonpHandler(model);
		Properties props = new Properties();
		props.setProperty(JsonGenerator.PRETTY_PRINTING, "true");
		jh.init(props);
		ContentBuilder<String> builder = jh.getBuilder();
		String json = builder.buildContent(data);
		System.out.println(json);
		assertNotNull(json);
	}
	
	enum GenderType {
		male, female;
	}

}

