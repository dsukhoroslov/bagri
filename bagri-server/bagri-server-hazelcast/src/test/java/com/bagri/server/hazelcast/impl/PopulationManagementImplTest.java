package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.PropUtils;

public class PopulationManagementImplTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
    //private static String txFileName;
    private PopulationManagementImpl popManager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		//System.setProperty(pn_log_level, "info");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "store.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Assert.assertTrue("expected to delete tx log from " + txFileName, Files.deleteIfExists(Paths.get(txFileName)));
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(SchemaRepositoryImpl.class);
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			Properties props = PropUtils.propsFromFile("src/test/resources/store.properties");
			schema.setProperties(props);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			
			//XDMDataStore ds = new XDMDataStore(1, new java.util.Date(), "", "JSON", null, null, null,
			//		"com.bagri.core.df.json.JsonApiParser", "com.bagri.core.df.json.JsonBuilder", true, null);
			//ArrayList<XDMDataStore> cStores = new ArrayList<>(1);
			//cStores.add(ds);
			//xdmRepo.setDataStores(cStores);
			
			setContext(schema.getName(), context);
			//((TransactionManagementImpl) xdmRepo.getTxManagement()).adjustTxCounter(0);
    		popManager = (PopulationManagementImpl) xdmRepo.getHzInstance().getUserContext().get(ctx_popService);
    		popManager.checkPopulation(1);
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		String dataPath = schema.getProperty(pn_schema_store_data_path);
		if (dataPath == null) {
			dataPath = "";
		}
		String nodeNum = System.getProperty(pn_node_instance);
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
