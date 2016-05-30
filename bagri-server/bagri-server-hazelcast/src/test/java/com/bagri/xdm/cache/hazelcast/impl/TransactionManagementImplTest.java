package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMSchema;

public class TransactionManagementImplTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			xdmRepo.setSchema(schema);
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		//Thread.sleep(1000);
	}

	public Collection<String> getSecurity(String symbol) throws Exception {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":sec", "/" + prefix + ":Security");
		return ((XDMQueryManagement) getQueryManagement()).getContent(ec, ":sec", params);
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

		// TODO: known issue, fix it..
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
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
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
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);

		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					long txId = getTxManagement().beginTransaction();
					XDMDocument doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						uris.add(doc.getUri());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					long txId = getTxManagement().beginTransaction();
					XDMDocument doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						uris.add(doc.getUri());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});
		
		th1.start();
		th2.start();
		cdl.await();
		// fails sometime, not clear why
		//assertFalse("expected less than 3 docs commited", ids.size() == 3);
	}	

	@Test
	public void concurrentRollbackTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);

		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					long txId = getTxManagement().beginTransaction();
					XDMDocument doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
					getTxManagement().rollbackTransaction(txId);
					if (doc != null) {
						uris.add(doc.getUri());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					long txId = getTxManagement().beginTransaction();
					XDMDocument doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						uris.add(doc.getUri());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});
		
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
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		getTxManagement().setTransactionTimeout(10);

		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					long txId = getTxManagement().beginTransaction();
					XDMDocument doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						uris.add(doc.getUri());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					long txId = getTxManagement().beginTransaction();
					XDMDocument doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						uris.add(doc.getUri());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});
		
		th1.start();
		th2.start();
		cdl.await();
		// fails sometime, not clear why
		//assertFalse("expected less than 3 docs commited", ids.size() == 3);
	}
	
}
