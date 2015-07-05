package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xqj.BagriXQConstants.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.hazelcast.core.Hazelcast;

public class TransactionManagementImplTest extends XDMManagementTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("hazelcast.config", "hazelcast/hazelcast.xml");
		System.setProperty(pn_server_address, "localhost:10500"); 
		//System.setProperty(PN_POOL_SIZE, "10");
		System.setProperty(pn_schema_name, "default"); // admin/admin ??
		System.setProperty(pn_schema_password, "password");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Hazelcast.shutdownAll();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = new RepositoryImpl();
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		xRepo.close();
	}
	
	@Test
	public void rollbackTransactionTest() throws IOException {
		long txId = xRepo.getTxManagement().beginTransaction();
		try {
			storeSecurityTest();
	
			Collection<String> sec = getSecurity("VFINX");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.size() > 0);
	
			sec = getSecurity("IBM");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.size() > 0);
	
			sec = getSecurity("PTTAX");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.size() > 0);
	
			xRepo.getTxManagement().rollbackTransaction(txId);
			txId = 0;
			
			sec = getSecurity("VFINX");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.size() == 0);
	
			sec = getSecurity("IBM");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.size() == 0);
	
			sec = getSecurity("PTTAX");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.size() == 0);
		} finally {
			if (txId > 0) {
				xRepo.getTxManagement().rollbackTransaction(txId);
			}
		}
	}
	
}
