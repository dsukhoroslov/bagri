package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_document_collections;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_schema_format_default;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.DocumentManagementTest;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.JMXUtils;

public class JsonDocumentManagementTest extends DocumentManagementTest {
	
    private static ClassPathXmlApplicationContext context;

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
			
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(SchemaRepositoryImpl.class);
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			schema.setProperty(pn_schema_format_default, "JSON");
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
		}
		// set bdb.document.format to JSON !
		//XQProcessor xqp = xdmRepo.getXQProcessor("test_client");
		//xqp.getProperties().setProperty("bdb.document.format", "JSON");
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	protected String getFileName(String original) {
		return original.substring(0, original.indexOf(".")) + ".json";
	}
	
	protected Properties getDocumentProperties() {
		Properties props = new Properties();
		props.setProperty(pn_document_data_format, "JSON");
		return props;
	}

	
	@Test
	public void queryJsonDocumentsTest() throws Exception {
		
		Schema schema = ((SchemaRepositoryImpl) xRepo).getSchema();
		Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 1, "products", "", "all products", true);
		schema.addCollection(collection);
		collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 2, "orders", "", "all orders", true);
		schema.addCollection(collection);
		
		String doc1 = "{\"id\": \"product-1\", \"type\": \"product\", \"name\": \"Pokemon Red\", \"price\": 29.99}";
		String doc2 = "{\"id\": \"order-1\", \"type\": \"order\", \"products\": [{\"product_id\": \"product-1\", \"quantity\": 2}]}";
		long txId = getTxManagement().beginTransaction();
		Properties props = getDocumentProperties();
		props.setProperty(pn_document_collections, "products");
		uris.add(getDocManagement().storeDocumentFromString("product.json", doc1, props).getUri());
		props.setProperty(pn_document_collections, "orders");
		uris.add(getDocManagement().storeDocumentFromString("order.json", doc2, props).getUri());
		getTxManagement().commitTransaction(txId);
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"declare namespace a=\"http://www.w3.org/2005/xpath-functions/array\";\n" +
				"for $ord in collection('orders'), $prod in a:flatten($ord('products')), $pro in collection('products')\n" +
				"where $pro('id') = $prod('product_id')\n" +
				"return serialize(m:put(m:remove($ord, 'products'), 'product', m:put($pro, 'quantity', $prod('quantity'))), map{'method': 'json'})";
				
		ResultCursor docs = query(query, null, null);
		assertNotNull(docs);
		assertTrue(docs.next());
		docs.close();
	}
	
	@Test
	public void queryPersonDocumentsTest() throws Exception {

		long txId = getTxManagement().beginTransaction();
		Properties props = getDocumentProperties();
		//props.setProperty(pn_document_collections, "person");
		uris.add(getDocManagement().storeDocumentFromString("person.json", json, props).getUri());
		getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"declare namespace a=\"http://www.w3.org/2005/xpath-functions/array\";\n" +
				"for $pn in collection()\n" +
				"let $phones := m:get($pn, 'phoneNumber')\n" +
				"for $phone in a:flatten($phones)\n" +
				"where m:get($phone, 'type') = 'fax'\n" +
				"return serialize($phone, map{'method': 'json'})";
				
		ResultCursor docs = query(query, null, null);
		assertNotNull(docs);
		assertTrue(docs.next());
		//System.out.println(docs.getString());
		docs.close();
	}

}
