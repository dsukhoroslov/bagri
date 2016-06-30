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
import com.bagri.xdm.api.test.ServerLauncher;
import com.hazelcast.core.Hazelcast;

public class TransactionManagementImplTest extends ClientQueryManagementTest {

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
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		xRepo.close();
	}
	
	@Test
	public void rollbackTransactionTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		try {
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
	
			xRepo.getTxManagement().rollbackTransaction(txId);
			txId = 0;
			
			sec = getSecurity("VFINX");
			Assert.assertNotNull(sec);
			Assert.assertFalse(sec.hasNext());
	
			sec = getSecurity("IBM");
			Assert.assertNotNull(sec);
			Assert.assertFalse(sec.hasNext());
	
			sec = getSecurity("PTTAX");
			Assert.assertNotNull(sec);
			Assert.assertFalse(sec.hasNext());
		} finally {
			if (txId > 0) {
				xRepo.getTxManagement().rollbackTransaction(txId);
			}
		}
	}
	
}
