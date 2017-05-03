package com.bagri.core.server.api.df.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.server.api.ModelManagement;
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
		List<Data> elts = parser.parse(content);
		//System.out.println(elts);
		assertNotNull(elts);
		assertEquals(5, elts.size()); 
	}
	
}
