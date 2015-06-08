package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.hazelcast.impl.RepositoryImpl.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.xdm.api.test.XDMDocumentManagementTest;
import com.hazelcast.core.Hazelcast;

public class DocumentManagementImplTest extends XDMDocumentManagementTest {
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("hazelcast.config", "hazelcast/hazelcast.xml");
		System.setProperty(PN_SERVER_ADDRESS, "localhost:10500"); 
		System.setProperty(PN_POOL_SIZE, "10");
		System.setProperty(PN_SCHEMA_NAME, "default");
		System.setProperty(PN_SCHEMA_PASS, "password");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Hazelcast.shutdownAll();
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
	public void storeDocumentsTest() throws IOException {
		storeSecurityTest();
		storeOrderTest();
		storeCustomerTest();
	}

}
