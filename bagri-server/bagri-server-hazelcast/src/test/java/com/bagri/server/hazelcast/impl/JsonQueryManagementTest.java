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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
					//1, "securities", "/{http://tpox-benchmark.com/security}Security", "all securities", true);
					1, "securities", "/Security", null, "all securities", true);
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
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"for $map in fn:collection()\n" + 
				"let $props := m:entry('method', 'json')\n" +
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
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"for $map in fn:collection(\"securities\")\n" + 
				"let $sec := m:get($map, 'Security')\n" +
				"where m:get($sec, 'Symbol') = 'IBM'\n" +
				//"return $sec?('Name')";
				"return m:get($sec, 'Name')";
				//"where get(get($map, 'Security'), 'Symbol') = 'IBM'\n" +
				//"return get(get($map, 'Security'), 'Name')";
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
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"for $map in fn:collection(\"securities\")\n" +
				"let $sec := m:get($map, 'Security')\n" +
				"where m:get($sec, 'id') = 5621\n" +
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
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"for $map in fn:collection(\"securities\")\n" +
				"let $sec := m:get($map, 'Security')\n" +
				"let $price := m:get($sec, 'Price')\n" +
				"let $p52 := m:get($price, 'Price52week')\n" +
				"let $phd := m:get($p52, 'Price52week-high-date')\n" +
				"where fn:starts-with($phd, '2002')\n" +
				"return $phd"; 
				//"where fn:starts-with(get(get(get(get($map, 'Security'), 'Price'), 'Price52week'), 'Price52week-high-date'), '2002')\n" +
				//"return get(get(get(get($map, 'Security'), 'Price'), 'Price52week'), 'Price52week-high-date')"; 
		
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

	@Test
	public void queryEndsWithDocumentsTest() throws Exception {
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"for $map in fn:collection(\"securities\")\n" +
				"let $sec := m:get($map, 'Security')\n" +
				"let $price := m:get($sec, 'Price')\n" +
				"let $p52 := m:get($price, 'Price52week')\n" +
				"let $pld := m:get($p52, 'Price52week-low-date')\n" +
				"where fn:ends-with($pld, '05-12')\n" +
				"return $pld"; 
		
		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, null, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getAtomicValue();
				assertEquals("2002-05-12", text);
				cnt++;
			}
			assertEquals(2, cnt);
		}
	}
	
	@Test
	public void queryContainsDocumentsTest() throws Exception {
	
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				"for $map in fn:collection(\"securities\")\n" +
				"let $sec := m:get($map, 'Security')\n" +
				"let $price := m:get($sec, 'Price')\n" +
				"let $p52 := m:get($price, 'Price52week')\n" +
				"let $pld := m:get($p52, 'Price52week-low-date')\n" +
				"where fn:contains($pld, '04-03')\n" +
				"return $pld"; 

		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, null, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getAtomicValue();
				assertEquals("2004-03-05", text);
				cnt++;
			}  
			assertEquals(1, cnt);
		}
	}

	@Test
	//@Ignore
	public void queryProductDocumentsTest() throws Exception {

		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
					   "declare namespace a=\"http://www.w3.org/2005/xpath-functions/array\";\n" +
					   "declare namespace bgdb=\"http://bagridb.com/bdb\";\n" +
					   "declare option bgdb:custom-path \"true\";\n" +
					   //"declare variable $pids external;\n" +
					   "declare variable $rid external;\n" +
					   "\n" +
					   //"  let $props := map {'method': 'json'}\n" +
					   "let $pids := (66751, 98514, 31386)\n" +
					   "for $map in fn:collection(\"securities\")\n" +
					   "for $pid in $pids\n" +
					   "  let $inv := m:get($map, 'inventory')\n" +
					   "  for $prod in a:flatten($inv)\n" +
					   "    where m:get($prod, 'product-id') = $pid\n" +
					   //"    let $vs := m:get($prod, 'virtual-stores')\n" +
					   "    for $item in a:flatten(m:get($prod, 'virtual-stores'))\n" +
					   "      where (m:get($item, 'status') = 'active')\n" +
					   "        and (m:get($item, 'region-id') = $rid)\n" + 
					   //"        and (fn:not(fn:exists($rid)) or m:get($item, 'region-id') = $rid)\n" + 
					   "      let $item := m:put($item, 'product-id', m:get($prod, 'product-id'))\n" +
					   "      let $item := m:put($item, 'product-category', m:get($prod, 'product-category'))\n" +
					   "      return fn:serialize($item, map{'method': 'json'})"; 	

		Map<String, Object> params = new HashMap<>();
		List<Integer> pids = new ArrayList<>();
		//pids.add(66751);
		//pids.add(98514);
		//pids.add(31386);
		//params.put("pids", pids);
		params.put("rid", 1161);
		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getAtomicValue();
				//assertEquals("2004-03-05", text);
				cnt++;
			}  
			assertEquals(0, cnt);
		}
	}
	
	@Test
	//@Ignore
	public void queryCategoryDocumentsTest() throws Exception {
	
		//long txId = xRepo.getTxManagement().beginTransaction();
		//try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("../../etc/samples/mp/mp2/"), "*.json")) {
		//    for (Path path: stream) {
		//		createDocumentTest(path.toFile().getAbsolutePath());
		//    }
		//}		
		//xRepo.getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
					   "declare namespace a=\"http://www.w3.org/2005/xpath-functions/array\";\n" +
					   //"declare variable $pcat external;\n" +
					   //"declare variable $rid external;\n" +
					   "\n" +
					   "let $props := map {'method': 'json'}\n" +
					   "let $sq := for $map in fn:collection(\"securities\")\n" +
					   "  let $inv := m:get($map, 'inventory')\n" +
					   "  for $prod in a:flatten($inv)\n" +
					   "    where fn:starts-with(m:get($prod, 'product-category'), '04090')\n" + //$pcat)\n" +
					   "    let $vs := m:get($prod, 'virtual-stores')\n" +
					   "    for $item in a:flatten($vs)\n" +
					   "      where (m:get($item, 'status') = 'active')\n" +
					   //"        and (m:get($item, 'region-id') = $rid)\n" + 
					   //"        and (fn:not(fn:exists($rid)) or m:get($item, 'region-id') = $rid)\n" + 
					   "      let $item := m:put($item, 'product-id', m:get($prod, 'product-id'))\n" +
					   "      let $item := m:put($item, 'product-category', m:get($prod, 'product-category'))\n" +
					   "      order by m:get($item, 'product-id')\n" + 	
					   "      return fn:serialize($item, $props)\n" + 	
					   "\n" +
					   "for $item at $cnt in fn:subsequence($sq, 3, 2)\n" +
					   "  return $item";

		Map<String, Object> params = new HashMap<>();
		//params.put("pcat", "04090");
		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getAtomicValue();
				//assertEquals("2004-03-05", text);
				cnt++;
			}  
			assertEquals(2, cnt);
		}
	}

	@Test
	//@Ignore
	public void queryDeliveryDocumentsTest() throws Exception {
	
		//long txId = xRepo.getTxManagement().beginTransaction();
		//try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("../../etc/samples/mp/mp3/"), "*.json")) {
		//    for (Path path: stream) {
		//		createDocumentTest(path.toFile().getAbsolutePath());
		//    }
		//}		
		//xRepo.getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
					   "declare namespace a=\"http://www.w3.org/2005/xpath-functions/array\";\n" +
					   //"declare variable $rid external;\n" +
					   "\n" +
					   "for $map in fn:collection(\"securities\")\n" +
					   "  let $props := map {'method': 'json'}\n" +
					   "  let $ships := m:get($map, 'shipments')\n" +
					   "  for $from in a:flatten($ships)\n" +
					   "    where m:get($from, 'region-id') = 462\n" + //$pid\n" +
					   "    let $dests := m:get($from, 'destinations')\n" +
					   "    for $item in a:flatten($dests)\n" +
					   "      where m:get($item, 'region-id') = 304\n" +
					   "      return fn:serialize($item, $props)"; 	

		Map<String, Object> params = new HashMap<>();
		Properties props = new Properties();
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getAtomicValue();
				//assertEquals("2004-03-05", text);
				cnt++;
			}  
			assertEquals(2, cnt);
		}
	}
}

