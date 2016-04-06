package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.pn_baseURI;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.ClientQueryManagementTest;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xquery.api.XQProcessor;

@Ignore
public class TpoxQueryTest extends ClientQueryManagementTest {

    private static ClassPathXmlApplicationContext context;
    
    private XQProcessor xqProc; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Thread.sleep(3000);
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		xqProc = context.getBean("xqProcessor", XQProcessor.class);
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			schema.setProperty(pn_baseURI, sampleRoot);
			xdmRepo.setSchema(schema);
			//XDMCollection collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
			//		1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			//schema.addCollection(collection);
			
			storeCustomerTest();
			storeOrderTest();
			storeSecurityTest();
		}
	}

	@After
	public void tearDown() throws Exception {
		// do not remove documents here!
		//removeDocumentsTest();
	}


	@Test
	public void getPriceTest() throws Exception {
		Iterator<?> sec = getPrice("VFINX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());

		sec = getPrice("IBM");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());

		sec = getPrice("PTTAX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void getSecurityTest() throws Exception {
		Iterator<?> sec = getSecurity("VFINX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());

		sec = getSecurity("IBM");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void searchSecurityTest() throws Exception {
		Iterator<?> sec = searchSecurity("Technology", 25, 28, 0);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());

		sec = searchSecurity("Technology", 25, 28, 1);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertFalse(sec.hasNext());

		sec = searchSecurity("Technology", 28, 29, 0);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertFalse(sec.hasNext());
	}

	@Test
	public void getOrderTest() throws Exception {
		Iterator<?> sec = getOrder("103404");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());

		sec = getOrder("103935");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void getCustomerTest() throws Exception {
		Iterator<?> sec = getCustomer("1011");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}
	
	@Test
	public void getCustomerProfileTest() throws Exception {
		Iterator<?> sec = getCustomerProfile("1011");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		Iterator<?> sec = getCustomerAccounts("1011");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}
	
	@Test
	public void getTodayOrderPriceTest() throws Exception {
		Iterator<?> sec = getTodayOrderPrice("103935");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void getOrderCustomersTest() throws Exception {
		Iterator<?> sec = getOrderCustomers(2000, "Portugal");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void getCustomerByAddressTest() throws Exception {
		Iterator<?> sec = getCustomerByAddress(56137, 1, true);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}
	
	@Test
	public void getMaxOrderPriceTest() throws Exception {
		Iterator<?> sec = getMaxOrderPrice(1011);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}

	@Test
	public void getMaxIndustryOrderTest() throws Exception {
		Iterator<?> sec = getMaxOrderForIndustry("ComputerHardware", "California");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}
	
	@Test
	public void getCustomerSecuritiesTest() throws Exception {
		Iterator<?> sec = getCustomerSecurities(1011);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
	}
	
}
