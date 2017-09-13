package com.bagri.client.hazelcast.impl;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.test.DocumentManagementTest;
import com.hazelcast.core.Hazelcast;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.bagri.core.Constants.*;
import static org.junit.Assert.*;

public class DocumentManagementImplTest extends DocumentManagementTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//launcher = new ServerLauncher("second", null, srvDir);
		//launcher.startServer();

		System.setProperty(pn_schema_address, "localhost:10500"); 
		System.setProperty(pn_schema_name, "default");
		System.setProperty(pn_schema_user, "guest");
		System.setProperty(pn_schema_password, "password");
		sampleRoot = "../../etc/samples/tpox/";
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
	public void storeDocumentsTest() throws Exception {
		storeSecurityTest();
		storeOrderTest();
		storeCustomerTest();
	}

	@Test
	public void documentPropertiesTest() throws Exception {
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		uris.add(doc.getUri());
		assertEquals("guest", doc.getCreatedBy());
		assertEquals(1, doc.getSizeInFragments());
		//assertEquals(doc.getTxStart(), 1);
		assertEquals(0L, doc.getTxFinish());
		assertEquals(1, doc.getVersion());
	}
	
}
