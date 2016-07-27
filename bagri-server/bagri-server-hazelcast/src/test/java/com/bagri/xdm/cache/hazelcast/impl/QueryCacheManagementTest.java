package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.pn_client_id;
import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static com.bagri.xdm.common.Constants.pn_client_fetchSize;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.test.BagriManagementTest;
import com.bagri.xdm.system.Collection;
import com.bagri.xdm.system.Schema;

public class QueryCacheManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
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
			xdmRepo.setSchema(schema);
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
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
		Properties props = new Properties();
		props.setProperty(pn_client_id, "1");
		props.setProperty(pn_client_fetchSize, "1");
		ResultCursor rc = query(query, params, props);
		assertNotNull(rc);
		assertTrue(rc.next());
		rc.close();
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
		
		Properties props = new Properties();
		props.setProperty(pn_client_id, "2");
		props.setProperty(pn_client_fetchSize, "1");
		ResultCursor rc = query(query, params, props);
		assertNotNull(rc);
		assertTrue(rc.next());
		rc.close();
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
