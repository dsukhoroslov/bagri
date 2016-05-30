package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_data_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_node_instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;
import com.bagri.common.util.FileUtils;
import com.bagri.common.util.PropUtils;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.cache.hazelcast.impl.TransactionManagementImpl;
import com.bagri.xdm.system.XDMSchema;

public class TransactionCacheStoreTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;
    private static String txFileName;
    private TransactionCacheStore txStore;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "info");
		System.setProperty(xdm_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "store.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Thread.sleep(3000);
		//Assert.assertTrue("expected to delete tx log from " + txFileName, Files.deleteIfExists(Paths.get(txFileName)));
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			Properties props = PropUtils.propsFromFile("src/test/resources/store.properties");
			schema.setProperties(props);
			xdmRepo.setSchema(schema);
			((TransactionManagementImpl) xdmRepo.getTxManagement()).adjustTxCounter();
			//PopulationManagementImpl pm = context.getBean(PopulationManagementImpl.class);
			//ManagedService svc = pm.getHzService(MapService.SERVICE_NAME, "xdm-transaction");
			txStore = TransactionCacheStore.instance;
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		XDMSchema schema = xdmRepo.getSchema();
		String dataPath = schema.getProperty(xdm_schema_store_data_path);
		String nodeNum = System.getProperty(xdm_node_instance);
		txFileName = FileUtils.buildStoreFileName(dataPath, nodeNum, "txlog");
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
	
	@Ignore
	@Test
	public void bulkTransactionTest() throws Exception {
		
		int oldCount = txStore.getStoredCount();
		int loops = 10;
		int thCount = 5;
		final CountDownLatch cdl = new CountDownLatch(thCount);
		for (int i=1; i <= thCount; i++) {
			Thread th = new Thread(new TransactionTest(i % 2 == 0, loops, cdl));
			Thread.sleep(10);
			th.start();
		}
		cdl.await();
		int newCount = txStore.getStoredCount();
		int expCount = oldCount; // + (loops*(thCount/2));
		Assert.assertTrue("expected " + expCount + " but got " + newCount + " transactions", newCount == expCount);
	}
	

	private class TransactionTest implements Runnable {

		private int loops;
		private boolean rollback;
		private CountDownLatch counter;
		
		TransactionTest(boolean rollback, int loops, CountDownLatch counter) {
			this.rollback = rollback;
			this.counter = counter;
			this.loops = loops;
		}
		
		@Override
		public void run() {

			try {
				for (int i=0; i < loops; i++) {
					long txId = xRepo.getTxManagement().beginTransaction();
					//storeSecurityTest();
					uris.add(updateDocumentTest("security1500.xml", sampleRoot + getFileName("security1500.xml")).getUri());
					uris.add(updateDocumentTest("security5621.xml", sampleRoot + getFileName("security5621.xml")).getUri());
					uris.add(updateDocumentTest("security9012.xml", sampleRoot + getFileName("security9012.xml")).getUri());
					
					Collection<String> sec = getSecurity("VFINX");
					Assert.assertNotNull(sec);
					Assert.assertTrue(sec.size() > 0);
		
					sec = getSecurity("IBM");
					Assert.assertNotNull(sec);
					Assert.assertTrue(sec.size() > 0);
		
					sec = getSecurity("PTTAX");
					Assert.assertNotNull(sec);
					Assert.assertTrue(sec.size() > 0);
		
					if (rollback) {
						xRepo.getTxManagement().rollbackTransaction(txId);
					} else {
						xRepo.getTxManagement().commitTransaction(txId);
					}
					// wait till it is flushed to store
					Thread.sleep(2000);
				}
			} catch (Exception ex) {
				counter.countDown();
				Assert.assertTrue("Unexpected exception: " + ex.getMessage(), false);
			}
			counter.countDown();
		}
		
	}

}
