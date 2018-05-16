package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.getBasicDataFormats;
import static com.bagri.core.test.TestUtils.loadProperties;
import static org.junit.Assert.assertEquals;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQItemAccessor;

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
import com.bagri.core.system.Parameter;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.support.util.FileUtils;
import com.bagri.support.util.JMXUtils;

public class QueryModuleTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/mp/";
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
			props.setProperty(pn_xqj_baseURI, "file:/../../etc/samples/mp/");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			//schema.setProperty(pn_schema_format_default, "JSON");
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Product_Inventory", "/", "inventories", true);
			schema.addCollection(collection);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			String fName = sampleRoot + "/inventory_service.xq";
			String mBody = FileUtils.readTextFile(fName); //do I need to read it from file?
			Module module = new Module(1, new Date(), JMXUtils.getCurrentUser(), 
					"in_svc", fName, "inv module", "inv", "http://mpoffice.ru/inv", mBody, true);
			List<Module> modules = new ArrayList<>();
			modules.add(module);
			xdmRepo.setModules(modules);
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
			
			long txId = xRepo.getTxManagement().beginTransaction();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("../../etc/samples/mp/mp2/"), "*.json")) {
			    for (Path path: stream) {
					createDocumentTest(path.toFile().getAbsolutePath());
			    }
			}		
			xRepo.getTxManagement().commitTransaction(txId);
		}
	}

	@After
	public void tearDown() throws Exception {
		xRepo.close();
	}
	
	@Test
	public void queryProductsByCategoryTest() throws Exception {
	
		String query = "import module namespace inv=\"http://mpoffice.ru/inv\" at \"inventory_service.xq\";\n" +
	        		   "declare variable $pcat external;\n" +
	        		   "declare variable $rid external;\n" +
	        		   "declare variable $psize external;\n" +
	        		   "declare variable $pnum external;\n" +
					   "\n" +
					   "inv:get-products-by-category($pcat, $rid, $psize, $pnum)\n";

		Map<String, Object> params = new HashMap<>();
		params.put("pcat", "091210");
		params.put("rid", null); //12345);
		params.put("psize", 100);
		params.put("pnum", 1);
		Properties props = new Properties();
		props.setProperty(pn_query_customPaths, "0=/inventory/product-category"); //;2=/inventory/virtual-stores/status;3=/inventory/virtual-stores/region-id");
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				cnt++;
			}  
			assertEquals(2, cnt);
		}
	}
	
	@Test
	public void queryProductByIdTest() throws Exception {
	
		String query = "import module namespace inv=\"http://mpoffice.ru/inv\" at \"inventory_service.xq\";\n" +
	        		   "declare variable $pid external;\n" +
	        		   "declare variable $rid external;\n" +
					   "\n" +
					   "inv:get-product-by-pid($pid, $rid)\n";

		Map<String, Object> params = new HashMap<>();
		params.put("pid", 7525L);
		params.put("rid", null); //235
		Properties props = new Properties();
		props.setProperty(pn_query_customPaths, "3=/inventory/virtual-stores/status;6=/inventory/virtual-stores/region-id");
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				cnt++;
			}  
			assertEquals(4, cnt);
		}
	}
	
	@Test
	public void queryProductsByIdTest() throws Exception {
	
		String query = "import module namespace inv=\"http://mpoffice.ru/inv\" at \"inventory_service.xq\";\n" +
	        		   "declare variable $pids external;\n" +
	        		   "declare variable $rid external;\n" +
					   "\n" +
					   "inv:get-products-by-pid($pids, $rid)\n";

		Map<String, Object> params = new HashMap<>();
		//List<Integer> pids = new ArrayList<>();
		List<String> pids = new ArrayList<>();
		pids.add("5977");
		pids.add("7525");
		pids.add("11386");
		params.put("pids", pids);
		params.put("rid", null); //235
		//params.put("var2", "active"); 
		Properties props = new Properties();
		// %bgdb:property("bdb.query.customQuery", "(/inventory/product-id = pids) AND ((/inventory/virtual-stores/status = var2) AND ((/inventory/virtual-stores/region-id = rid) OR (rid = null)))") :)
		// %bgdb:property("bdb.query.customQuery", "1=(/inventory/product-id = pids) AND (/inventory/virtual-stores/status = literal_24_40)") :)		
		//props.setProperty(pn_query_customPaths, "3=/inventory/virtual-stores/status;6=/inventory/virtual-stores/region-id");
		props.setProperty(pn_query_customQuery, "1=(/inventory/product-id = pids) and (/inventory/virtual-stores/status = literal_24_40)"); // and ((/inventory/virtual-stores/region-id = rid) or (rid = null)))");
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				cnt++;
			}  
			assertEquals(10, cnt);
		}
	}

	@Test
	public void checkProductsCacheTest() throws Exception {
	
		String query = "import module namespace inv=\"http://mpoffice.ru/inv\" at \"inventory_service.xq\";\n" +
	        		   "declare variable $pids external;\n" +
	        		   "declare variable $rid external;\n" +
					   "\n" +
					   "inv:get-products-by-pid($pids, $rid)\n";

		Map<String, Object> params = new HashMap<>();
		//List<Integer> pids = new ArrayList<>();
		List<String> pids = new ArrayList<>();
		pids.add("11386");
		params.put("pids", pids);
		params.put("rid", null); //235
		Properties props = new Properties();
		props.setProperty(pn_query_customPaths, "1=/inventory/product-id EQ pids");
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				cnt++;
			}  
			assertEquals(2, cnt);
		}
	}
}
