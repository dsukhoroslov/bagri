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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class QueryCacheManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/tpox/";
		//System.setProperty(pn_log_level", "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
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
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", null, "securities", true);
			schema.addCollection(collection);
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
	}

	@Test
	public void invalidateQueryCacheTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		storeSecurityTest();
		xRepo.getTxManagement().commitTransaction(txId);
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return $sec\n";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", "VFINX");
		Properties props = getDocumentProperties();
		props.setProperty(pn_client_fetchSize, "1");
		checkCursorResult(query, params, props, null);
		Thread.currentThread().sleep(1000);
		
		// here we must have 1 result cached
		List<Object> results = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, params, props);
		assertNotNull(results);
		assertEquals(1, results.size());
		
		removeDocumentsTest(); 
		//Thread.currentThread().sleep(1000);
		//updateDocumentTest(0, null, sampleRoot + getFileName("security5621.xml"));
		// here we must have 0 result cached
		results = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, params, props);		
		assertNull(results);
		//assertFalse(itr.hasNext());
	}

	@Test
	public void invalidateResultCacheTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		createDocumentTest(sampleRoot + getFileName("security5621.xml"));
		xRepo.getTxManagement().commitTransaction(txId);
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sect external;\n" + 
			"declare variable $pemin external;\n" +
			"declare variable $pemax external;\n" + 
			"declare variable $yield external;\n" + 
			"for $sec in fn:collection(\"CLN_Security\")/Security\n" +
		  	"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
			"return	<Security>\n" +	
			"\t{$sec/Symbol}\n" +
			"\t{$sec/Name}\n" +
			"\t{$sec/SecurityType}\n" +
			"\t{$sec/SecurityInformation//Sector}\n" +
			"\t{$sec/PE}\n" +
			"\t{$sec/Yield}\n" +
			"</Security>";
		Map<String, Object> params = new HashMap<>();
		params.put("sect", "Technology"); 
		params.put("pemin", new java.math.BigDecimal("25.0"));
		params.put("pemax", new java.math.BigDecimal("28.0"));
		params.put("yield", new java.math.BigDecimal("0.1"));
		
		Properties props = getDocumentProperties();
		props.setProperty(pn_client_fetchSize, "1");
		checkCursorResult(query, params, props, null);
		Thread.currentThread().sleep(1000);
		
		// here we must have 1 result cached
		List<Object> results = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, params, props);		
		assertNotNull(results);
		assertEquals(1, results.size());
		
		txId = xRepo.getTxManagement().beginTransaction();
		createDocumentTest(sampleRoot + getFileName("security9012.xml"));
		xRepo.getTxManagement().commitTransaction(txId);
		// here we must have 0 result cached
		// but there is no code to do this!
		results = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, params, props);		
		assertNull(results);
		//assertFalse(itr.hasNext());
	}
	
}
