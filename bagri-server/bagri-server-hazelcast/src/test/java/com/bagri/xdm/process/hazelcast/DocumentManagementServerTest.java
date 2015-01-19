package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.api.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_POOL_SIZE;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_NAME;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SCHEMA_PASS;
import static com.bagri.xdm.access.hazelcast.impl.HazelcastConfigProperties.PN_SERVER_ADDRESS;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMDocumentManagerTest;

public class DocumentManagementServerTest extends XDMDocumentManagerTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Hazelcast.shutdownAll();
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		dMgr = context.getBean(DocumentManagementServer.class);
		mDictionary = dMgr.getSchemaDictionary();
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

}
