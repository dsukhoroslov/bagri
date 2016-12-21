package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_document_collections;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_schema_format_default;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.server.api.df.json.JsonApiParser;
import com.bagri.core.server.api.df.json.JsonBuilder;
import com.bagri.core.system.Collection;
import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.JMXUtils;

public class JsonQueryManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("bdb.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "json.properties");
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
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "securities", "/{http://tpox-benchmark.com/security}Security", "all securities", true);
			schema.addCollection(collection);
			xdmRepo.setSchema(schema);
			DataFormat df = new DataFormat(1, new java.util.Date(), "", "JSON", null, "application/json", null, 
					JsonApiParser.class.getName(), JsonBuilder.class.getName(), true, null);
			ArrayList<DataFormat> cFormats = new ArrayList<>(1);
			cFormats.add(df);
			xdmRepo.setDataFormats(cFormats);
			
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
		Properties props = new Properties();
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
		ResultCursor docs = query(query, null, null);
		assertNotNull(docs);
		List<String> jsons = new ArrayList<>();
		while (docs.next()) {
			String json = docs.getString();
			jsons.add(json);
			//System.out.println(json);
		}
		docs.close();
		assertEquals(3, jsons.size());
	}
	
	@Test
	public void serializeJsonDocumentsTest() throws Exception {
	
		String query = "for $uri in fn:uri-collection()\n" +
				"let $map := fn:json-doc($uri)\n" +
				"let $props := map { 'method': 'json' }\n" +
				"return fn:serialize($map, $props)";
		
		Properties props = new Properties();
		//props.setProperty("method", "json");
		ResultCursor docs = query(query, null, props);
		assertNotNull(docs);
		props = new Properties();
		//props.setProperty("method", "json");
		List<String> jsons = new ArrayList<>();
		while (docs.next()) {
			String json = docs.getString();
			jsons.add(json);
			//System.out.println(json);
		}
		docs.close();
		assertEquals(3, jsons.size());
	}

	@Test
	public void getJsonDocumentsTest() throws Exception {
	
		String query = "for $map in fn:collection(\"securities\")\n" + 
				"let $v := get($map, 'Security')\n" +
				//"where get($v, '-id') = '5621'\n" +
				"where get($v, 'Symbol') = 'IBM'\n" +
				"return $v?('Symbol', 'Name')";
		ResultCursor docs = query(query, null, null);
		assertNotNull(docs);
		Properties props = new Properties();
		props.setProperty("method", "text");
		List<String> results = new ArrayList<>();
		while (docs.next()) {
			String text = docs.getString();
			results.add(text);
		}
		docs.close();
		assertEquals(2, results.size());
	}
	
	@Test
	public void queryJsonDocumentsTest() throws Exception {
	
		String query = "for $uri in fn:uri-collection(\"securities\")\n" +
				"let $props := map {'method': 'json'}\n" +
				"let $map := fn:json-doc($uri)\n" +
				"let $sec := get($map, 'Security')\n" +
				"where get($sec, 'id') = 5621\n" +
				//"where get($sec, 'Symbol') = 'IBM'\n" +
				"return fn:serialize($map, $props)";
		
		Properties props = new Properties();
		//props.setProperty("method", "json");
		ResultCursor docs = query(query, null, props);
		assertNotNull(docs);
		props = new Properties();
		//props.setProperty("method", "json");
		List<String> jsons = new ArrayList<>();
		while (docs.next()) {
			String json = docs.getString();
			jsons.add(json);
			//System.out.println(json);
		}
		docs.close();
		assertEquals(1, jsons.size());
	}
}
