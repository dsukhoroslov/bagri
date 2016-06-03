package com.bagri.xdm.access.coherence.impl;

import static com.bagri.xdm.cache.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.cache.api.XDMCacheConstants.CN_XDM_ELEMENT;
import static com.bagri.xdm.cache.api.XDMCacheConstants.CN_XDM_NAMESPACE_DICT;
import static com.bagri.xdm.cache.api.XDMCacheConstants.CN_XDM_PATH_DICT;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.bagri.xdm.api.test.XDMDocumentManagementTest;
import com.tangosol.net.CacheFactory;

public class DocumentManagementClientTest extends XDMDocumentManagementTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("tangosol.coherence.distributed.localstorage", "true");
		//System.setProperty("tangosol.pof.enabled", "true");
		//System.setProperty("tangosol.coherence.cluster", "XDMCacheCluster");
        //System.setProperty("tangosol.coherence.role", "TestCacheServer");
		//System.setProperty("tangosol.coherence.cacheconfig", "coherence/xdm-test-cache-config.xml");
		System.setProperty("tangosol.coherence.cacheconfig", "coherence/xdm-client-cache-config.xml");
		System.setProperty("tangosol.coherence.proxy.address", "localhost");
		System.setProperty("tangosol.coherence.proxy.port", "21000");

		//context = new ClassPathXmlApplicationContext("spring/coh-client-context.xml");

		System.out.println("about to start cluster");
		//CacheFactory.ensureCluster();
		CacheFactory.getCache(CN_XDM_DOCUMENT);
		CacheFactory.getCache(CN_XDM_ELEMENT);
		CacheFactory.getCache(CN_XDM_NAMESPACE_DICT);
		CacheFactory.getCache(CN_XDM_PATH_DICT);
		System.out.println("cluster started");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("about to shutdown cluster");
		CacheFactory.shutdown();
		System.out.println("cluster shut down");
	}
	
	@Before
	public void setUp() throws Exception {
		//dMgr = new DocumentManagementClient();
		//mDictionary = dMgr.getSchemaDictionary();
		
		storeSecurityTest();
		storeCustomerTest();
		storeOrderTest();
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
	}

	
}
