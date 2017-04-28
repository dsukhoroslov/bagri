package com.bagri.core.server.api.df.json;

import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.df.json.JsonpModeler;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class JsonpModelerTest {

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
			"      \"number\": \"646 555-4567\",\n" +
			"      \"comment\": null\n" +
			"    }\n" +
			"  ],\n" +
			"  \"gender\": {\n" +
			"    \"type\": \"male\"\n" +
			"  }\n" +
			"}";
				
	
	private static String schema = 
			"{\n" +
	    	"  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
	    	"  \"definitions\": {},\n" +
	    	"  \"id\": \"http://bagridb.com/person.json\",\n" +
	    	"  \"properties\": {\n" +
	        "    \"address\": {\n" +
	        "      \"properties\": {\n" +
	        "        \"city\": {\n" +
	        "          \"type\": \"string\"\n" +
	        "         },\n" +
	        "        \"postalCode\": {\n" +
	        "          \"type\": \"string\"\n" +
	        "         },\n" +
	        "        \"state\": {\n" +
	        "          \"type\": \"string\"\n" +
	        "         },\n" +
	        "        \"streetAddress\": {\n" +
	        "          \"type\": \"string\"\n" +
	        "         }\n" +
	        "       },\n" +
	        "      \"required\": [\"postalCode\", \"city\", \"streetAddress\"],\n" +
	        "      \"type\": \"object\"\n" +
	        "     },\n" +
	        "    \"age\": {\n" +
	        "      \"type\": \"integer\"\n" +
	        "     },\n" +
	        "    \"firstName\": {\n" +
	        "      \"type\": \"string\"\n" +
	        "     },\n" +
	        "    \"gender\": {\n" +
	        "      \"properties\": {\n" +
	        "        \"type\": {\n" +
	        "          \"enum\": [\"male\", \"female\"],\n" +
	        "            \"type\": \"string\"\n" +
	        "         }\n" +
	        "       },\n" +
	        "      \"required\": [\"type\"],\n" +
	        "      \"type\": \"object\"\n" +
	        "     },\n" +
	        "    \"lastName\": {\n" +
	        "      \"type\": \"string\"\n" +
	        "     },\n" +
	        "    \"phoneNumber\": {\n" +
	        "      \"items\": {\n" +
	        "        \"properties\": {\n" +
	        "          \"number\": {\n" +
	        "            \"type\": \"string\"\n" +
	        "           },\n" +
	        "          \"type\": {\n" +
	        "            \"type\": \"string\"\n" +
	        "           }\n" +
	        "        },\n" +
	        "        \"required\": [\"number\"],\n" +
	        "        \"type\": \"object\"\n" +
	        "       },\n" +
	        "      \"type\": \"array\"\n" +
	        "     }\n" +
	        "   },\n" +
	        "  \"required\": [\"firstName\", \"lastName\", \"gender\"],\n" +
	        "  \"type\": \"object\"\n" +
	        "}";
	
	
	private ModelManagement model;
	private ContentModeler modelPro;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
		//System.setProperty(pn_log_level, "trace");
	}

	@Before
	public void setUp() throws Exception {
		model = new ModelManagementImpl();
		modelPro = new JsonpModeler(model);
	}

	//@After
	//public void tearDown() throws Exception {
	//}

	@Test
	public void registerPersonModelTest() throws Exception {
		modelPro.registerModel(schema);
		Collection<Path> paths = model.getTypePaths("/");
		System.out.println(paths);
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
	}

	
}
