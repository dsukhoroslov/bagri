package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;

import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.xdm.api.test.ClientQueryManagementTest;
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

		Iterator<?> sec = getPrice("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());

		sec = getPrice("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());

		sec = getPrice("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());
	}

	@Test
	public void getSecurityTest() throws Exception {
		storeSecurityTest();

		Iterator<?> sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());
	}

	@Test
	public void searchSecurityTest() throws Exception {
		storeSecurityTest();

		Iterator<?> sec = searchSecurity("Technology", 25, 28, 0);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());

		sec = searchSecurity("Technology", 25, 28, 1);
		Assert.assertNotNull(sec);
		Assert.assertFalse(sec.hasNext());

		sec = searchSecurity("Technology", 28, 29, 0);
		Assert.assertNotNull(sec);
		Assert.assertFalse(sec.hasNext());
	}

	@Test
	public void getOrderTest() throws Exception {
		storeOrderTest();
		Iterator<?> sec = getOrder("103404");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());
		sec = getOrder("103935");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());
	}

	@Test
	public void getCustomerProfileTest() throws Exception {
		storeCustomerTest();
		Iterator<?> sec = getCustomerProfile("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		storeCustomerTest();
		Iterator<?> sec = getCustomerAccounts("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.hasNext());
	}
	
	
}
