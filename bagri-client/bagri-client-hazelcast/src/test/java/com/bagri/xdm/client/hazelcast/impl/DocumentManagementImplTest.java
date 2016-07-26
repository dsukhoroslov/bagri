package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.common.Constants.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.xdm.api.test.ServerLauncher;
import com.bagri.xdm.api.test.DocumentManagementTest;
import com.bagri.xdm.domain.Document;
import com.hazelcast.core.Hazelcast;

public class DocumentManagementImplTest extends DocumentManagementTest {
	
	private static ServerLauncher launcher;
	private static final String srvDir = "C:\\Work\\Bagri\\git\\bagri\\bagri-server\\bagri-server-hazelcast";
	
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
	public void storeDocumentsTest() throws Exception {
		storeSecurityTest();
		storeOrderTest();
		storeCustomerTest();
	}

	@Test
	public void documentPropertiesTest() throws Exception {
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		uris.add(doc.getUri());
		assertEquals(doc.getCreatedBy(), "guest");
		assertEquals(doc.getFragments().length, 1);
		//assertEquals(doc.getTxStart(), 1);
		assertEquals(doc.getTxFinish(), 0);
		assertEquals(doc.getVersion(), 1);
	}
	
}
