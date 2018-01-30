package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.model.Document;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.collection.impl.queue.QueueService;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQSequence;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static com.bagri.support.util.XQUtils.mapFromSequence;
import static org.junit.Assert.*;

public class ResultCursorTest extends BagriManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/tpox/";
		//System.setProperty(pn_log_level, "trace");
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
		SchemaRepositoryImpl repo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = repo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			//schema.setProperty(pn_baseURI, sampleRoot);
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
			repo.setSchema(schema);
			repo.setDataFormats(getBasicDataFormats());
			repo.setLibraries(new ArrayList<Library>());
			repo.setModules(new ArrayList<Module>());
			((ClientManagementImpl) repo.getClientManagement()).addClient(client_id, user_name);
			repo.setClientId(client_id);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void fetchResultsTest() throws Exception {
		storeSecurityTest();
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sect external;\n" + 
				"declare variable $pemin external;\n" +
				"declare variable $pemax external;\n" + 
				"declare variable $yield external;\n" + 
				"for $sec in fn:collection(\"CLN_Security\")/Security\n" +
				//"for $sec in fn:collection()/Security\n" +
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
	    params.put("pemin", 25.0f);
	    params.put("pemax", 28.0f);
	    params.put("yield", 0.1f);
		Properties props = getDocumentProperties();
		props.setProperty(pn_client_fetchSize, "1");
		//props.setProperty(pn_client_id, "dummy");
		checkCursorResult(query, params, props, null);
	}

	@Test
	public void fetchSecurityTest() throws Exception {
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", "IBM");
		Properties props = getDocumentProperties();
		props.setProperty(pn_client_fetchSize, "1");
		//props.setProperty(pn_client_id, "dummy");
		checkCursorResult(query, params, props, null);

		props = getDocumentProperties();
		props.setProperty(pn_client_fetchAsynch, "true");
		checkCursorResult(query, params, props, null);
		assertTrue(findDistributedObject(QueueService.SERVICE_NAME, "client:client"));
	}
	
	@Test
	public void fetchSecAsynchTest() throws Exception {
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				//"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
				//"for $sec in fn:collection()/s:Security\n" +
				"for $sec in fn:collection()\n" +
		  		//"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		
		Properties props = getDocumentProperties();
		props.setProperty(pn_client_fetchAsynch, "true");
		try (ResultCursor<XQItemAccessor> results = query(query, null, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				//String text = item.getItemAsString(null);
				//assertTrue("unexpected result: " + text, text.startsWith("<increase>") && text.endsWith("</increase>"));
				cnt++;
			}
			//assertEquals(4, cnt); //results.size());
			assertTrue(findDistributedObject(QueueService.SERVICE_NAME, "client:client"));
		}
	}
	
	private boolean findDistributedObject(String serviceName, String objectName) {
		HazelcastInstance hzi = ((SchemaRepositoryImpl) xRepo).getHzInstance();
		for (DistributedObject svc: hzi.getDistributedObjects()) {
			if (objectName.equals(svc.getName())) {
				if (serviceName == null) {
					return true;
				} else if (serviceName.equals(svc.getServiceName())) {
					return true;
				}
			}
		}
		return false;
	}

	@Test
	public void fetchSecuritiesTest() throws Exception {
		storeSecurityTest();
		
		AtomicInteger counter = new AtomicInteger(2);
		CountDownLatch latch = new CountDownLatch(2);

		Thread th1 = new Thread(new DocQuery("IBM", latch, counter));
		Thread th2 = new Thread(new DocQuery("VFINX", latch, counter));
		th1.start();
		th2.start();
		latch.await();
		assertEquals(0, counter.get());
	}
	
	@Test
	public void fetchMapTest() throws Exception {
	    Properties props = getDocumentProperties();
		props.setProperty(pn_document_data_format, "MAP");
		long txId = xRepo.getTxManagement().beginTransaction();
		Map<String, Object> map = new HashMap<>();
		map.put("intProp", 10); 
		map.put("boolProp", true);
		map.put("strProp", "ABC");
		DocumentAccessor mDoc = xRepo.getDocumentManagement().storeDocument("map_test", map, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				//"declare variable $value external;\n" +
				"for $doc in fn:collection()\n" +
				"where m:get($doc, '@intProp') = 10\n" +
				"return $doc";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			Iterator<XQItemAccessor> itr = results.iterator();
			assertTrue(itr.hasNext());
			Map<String, Object> doc = mapFromSequence((XQSequence) itr.next());
			assertNotNull(doc);
			//System.out.println(doc);
			assertEquals(10, doc.get("intProp"));
			assertEquals(true, doc.get("boolProp"));
			assertEquals("ABC", doc.get("strProp"));
			assertFalse(itr.hasNext());
		}
	}
	
	
	@Test
	public void fetchMapsTest() throws Exception {
	    Properties props = getDocumentProperties();
		props.setProperty(pn_document_data_format, "MAP");
		long txId = xRepo.getTxManagement().beginTransaction();
		for (int i=0; i < 100; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("intProp", i); 
			map.put("boolProp", i % 2 == 0);
			map.put("strProp", "XYZ" + i);
			DocumentAccessor mDoc = xRepo.getDocumentManagement().storeDocument("map_test_" + i, map, props);
			assertNotNull(mDoc);
			assertEquals(txId, mDoc.getTxStart());
			uris.add(mDoc.getUri());
		}
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = "declare namespace m=\"http://www.w3.org/2005/xpath-functions/map\";\n" +
				//"declare variable $value external;\n" +
				"for $doc in fn:collection()\n" +
				"where m:get($doc, '@intProp') >= 20\n" +
				"return serialize($doc, map{'method': 'json'})";

		props.setProperty(pn_xqj_scrollability, "1");
		props.setProperty(pn_client_fetchSize, "5");
		Map<String, Object> params = new HashMap<>();
		//params.put("value", 1);
		try (ResultCursor<XQItemAccessor> results = query(query, params, props)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				assertNotNull(item.getAtomicValue());
				cnt++;
			}
			assertEquals(5, cnt);
		}
	}
	

	private class DocQuery implements Runnable {

		private String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";

		private String symbol;
		private CountDownLatch latch;
		private AtomicInteger counter;
		
		DocQuery(String symbol, CountDownLatch latch, AtomicInteger counter) {
			this.symbol = symbol;
			this.latch = latch;
			this.counter = counter;
		}
		
		@Override
		public void run() {
			Map<String, Object> params = new HashMap<>();
			params.put("sym", symbol);
			Properties props = getDocumentProperties();
			props.setProperty(pn_client_fetchSize, "1");
			//props.setProperty(pn_client_id, "thread2");
			try {
				checkCursorResult(query, params, props, null);
				counter.decrementAndGet();
			} catch (Exception ex) {
				ex.printStackTrace();
				//System.out.println(ex);
			}
			latch.countDown();
		}
	}
	
}
