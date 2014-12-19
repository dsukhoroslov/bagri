package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_CACHE_MODE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_POOL_SIZE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_NAME;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_PASS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SERVER_ADDRESS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_CLIENT;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.xdm.access.test.XDMDocumentManagerTest;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;

public class HazelcastDocumentManagerTest extends XDMDocumentManagerTest {
	
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

		//storeSecurityTest();
		//storeCustomerTest();
		//storeOrderTest();
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		dMgr.close();
	}
	
	@Test
	@Ignore
	public void getDocumentsCacheTest() {
		IMap<Long, XDMDocument> cache = ((DocumentManagementClient) dMgr).getDocumentCache();
		Map<Long, XDMDocument> all = cache.getAll(cache.keySet());
		System.out.println("Documents Cache: " + all);
	}

	@Test
	@Ignore
	public void getElementsCacheTest() {
		IMap<XDMDataKey, XDMElements> cache = ((DocumentManagementClient) dMgr).getDataCache();
		Map<XDMDataKey, XDMElements> all = cache.getAll(cache.keySet());
		System.out.println("Elements Cache: " + all);
	}
	
	@Test
	public void storeDocumentsTest() throws IOException {
		storeSecurityTest();
		storeOrderTest();
		storeCustomerTest();
	}

}
