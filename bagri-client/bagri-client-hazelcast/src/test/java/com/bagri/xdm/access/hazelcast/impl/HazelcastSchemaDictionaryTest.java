package com.bagri.xdm.access.hazelcast.impl;

import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_CACHE_MODE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_POOL_SIZE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SERVER_ADDRESS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_NAME;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_PASS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PV_MODE_CLIENT;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.xdm.access.test.XDMSchemaDictionaryTest;
import com.hazelcast.core.Hazelcast;

public class HazelcastSchemaDictionaryTest extends XDMSchemaDictionaryTest {
	
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

		registerSecuritySchemaTest();
		registerCustaccSchemaTest();
		//registerCommonSchemaTest();
	}

	@After
	public void tearDown() throws Exception {
		dMgr.close();
	}

}
