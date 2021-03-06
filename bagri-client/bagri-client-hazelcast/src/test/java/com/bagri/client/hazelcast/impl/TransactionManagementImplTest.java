package com.bagri.client.hazelcast.impl;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.test.ClientQueryManagementTest;
import com.hazelcast.core.Hazelcast;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.bagri.core.Constants.*;

import javax.xml.xquery.XQItemAccessor;

public class TransactionManagementImplTest extends ClientQueryManagementTest {
	
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
	
			ResultCursor<XQItemAccessor> sec = getSecurity("VFINX");
			Assert.assertNotNull(sec);
			Assert.assertFalse(sec.isEmpty());
			sec.close();

			sec = getSecurity("IBM");
			Assert.assertNotNull(sec);
			Assert.assertFalse(sec.isEmpty());
			sec.close();

			sec = getSecurity("PTTAX");
			Assert.assertNotNull(sec);
			Assert.assertFalse(sec.isEmpty());
			sec.close();

			xRepo.getTxManagement().rollbackTransaction(txId);
			txId = 0;
			
			sec = getSecurity("VFINX");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.isEmpty());
			sec.close();

			sec = getSecurity("IBM");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.isEmpty());
			sec.close();

			sec = getSecurity("PTTAX");
			Assert.assertNotNull(sec);
			Assert.assertTrue(sec.isEmpty());
			sec.close();
		} finally {
			if (txId > 0) {
				xRepo.getTxManagement().rollbackTransaction(txId);
			}
		}
	}
	
}
