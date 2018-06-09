package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class TransactionManagementImplTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/tpox/";
		//System.setProperty(pn_log_level, "trace");
		//System.setProperty("hz.log.level", "trace");
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
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		//Thread.sleep(1000);
	}

	public Collection<Long> getSecurity(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	@Test
	public void rollbackTransactionTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		storeSecurityTest();

		Collection<Long> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);

		sec = getSecurity("IBM");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
		
		assertEquals(user_name, ((TransactionManagementImpl) xRepo.getTxManagement()).getCurrentTransaction().getStartedBy());

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
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
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
		
		Collection<Long> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 0 but got " + sec.size() + " test documents", sec.size() == 0);
	}

	@Test
	public void concurrentUpdateTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		List<Long> keys = new CopyOnWriteArrayList<>();
		keys.add(((DocumentAccessorImpl) doc).getDocumentKey());

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
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		List<Long> keys = new CopyOnWriteArrayList<>();
		keys.add(((DocumentAccessorImpl) doc).getDocumentKey());

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
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
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
		assertFalse("expected less than 3 docs commited", keys.size() == 3);
	}	

	@Test
	public void timeoutTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
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
		assertFalse("expected less than 3 docs commited", keys.size() == 3);
	}
	
	@Test
	public void noTransactionTest() throws Exception {
		String doc1 = "<product id=\"product-1\"><type>product</type><name>Pokemon Red</name><price>29.99</price></product>";
		Properties props = getDocumentProperties();
		props.setProperty(pn_document_data_format, "XML");
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_FULL_DOCUMENT));
		props.setProperty(pn_client_txLevel, pv_client_txLevel_skip);
		DocumentAccessor da = getDocManagement().storeDocument("product.xml", doc1, props);
		uris.add(da.getUri());
		assertEquals(TransactionManagement.TX_NO, da.getTxStart());
		assertEquals(TransactionManagement.TX_NO, da.getTxFinish());
		assertEquals(1, da.getVersion());
		assertEquals(doc1, da.getContent());
		
		String doc2 = "<product id=\"product-2\"><type>product</type><name>Pokemon Black</name><price>19.01</price></product>";
		DocumentAccessor da2 = getDocManagement().storeDocument("product.xml", doc2, props);
		uris.add(da2.getUri());
		assertEquals(TransactionManagement.TX_NO, da2.getTxStart());
		assertEquals(TransactionManagement.TX_NO, da2.getTxFinish());
		assertEquals(1, da2.getVersion());
		assertEquals(doc2, da2.getContent());
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
				((SchemaRepositoryImpl) xRepo).setClientId(client_id);
				long txId = getTxManagement().beginTransaction(tiLevel);
				DocumentAccessor doc = updateDocumentTest(uri, sampleRoot + getFileName(uri2));
				getTxManagement().commitTransaction(txId);
				if (doc != null) {
					uris.add(doc.getUri());
					keys.add(((DocumentAccessorImpl) doc).getDocumentKey());
				}
			} catch (Exception ex) {
				try {
					((TransactionManagementImpl) getTxManagement()).finishCurrentTransaction(true);
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
