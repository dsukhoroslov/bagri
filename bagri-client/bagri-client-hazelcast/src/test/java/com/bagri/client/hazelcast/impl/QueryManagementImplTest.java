package com.bagri.client.hazelcast.impl;

import static com.bagri.core.Constants.pn_schema_address;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_password;
import static com.bagri.core.Constants.pn_schema_user;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.test.ClientQueryManagementTest;
import com.hazelcast.core.Hazelcast;

public class QueryManagementImplTest extends ClientQueryManagementTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//launcher = new ServerLauncher("second", null, srvDir);
		//launcher.startServer();

		System.setProperty(pn_schema_address, "localhost:10500"); 
		System.setProperty(pn_schema_name, "default");
		System.setProperty(pn_schema_user, "guest");
		System.setProperty(pn_schema_password, "password");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Hazelcast.shutdownAll();
		//launcher.stopServer();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = new SchemaRepositoryImpl();

		//storeSecurityTest();
		//storeCustomerTest();
		//storeOrderTest();
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		xRepo.close();
	}
	
	@Test
	public void getPriceTest() throws Exception {
		storeSecurityTest();

		ResultCursor sec = getPrice("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();

		sec = getPrice("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();

		sec = getPrice("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();
	}

	@Test
	public void getSecurityTest() throws Exception {
		storeSecurityTest();

		ResultCursor sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();
	}

	@Test
	public void searchSecurityTest() throws Exception {
		storeSecurityTest();

		ResultCursor sec = searchSecurity("Technology", 25, 28, 0);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();

		sec = searchSecurity("Technology", 25, 28, 1);
		Assert.assertNotNull(sec);
		Assert.assertFalse(sec.next());
		sec.close();

		sec = searchSecurity("Technology", 28, 29, 0);
		Assert.assertNotNull(sec);
		Assert.assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getOrderTest() throws Exception {
		storeOrderTest();
		ResultCursor sec = getOrder("103404");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();

		sec = getOrder("103935");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();
	}

	@Test
	public void getCustomerProfileTest() throws Exception {
		storeCustomerTest();
		ResultCursor sec = getCustomerProfile("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		storeCustomerTest();
		ResultCursor sec = getCustomerAccounts("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.next());
		sec.close();
	}
	
	
}
