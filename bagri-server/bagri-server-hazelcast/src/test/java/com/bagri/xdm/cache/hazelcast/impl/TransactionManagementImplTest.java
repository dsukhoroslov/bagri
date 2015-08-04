package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMPath;

public class TransactionManagementImplTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Hazelcast.shutdownAll();
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
	}

	@Test
	public void rollbackTransactionTest() throws IOException {
		long txId = xRepo.getTxManagement().beginTransaction();
		storeSecurityTest();

		Collection<String> sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);

		xRepo.getTxManagement().rollbackTransaction(txId);
		
		sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);
	}

	@Test
	public void concurrentUpdateTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		Assert.assertNotNull(doc);
		Assert.assertTrue(doc.getTxStart() == txId);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);

		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				long txId = getTxManagement().beginTransaction();
				XDMDocument doc = null;
				try {
					doc = updateDocumentTest(0, uri, sampleRoot + getFileName("security5621.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						ids.add(doc.getDocumentKey());
					}
				} catch (IOException ex) {
				}
				cdl.countDown();
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				long txId = getTxManagement().beginTransaction();
				XDMDocument doc = null;
				try {
					doc = updateDocumentTest(0, uri, sampleRoot + getFileName("security9012.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						ids.add(doc.getDocumentKey());
					}
				} catch (IOException ex) {
				}
				cdl.countDown();
			}
		});
		
		th1.start();
		th2.start();
		cdl.await();
		Assert.assertFalse("expected less than 3 docs commited", ids.size() == 3);
	}	

	@Test
	public void timeoutTransactionTest() throws Exception {
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		Assert.assertNotNull(doc);
		Assert.assertTrue(doc.getTxStart() == txId);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
		final String uri = doc.getUri();
		final CountDownLatch cdl = new CountDownLatch(2);
		getTxManagement().setTransactionTimeout(10);

		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				long txId = getTxManagement().beginTransaction();
				XDMDocument doc = null;
				try {
					doc = updateDocumentTest(0, uri, sampleRoot + getFileName("security5621.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						ids.add(doc.getDocumentKey());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				
				long txId = getTxManagement().beginTransaction();
				XDMDocument doc = null;
				try {
					doc = updateDocumentTest(0, uri, sampleRoot + getFileName("security9012.xml"));
					getTxManagement().commitTransaction(txId);
					if (doc != null) {
						ids.add(doc.getDocumentKey());
					}
				} catch (Exception ex) {
				}
				cdl.countDown();
			}
		});
		
		th1.start();
		th2.start();
		cdl.await();
		Assert.assertFalse("expected less than 3 docs commited", ids.size() == 3);
	}
	
}
