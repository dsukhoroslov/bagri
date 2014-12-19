package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_CACHE_MODE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_POOL_SIZE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_NAME;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_PASS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SERVER_ADDRESS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_CLIENT;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_SERVER;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.bagri.xdm.access.test.XDMDocumentManagerTest;
import com.hazelcast.core.Hazelcast;

public class HazelcastDocumentManagerSrvTest extends XDMDocumentManagerTest {
	
	//private staic boolean
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("hazelcast.config", "hazelcast/hazelcast.xml");
		System.setProperty(PN_SERVER_ADDRESS, "localhost:10500");
		System.setProperty(PN_POOL_SIZE, "10");
		System.setProperty(PN_CACHE_MODE, PV_MODE_CLIENT);
		System.setProperty(PN_SCHEMA_NAME, "TPoX2");
		System.setProperty(PN_SCHEMA_PASS, "TPoX2");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Hazelcast.shutdownAll();
	}

	@Before
	public void setUp() throws Exception {
		dMgr = new DocumentManagementClient();
		mDictionary = dMgr.getSchemaDictionary();

		storeSecurityTest();
		storeCustomerTest();
		storeOrderTest();
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		//Hazelcast.shutdownAll();
	}

}
