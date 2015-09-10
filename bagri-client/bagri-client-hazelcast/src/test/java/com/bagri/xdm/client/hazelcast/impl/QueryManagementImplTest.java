package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.bagri.xdm.api.test.XDMQueryManagementTest;
import com.hazelcast.core.Hazelcast;

public class QueryManagementImplTest extends XDMQueryManagementTest {

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
		xRepo = new RepositoryImpl();

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
	
	
}
