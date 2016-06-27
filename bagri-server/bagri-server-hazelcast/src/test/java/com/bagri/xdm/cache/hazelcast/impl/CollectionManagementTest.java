package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.xdm_config_path;
import static com.bagri.xdm.common.XDMConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.xdm_document_collections;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMSchema;

public class CollectionManagementTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;

    private Properties props = null;
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			XDMCollection collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
			collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
					2, "CLN_Custom", "", "custom", true);
			schema.addCollection(collection);
			xdmRepo.setSchema(schema);
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
	}

	@Override
	protected Properties getDocumentProperties() {
		return props;
	}
	
	@Test
	public void addDefaultCollectionDocumentsTest() throws Exception {
		Collection<String> ids = this.getDocManagement().getCollectionDocumentUris(null);
		assertEquals(0, ids.size());
		props = new Properties();
		//props.setProperty(xdm_document_collections, "CLN_Security");
		storeSecurityTest();
		ids = this.getDocManagement().getCollectionDocumentUris("CLN_Security");
		assertEquals(4, ids.size());
		int cnt = 0;
		for (String id: ids) {
			if (ids.contains(id)) {
				cnt++;
			}
		}
		assertEquals(4, cnt);
		props = null;
	}

	@Test
	public void addCustomCollectionDocumentsTest() throws Exception {
		Collection<String> ids = this.getDocManagement().getCollectionDocumentUris(null);
		assertEquals(0, ids.size());
		props = new Properties();
		props.setProperty(xdm_document_collections, "CLN_Custom");
		assertEquals(0, uris.size());
		storeSecurityTest();
		assertEquals(4, uris.size());
		ids = this.getDocManagement().getCollectionDocumentUris("CLN_Security");
		assertEquals(0, ids.size());
		ids = this.getDocManagement().getCollectionDocumentUris("CLN_Custom");
		assertEquals(4, ids.size());
		int cnt = 0;
		for (String id: ids) {
			if (uris.contains(id)) {
				cnt++;
			}
		}
		assertEquals(4, cnt);
		props = null;
	}

	@Test
	public void getCollectionDocumentsTest() throws Exception {
		Collection<String> ids = this.getDocManagement().getCollectionDocumentUris(null);
		assertEquals(0, ids.size());
		props = new Properties();
		props.setProperty(xdm_document_collections, "CLN_Security");
		storeSecurityTest();
		ids = this.getDocManagement().getCollectionDocumentUris("CLN_Security");
		assertEquals(4, ids.size());
		int cnt = 0;
		for (String id: ids) {
			if (uris.contains(id)) {
				cnt++;
			}
		}
		assertEquals(4, cnt);
		props = null;
	}

	@Test
	public void addDocumentsToCollectionTest() throws Exception {
		Collection<String> ids = this.getDocManagement().getCollectionDocumentUris(null);
		assertEquals(0, ids.size());
		assertEquals(0, uris.size());
		storeSecurityTest();
		assertEquals(4, uris.size());
		// docs in default collection
		ids = this.getDocManagement().getCollectionDocumentUris(null);
		assertEquals(4, ids.size());
		// the docs are already in CLN_Security collection 
		ids = this.getDocManagement().getCollectionDocumentUris("CLN_Security");
		assertEquals(4, ids.size());

		for (String uri: uris) {
			this.getDocManagement().addDocumentToCollections(uri, new String[] {"CLN_Custom"});
		}
		ids = this.getDocManagement().getCollectionDocumentUris("CLN_Custom");
		assertEquals(4, ids.size());
		int cnt = 0;
		for (String id: ids) {
			if (uris.contains(id)) {
				cnt++;
			}
		}
		assertEquals(4, cnt);
	}

	@Test
	public void removeDocumentsFromCollectionTest() throws Exception {
		addDocumentsToCollectionTest();
		for (String uri: uris) {
			this.getDocManagement().removeDocumentFromCollections(uri, new String[] {"CLN_Custom"});
		}
		Collection<String> ids = this.getDocManagement().getCollectionDocumentUris("CLN_Custom");
		assertEquals(0, ids.size());
	}

	@Test
	public void removeCollectionDocumentsTest() throws Exception {
		addDocumentsToCollectionTest();
		assertEquals(4, uris.size());
		
		long txId = xRepo.getTxManagement().beginTransaction();
		int cnt = getDocManagement().removeCollectionDocuments("CLN_Security");
		xRepo.getTxManagement().commitTransaction(txId);
		
		Collection<String> ids = this.getDocManagement().getCollectionDocumentUris("CLN_Security");
		assertEquals(0, ids.size());
	}


}
