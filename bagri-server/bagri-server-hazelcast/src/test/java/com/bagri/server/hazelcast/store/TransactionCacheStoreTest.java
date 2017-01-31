package com.bagri.server.hazelcast.store;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_TRANSACTION;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.TransactionIsolation;
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
import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.server.hazelcast.impl.TransactionManagementImpl;
import com.bagri.server.hazelcast.store.TransactionCacheStore;
import com.bagri.support.util.PropUtils;
import com.hazelcast.core.HazelcastInstance;

public class TransactionCacheStoreTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
    //private static String txFileName;
    private TransactionCacheStore txStore;
    private Properties tmProps;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("bdb.log.level", "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "store.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// wait for document store..?
		Thread.sleep(5000);
		//Assert.assertTrue("expected to delete tx log from " + txFileName, Files.deleteIfExists(Paths.get(txFileName)));
		// delete all stored securities?
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
			((TransactionManagementImpl) xdmRepo.getTxManagement()).adjustTxCounter(0);
			
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			
			PopulationManagementImpl pm = (PopulationManagementImpl) context.getBean(HazelcastInstance.class).getUserContext().get(ctx_popService);
			txStore = (TransactionCacheStore) pm.getMapStore(CN_XDM_TRANSACTION);
			
			tmProps = new Properties();
			tmProps.setProperty(pn_client_txTimeout, "50");
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		//SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		//Schema schema = xdmRepo.getSchema();
		//String dataPath = schema.getProperty(pn_schema_store_data_path);
		//String nodeNum = System.getProperty(pn_node_instance);
		//txFileName = FileUtils.buildStoreFileName(dataPath, nodeNum, "txlog");
	}

	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
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
		Map<String, Object> params = new HashMap<>();
		params.put(":sec", "/" + prefix + ":Security");
		return ((QueryManagement) getQueryManagement()).getContent(ec, ":sec", params);
	}
	
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
		int expCount = oldCount; // + (loops*(thCount/2)); why is new == old??
		assertTrue("expected " + expCount + " but got " + newCount + " transactions", newCount == expCount);
	}

	@Override
	protected Properties getDocumentProperties() {
		return tmProps;
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

			long txId = 0;
			try {
				//for (int i=0; i < loops; i++) {
				int i = 0;
				while (i < loops) {
					txId = xRepo.getTxManagement().beginTransaction(); //TransactionIsolation.repeatableRead);
					try {
						//storeSecurityTest();
						uris.add(updateDocumentTest("security1500.xml", sampleRoot + getFileName("security1500.xml")).getUri());
						uris.add(updateDocumentTest("security5621.xml", sampleRoot + getFileName("security5621.xml")).getUri());
						uris.add(updateDocumentTest("security9012.xml", sampleRoot + getFileName("security9012.xml")).getUri());
					} catch (BagriException ex) {
						if (ex.getErrorCode() == BagriException.ecTransTimeout) {
							xRepo.getTxManagement().rollbackTransaction(txId);
							Thread.sleep(100);
							continue;
						}
						throw ex;
					}
						
					Collection<String> sec = getSecurity("VFINX");
					assertNotNull("Got null VFINX", sec);
					assertTrue("Got empty VFINX", sec.size() > 0);
					sec = getSecurity("IBM");
					assertNotNull("Got null IBM", sec);
					assertTrue("Got empty IBM", sec.size() > 0);
					sec = getSecurity("PTTAX");
					assertNotNull("Got null PTTAX", sec);
					assertTrue("Got empty PTTAX", sec.size() > 0);
			
					if (rollback) {
						xRepo.getTxManagement().rollbackTransaction(txId);
					} else {
						xRepo.getTxManagement().commitTransaction(txId);
					}
					// wait till it is flushed to store
					Thread.sleep(2000);
					i++;
				}
			} catch (Throwable ex) {
				counter.countDown();
				try {
					xRepo.getTxManagement().rollbackTransaction(txId);
				} catch (BagriException e) {
					e.printStackTrace();
				}
				assertTrue("Unexpected exception: " + ex.getMessage(), false);
			}
			counter.countDown();
		}
		
	}

}
