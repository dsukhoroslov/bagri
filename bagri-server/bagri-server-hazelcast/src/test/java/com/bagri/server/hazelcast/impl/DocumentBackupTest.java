package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.getBasicDataFormats;
import static com.bagri.core.test.TestUtils.loadProperties;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.server.api.CacheConstants;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class DocumentBackupTest {

	private static final int map_cln_id = 1;
	private static final int cluster_size = 2; 
	private static ClassPathXmlApplicationContext[] contexts = new ClassPathXmlApplicationContext[cluster_size];
	
	private SchemaRepositoryImpl[] repos = new SchemaRepositoryImpl[cluster_size];
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty(pn_log_level, "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "backup.properties");
		System.setProperty(pn_config_path, "src/test/resources");

		for (int i=0; i < cluster_size; i++) {
			System.setProperty(pn_node_instance, String.valueOf(i));
			contexts[i] = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for (int i=0; i < cluster_size; i++) {
			if (contexts[i] != null) {
				contexts[i].close();
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		Properties props = loadProperties("src/test/resources/test.properties");
		for (int i=0; i < cluster_size; i++) {
			repos[i] = contexts[i].getBean(SchemaRepositoryImpl.class);
			Schema schema = repos[i].getSchema();
			if (schema == null) {
				schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
				Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), map_cln_id, "maps", "", "custom", true);
				schema.addCollection(collection);
				repos[i].setSchema(schema);
				repos[i].setDataFormats(getBasicDataFormats());
				repos[i].setLibraries(new ArrayList<Library>());
				repos[i].setModules(new ArrayList<Module>());
				((ClientManagementImpl) repos[i].getClientManagement()).addClient("client", "guest");
				repos[i].setClientId("client");
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		//removeDocumentsTest();
	}


	@Test
	public void testDocumentBackup() throws Exception {
	    Properties props = new Properties();
	    props.setProperty(pn_client_id, "client");
		props.setProperty(pn_document_collections, "maps");
		props.setProperty(pn_document_data_format, "MAP");
		props.setProperty(pn_client_txLevel, pv_client_txLevel_skip);
		Map<String, Object> m1 = new HashMap<>();
		m1.put("intProp", 7); 
		m1.put("boolProp", 7 % 2 == 0);
		m1.put("strProp", "xyz" + 32*7);
		String uri = "map_test" + 7;
		int idx = getIndexForUri(uri);
		SchemaRepositoryImpl repo = repos[idx];
		DocumentAccessor mDoc = repo.getDocumentManagement().storeDocument(uri, m1, props);
		assertNotNull(mDoc);
		assertEquals(uri, mDoc.getUri());
		DocumentAccessor oDoc = repo.getDocumentManagement().getDocument(uri, props);
		assertEquals(mDoc.getUri(), oDoc.getUri());
		HazelcastInstance hz = repo.getHzInstance();
		IMap cCnts = hz.getMap(CacheConstants.CN_XDM_CONTENT);
		assertEquals(0, cCnts.getLocalMapStats().getBackupEntryCount());
		IMap cDocs = hz.getMap(CacheConstants.CN_XDM_DOCUMENT);
		assertEquals(0, cDocs.getLocalMapStats().getBackupEntryCount());
		IMap cElts = hz.getMap(CacheConstants.CN_XDM_ELEMENT);
		assertEquals(0, cElts.getLocalMapStats().getBackupEntryCount());
		
		int bdx = idx ^ 1;
		repo = repos[bdx];
		DocumentAccessor bDoc = repo.getDocumentManagement().getDocument(uri, props);
		assertNull(bDoc);
		Thread.sleep(10); // wait for stats a bit
		hz = repo.getHzInstance();
		cCnts = hz.getMap(CacheConstants.CN_XDM_CONTENT);
		assertEquals(1, cCnts.getLocalMapStats().getBackupEntryCount());
		cDocs = hz.getMap(CacheConstants.CN_XDM_DOCUMENT);
		assertEquals(1, cDocs.getLocalMapStats().getBackupEntryCount());
		cElts = hz.getMap(CacheConstants.CN_XDM_ELEMENT);
		assertEquals(3, cElts.getLocalMapStats().getBackupEntryCount());

		contexts[idx].close();
		contexts[idx] = null;

		bDoc = repo.getDocumentManagement().getDocument(uri, props);
		assertNotNull(bDoc);
	}

	private int getIndexForUri(String uri) {
		Integer key = uri.hashCode();
		for (int i=0; i < cluster_size; i++) {
			SchemaRepositoryImpl repo = repos[i];
			HazelcastInstance hz = repo.getHzInstance();
			Member m = hz.getCluster().getLocalMember();
			if (hz.getPartitionService().getPartition(key).getOwner().getUuid().equals(m.getUuid())) {
				//System.out.println("idx: " + i + " for uri: " + uri);
				return i;
			}
		}
		return -1;
	}
	

}
