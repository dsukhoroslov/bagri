package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.xdm.api.test.ServerLauncher;
import com.bagri.xdm.api.test.XDMDocumentManagementTest;
import com.hazelcast.core.Hazelcast;

public class DocumentManagementImplTest extends XDMDocumentManagementTest {
	
	private static ServerLauncher launcher;
	private static final String srvDir = "C:\\Work\\Bagri\\git\\bagri\\bagri-server\\bagri-server-hazelcast";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		launcher = new ServerLauncher("second", null, srvDir);
		launcher.startServer();

		System.setProperty(pn_server_address, "localhost:10500"); 
		System.setProperty(pn_schema_name, "default");
		System.setProperty(pn_schema_user, "test");
		System.setProperty(pn_schema_password, "password");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Hazelcast.shutdownAll();
		launcher.stopServer();
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
	
	@Test
	public void storeDocumentsTest() throws Exception {
		storeSecurityTest();
		storeOrderTest();
		storeCustomerTest();
	}

}
