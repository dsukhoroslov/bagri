package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_document_collections;
import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_node_instance;
import static com.bagri.core.Constants.pn_schema_format_default;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.Document;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.support.util.JMXUtils;

public class MapDocumentManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
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
			schema.setProperty(pn_schema_format_default, "MAP");
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "maps", "", "custom", true);
			schema.addCollection(collection);
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

	//protected String getFileName(String original) {
	//	return original.substring(0, original.indexOf(".")) + ".json";
	//}
	
	@Override
	protected Properties getDocumentProperties() {
		Properties props = new Properties();
		props.setProperty(pn_document_data_format, "MAP");
		return props;
	}
	
	@Test
	public void createMapDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		Map<String, Object> m1 = new HashMap<>();
		m1.put("intProp", 1); 
		m1.put("boolProp", Boolean.FALSE);
		m1.put("strProp", "XYZ");
	    Properties props = new Properties();
		props.setProperty(pn_document_collections, "maps");
		props.setProperty(pn_document_data_format, "MAP");
		Document mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test1", m1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		Map<String, Object> m2 = xRepo.getDocumentManagement().getDocumentAsMap(mDoc.getUri(), props);
		assertEquals(m1.get("intProp"), m2.get("intProp"));
		assertEquals(m1.get("boolProp"), m2.get("boolProp"));
		assertEquals(m1.get("strProp"), m2.get("strProp"));
/*
		m2.put("intProp", 2); 
		m2.put("boolProp", Boolean.TRUE);
		m2.put("strProp", "ABC");
		txId = xRepo.getTxManagement().beginTransaction();
		mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test2.xml", m2, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = //"declare default element namespace \"\";\n" + 
				"declare variable $value external;\n" +
				"for $doc in fn:collection()/map\n" +
				//"where $doc/intProp = $value\n" +
				"where $doc[intProp = $value]\n" +
				"return $doc/strProp/text()";
		
		Map<String, Object> params = new HashMap<>();
		params.put("value", 0);
		ResultCursor results = query(query, params, null);
		assertFalse(results.next());
		results.close();
		
		params.put("value", 1);
		results = query(query, params, null);
		assertTrue(results.next());
		
		props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) results.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("XYZ", text);
		assertFalse(results.next());
		results.close();
*/		
	}
		
	@Test
	public void queryMapDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		Map<String, Object> m1 = new HashMap<>();
		m1.put("intProp", 1); 
		m1.put("boolProp", Boolean.TRUE);
		m1.put("strProp", "ABC");
	    Properties props = new Properties();
		props.setProperty(pn_document_collections, "maps");
		props.setProperty(pn_document_data_format, "MAP");
		Document mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test2", m1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());

		m1.clear();
		m1.put("intProp", 2); 
		m1.put("boolProp", Boolean.FALSE);
		m1.put("strProp", "CDE");
		mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test3", m1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		
		m1.clear();
		m1.put("intProp", 3); 
		m1.put("boolProp", Boolean.FALSE);
		m1.put("strProp", "EFGH");
		mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test4", m1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());

		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				//"declare variable $value external;\n" +
				"for $doc in fn:collection('maps')\n" +
				//"let $map := m:get($doc, 'map')\n" +
				"where m:get($doc, '@intProp') = 2\n" +
				"return fn:serialize($doc, map{'method': 'json'})";
		
		Map<String, Object> params = new HashMap<>();
		//params.put("value", "CDE");
		//params.put("value", 2);
		try (ResultCursor results = query(query, params, null)) {
			assertTrue(results.next());
			//String text = results.getString();
			//assertNotNull(text);
			Object value = results.getObject();
			assertNotNull(value);
			//assertEquals("{\"boolProp\":false,\"intProp\":2,\"strProp\":\"CDE\"}", text);
			assertFalse(results.next());
		}
	}
	
	@Test
	public void queryJsonDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		String s1 = "{\"boolProp\":true,\"intProp\":1,\"strProp\":\"ABC\"}";
	    Properties props = new Properties();
		props.setProperty(pn_document_collections, "maps");
		props.setProperty(pn_document_data_format, "JSON");
		Document mDoc = xRepo.getDocumentManagement().storeDocumentFromString("json_test2", s1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());

		s1 = "{\"boolProp\":false,\"intProp\":2,\"strProp\":\"CDE\"}";
		mDoc = xRepo.getDocumentManagement().storeDocumentFromString("json_test3", s1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		
		s1 = "{\"boolProp\":false,\"intProp\":3,\"strProp\":\"EFGH\"}";
		mDoc = xRepo.getDocumentManagement().storeDocumentFromString("json_test4", s1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());

		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				//"declare variable $value external;\n" +
				"for $doc in fn:collection('maps')\n" +
				"where m:get($doc, 'intProp') = 2\n" +
				"return fn:serialize($doc, map{'method': 'json'})";
		
		Map<String, Object> params = new HashMap<>();
		//params.put("value", "CDE");
		//params.put("value", 2);
		try (ResultCursor results = query(query, params, null)) {
			assertTrue(results.next());
			//String text = results.getString();
			//assertNotNull(text);
			Object value = results.getObject();
			assertNotNull(value);
			//assertEquals("{\"boolProp\":false,\"intProp\":2,\"strProp\":\"CDE\"}", text);
			assertFalse(results.next());
		}
	}
	
}
