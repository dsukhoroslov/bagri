package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_node_instance;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.model.Document;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;

public class TransactionManagementImplTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		//System.setProperty(pn_log_level, "trace");
		//System.setProperty("hz.log.level", "trace");
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
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		//Thread.sleep(1000);
	}

	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	public Collection<String> getSecurity(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, Object> params = new HashMap<>();
		params.put(":sec", "/{" + prefix + "}Security");
		return ((QueryManagement) getQueryManagement()).getContent(ec, ":sec", params);
	}
	
	@Test
	public void rollbackTransactionTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		storeSecurityTest();

		Collection<String> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);

		sec = getSecurity("IBM");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);

		xRepo.getTxManagement().rollbackTransaction(txId);
		
		sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 0 but got " + sec.size() + " test documents", sec.size() == 0);

		sec = getSecurity("IBM");
		assertNotNull(sec);
		assertTrue("expected 0 but got " + sec.size() + " test documents", sec.size() == 0);

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		assertTrue("expected 0 but got " + sec.size() + " test documents", sec.size() == 0);
	}
	
	@Test
	public void rollbackTransactionUpdateTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		String uri = doc.getUri();
		uris.add(uri);
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		getTxManagement().commitTransaction(txId);
		int version = doc.getVersion();
	
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().rollbackTransaction(txId);
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		assertEquals(version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
		
		Collection<String> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 0 but got " + sec.size() + " test documents", sec.size() == 0);
	}

	@Test
	public void concurrentUpdateTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		List<Long> keys = new CopyOnWriteArrayList<>();
		keys.add(doc.getDocumentKey());

		Thread th1 = new Thread(new DocUpdater(uri, "security5621.xml", cdl, keys, TransactionIsolation.readCommited));
		Thread th2 = new Thread(new DocUpdater(uri, "security1500.xml", cdl, keys, TransactionIsolation.readCommited));

		th1.start();
		th2.start();
		cdl.await();
		assertTrue("expected 3 versions commited, but got " + keys.size(), keys.size() == 3);
	}	

	@Test
	public void concurrentUpdateRepeatableTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		List<Long> keys = new CopyOnWriteArrayList<>();
		keys.add(doc.getDocumentKey());

		Thread th1 = new Thread(new DocUpdater(uri, "security1500.xml", cdl, keys, TransactionIsolation.repeatableRead));
		Thread th2 = new Thread(new DocUpdater(uri, "security9012.xml", cdl, keys, TransactionIsolation.repeatableRead));

		th1.start();
		th2.start();
		cdl.await();
		assertTrue("expected 2 versions commited, but got " + keys.size(), keys.size() == 2);
	}	
	
	@Test
	public void concurrentRollbackTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		List<Long> keys = new CopyOnWriteArrayList<>();

		Thread th1 = new Thread(new DocUpdater(uri, "security5621.xml", cdl, keys, TransactionIsolation.readCommited));
		Thread th2 = new Thread(new DocUpdater(uri, "security9012.xml", cdl, keys, TransactionIsolation.readCommited));

		th1.start();
		Thread.sleep(50);
		th2.start();
		cdl.await();
		// how should we check it properly??
		//assertFalse("expected less than 3 docs commited", ids.size() == 3);
	}	

	@Test
	public void timeoutTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		getTxManagement().setTransactionTimeout(10);
		List<Long> keys = new CopyOnWriteArrayList<>();

		Thread th1 = new Thread(new DocUpdater(uri, "security5621.xml", cdl, keys, TransactionIsolation.readCommited));
		Thread th2 = new Thread(new DocUpdater(uri, "security9012.xml", cdl, keys, TransactionIsolation.readCommited));
		
		th1.start();
		th2.start();
		cdl.await();
		// fails sometime, not clear why
		//assertFalse("expected less than 3 docs commited", ids.size() == 3);
	}
	
	private class DocUpdater implements Runnable {
		
		private String uri;
		private String uri2;
		private List<Long> keys;
		private CountDownLatch latch;
		private TransactionIsolation tiLevel;
		
		DocUpdater(String uri, String uri2, CountDownLatch latch, List<Long> keys, TransactionIsolation tiLevel) {
			this.uri = uri;
			this.uri2 = uri2;
			this.latch = latch;
			this.keys = keys;
			this.tiLevel = tiLevel;
		}
		
		@Override
		public void run() {
			
			try {
				long txId = getTxManagement().beginTransaction(tiLevel);
				Document doc = updateDocumentTest(uri, sampleRoot + getFileName(uri2));
				getTxManagement().commitTransaction(txId);
				if (doc != null) {
					uris.add(doc.getUri());
					keys.add(doc.getDocumentKey());
				}
			} catch (Exception ex) {
				try {
					((TransactionManagementImpl) getTxManagement()).flushCurrentTx();
				} catch (BagriException e) {
					e.printStackTrace();
				}
				// log it..
				System.out.println(ex);
			}
			latch.countDown();
		}
		
	}
	
}
