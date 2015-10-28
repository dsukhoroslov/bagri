package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_data_path;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.PropUtils;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.cache.hazelcast.impl.PopulationManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.cache.hazelcast.impl.TransactionManagementImpl;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.spi.ManagedService;

public class TransactionCacheStoreTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;
    private static String txFileName;
    private TransactionCacheStore txStore;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "info");
		System.setProperty("xdm.node.instance", "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "store.properties");
		context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// give it a time to perform write-behind task
		Thread.sleep(3000);
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
		if (dataPath == null) {
			dataPath = "";
		}
		String nodeNum = System.getProperty("xdm.node.instance");
		if (nodeNum == null) {
			nodeNum = "0";
		}
		txFileName = TransactionCacheStore.getTxLogFile(dataPath, nodeNum);
	}

	@Test
	public void bulkTransactionTest() throws Exception {
		
		int oldCount = txStore.getStoredCount();
		int count = 3;
		for (int i=1; i <= count; i++) {

			Thread th = new Thread(new TransactionTest(i % 2 == 0));
			th.start();
		}
		int newCount = txStore.getStoredCount();
		int expCount = oldCount + (count % 2);
		Assert.assertTrue("expected " + expCount + " but got " + newCount + " transactions", 
				newCount == expCount);
	}
	
	private class TransactionTest implements Runnable {

		private boolean rollback;
		
		TransactionTest(boolean rollback) {
			this.rollback = rollback;
		}
		
		@Override
		public void run() {

			try {
				long txId = xRepo.getTxManagement().beginTransaction();
				storeSecurityTest();
				
				// sleep for write-behind interval
				Thread.sleep(2000);
	
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
			} catch (Exception ex) {
				Assert.assertTrue("Unexpected exception: " + ex.getMessage(), false);
			}
			
		}
		
	}

}
