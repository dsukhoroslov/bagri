package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.support.util.JMXUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.xquery.XQItemAccessor;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class JsonQueryManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/json/";
		System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "json.properties");
		System.setProperty(pn_config_path, "src/test/resources");
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
			Properties props = loadProperties("src/test/resources/json.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			schema.setProperty(pn_schema_format_default, "JSON");
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "securities", "/{http://tpox-benchmark.com/security}Security", "all securities", true);
			schema.addCollection(collection);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
			long txId = xRepo.getTxManagement().beginTransaction();
			createDocumentTest(sampleRoot + "security1500.json");
			createDocumentTest(sampleRoot + "security5621.json");
			createDocumentTest(sampleRoot + "security9012.json");
			xRepo.getTxManagement().commitTransaction(txId);
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
	}
	
	//protected String getFileName(String original) {
	//	return original.substring(0, original.indexOf(".")) + ".json";
	//}
	
	protected Properties getDocumentProperties() {
		Properties props = super.getDocumentProperties();
		props.setProperty(pn_document_collections, "securities");
		props.setProperty(pn_document_data_format, "JSON");
		return props;
	}

	@Test
	public void convertJsonDocumentsTest() throws Exception {
	
		String query = "for $map in fn:collection()\n" + 
				"let $props := entry('method', 'json')\n" +
				"let $json := fn:serialize($map, $props)\n" +
				"return fn:json-to-xml($json)";
		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getAtomicValue();
				cnt++;
			}
			assertEquals(3, cnt);
		}
	}
	
	@Test
	public void serializeJsonDocumentsTest() throws Exception {
	
		String query = "for $uri in fn:uri-collection()\n" +
				"let $map := fn:json-doc($uri)\n" +
				"let $props := map { 'method': 'json' }\n" +
				"return fn:serialize($map, $props)";
		
		Properties props = new Properties();
		//props.setProperty("method", "json");
		try (ResultCursor<XQItemAccessor> results = query(query, null, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getAtomicValue();
				cnt++;
			}
			assertEquals(3, cnt);
		}
	}

	@Test
	public void getJsonDocumentsTest() throws Exception {
	
		String query = "for $map in fn:collection(\"securities\")\n" + 
				"let $sec := get($map, 'Security')\n" +
				"where get($sec, 'Symbol') = 'IBM'\n" +
				"return $sec?('Name')";
		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getAtomicValue();
				assertEquals("Internatinal Business Machines Corporation", text);
				cnt++;
			}
			assertEquals(1, cnt);
		}
	}
	
	@Test
	public void queryJsonDocumentsTest() throws Exception {
	
		String query = "for $map in fn:collection(\"securities\")\n" +
				"let $sec := get($map, 'Security')\n" +
				"where get($sec, 'id') = 5621\n" +
				"return fn:serialize($map, map{'method': 'json'})";
		
		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, null, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getAtomicValue();
				cnt++;
			}
			assertEquals(1, cnt);
		}
	}

	@Test
	public void queryStartsWithDocumentsTest() throws Exception {
	
		String query = "for $map in fn:collection(\"securities\")\n" +
				"let $sec := get($map, 'Security')\n" +
				"let $price := get($sec, 'Price')\n" +
				"let $p52 := get($price, 'Price52week')\n" +
				"let $phd := get($p52, 'Price52week-high-date')\n" +
				"where fn:starts-with($phd, '2002')\n" +
				"return $phd"; //fn:serialize($map, map{'method': 'json'})";
		
		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, null, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getAtomicValue();
				assertEquals("2002-11-02", text);
				cnt++;
			}
			assertEquals(2, cnt);
		}
	}
}
