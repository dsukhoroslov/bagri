package com.bagri.server.hazelcast.predicate;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class DocumentPredicateTest {

	private static final int map_cln_id = 1;
	private static final int cluster_size = 3; 
	private static ClassPathXmlApplicationContext[] contexts = new ClassPathXmlApplicationContext[cluster_size];
	
	private SchemaRepositoryImpl[] repos = new SchemaRepositoryImpl[cluster_size];
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty(pn_log_level, "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src/test/resources");

		for (int i=0; i < cluster_size; i++) {
			System.setProperty(pn_node_instance, String.valueOf(i));
			contexts[i] = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for (int i=0; i < cluster_size; i++) {
			contexts[i].close();
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
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		//removeDocumentsTest();
	}

	@Test
	public void testHzCluster() throws Exception {
	    Properties props = new Properties();
		props.setProperty(pn_document_collections, "maps");
		props.setProperty(pn_document_data_format, "MAP");
		props.setProperty(pn_client_txLevel, pv_client_txLevel_skip);
		//long txId = xRepo.getTxManagement().beginTransaction();
		int cnt = 100;
		for (int i=0; i < cnt; i++) {
			Map<String, Object> m1 = new HashMap<>();
			m1.put("intProp", i); 
			m1.put("boolProp", i % 2 == 0);
			m1.put("strProp", "xyz" + 32*i);
			String uri = "map_test" + i;
			SchemaRepositoryImpl repo = getRepoForUri(uri);
			Document mDoc = repo.getDocumentManagement().storeDocumentFrom(uri, m1, props);
			assertNotNull(mDoc);
			//assertEquals(txId, mDoc.getTxStart());
			//uris.add(mDoc.getUri());
		}
		//xRepo.getTxManagement().commitTransaction(txId);
		
		HazelcastInstance hz = repos[0].getHzInstance();
		IMap<DocumentKey, Document> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		java.util.Collection<Document> maps = xddCache.values(new CollectionPredicate(map_cln_id));
		assertEquals(cnt, maps.size());
		
		//java.util.Collection<String> uris2 = xRepo.getDocumentManagement().getDocumentUris("uri >= map_test50", props);
		//assertEquals(54, uris2.size());
		
		//Iterable<?> results = xRepo.getDocumentManagement().getDocuments("uri >= map_test50", props);
		//assertEquals(uris2.size(), ((java.util.Collection) results).size());
	}
	
	private SchemaRepositoryImpl getRepoForUri(String uri) {
		Integer key = uri.hashCode();
		for (int i=0; i < cluster_size; i++) {
			SchemaRepositoryImpl repo = repos[i];
			HazelcastInstance hz = repo.getHzInstance();
			Member m = hz.getCluster().getLocalMember();
			if (hz.getPartitionService().getPartition(key).getOwner().getUuid().equals(m.getUuid())) {
				//System.out.println("idx: " + i + " for uri: " + uri);
				return repo;
			}
		}
		return null;
	}
	
}
