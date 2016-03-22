package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.common.config.XDMConfigConstants.xdm_document_collections;
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

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.common.XDMDocumentId;
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
		Thread.sleep(3000);
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			xdmRepo.setSchema(schema);
			XDMCollection collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
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
	public void getCollectionDocumentsTest() throws Exception {
		props = new Properties();
		props.setProperty(xdm_document_collections, "CLN_Security");
		storeSecurityTest();
		Collection<XDMDocumentId> docIds = this.getDocManagement().getCollectionDocumentIds("CLN_Security");
		assertEquals(3, docIds.size());
		int cnt = 0;
		for (XDMDocumentId docId: docIds) {
			if (ids.contains(docId.getDocumentKey())) {
				cnt++;
			}
		}
		assertEquals(3, cnt);
		props = null;
	}

	@Test
	public void addDocumentsToCollectionTest() throws Exception {
		storeSecurityTest();
		for (Long docKey: ids) {
			XDMDocumentId docId = new XDMDocumentId(docKey);
			this.getDocManagement().addDocumentToCollections(docId, new String[] {"CLN_Security"});
		}
		Collection<XDMDocumentId> docIds = this.getDocManagement().getCollectionDocumentIds("CLN_Security");
		assertEquals(3, docIds.size());
		int cnt = 0;
		for (XDMDocumentId docId: docIds) {
			if (ids.contains(docId.getDocumentKey())) {
				cnt++;
			}
		}
		assertEquals(3, cnt);
	}

	@Test
	public void removeDocumentsFromCollectionTest() throws Exception {
		addDocumentsToCollectionTest();
		for (Long docKey: ids) {
			XDMDocumentId docId = new XDMDocumentId(docKey);
			this.getDocManagement().removeDocumentFromCollections(docId, new String[] {"CLN_Security"});
		}
		Collection<XDMDocumentId> docIds = this.getDocManagement().getCollectionDocumentIds("CLN_Security");
		assertEquals(0, docIds.size());
	}

	@Test
	public void removeCollectionDocumentsTest() throws Exception {
		addDocumentsToCollectionTest();
		assertEquals(3, ids.size());
		
		long txId = xRepo.getTxManagement().beginTransaction();
		int cnt = getDocManagement().removeCollectionDocuments("CLN_Security");
		xRepo.getTxManagement().commitTransaction(txId);
		
		Collection<XDMDocumentId> docIds = this.getDocManagement().getCollectionDocumentIds("CLN_Security");
		assertEquals(0, docIds.size());
	}


}
