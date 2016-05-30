package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.common.config.XDMConfigConstants.xdm_node_instance;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_store_data_path;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.PropUtils;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.system.XDMSchema;

public class PopulationManagementImplTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;
    //private static String txFileName;
    private PopulationManagementImpl popManager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "info");
		System.setProperty(xdm_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "store.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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
			setContext(schema.getName(), schema_context, context);
			((TransactionManagementImpl) xdmRepo.getTxManagement()).adjustTxCounter();
    		popManager = (PopulationManagementImpl) xdmRepo.getHzInstance().getUserContext().get("popManager");
    		popManager.checkPopulation(1);
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
		String nodeNum = System.getProperty(xdm_node_instance);
		if (nodeNum == null) {
			nodeNum = "0";
		}
		//txFileName = TransactionCacheStore.getTxLogFile(dataPath, nodeNum);
	}


	@Test
	public void bulkPopulationTest() throws Exception {
		
		int oldCount = popManager.getDocumentCount();
		int loops = 10;
		int thCount = 5;
		final CountDownLatch cdl = new CountDownLatch(thCount);
		//for (int i=1; i <= thCount; i++) {
		//	Thread th = new Thread(new TransactionTest(i % 2 == 0, loops, cdl));
		//	Thread.sleep(10);
		//	th.start();
		//}
		//cdl.await();
		//int newCount = txStore.getStoredCount();
		//int expCount = oldCount + (loops*(thCount/2));
		//Assert.assertTrue("expected " + expCount + " but got " + newCount + " transactions", newCount == expCount);
	}
	
}
