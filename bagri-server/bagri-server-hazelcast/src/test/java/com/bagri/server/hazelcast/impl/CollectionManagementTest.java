package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCollection;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.support.util.JMXUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class CollectionManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

    private Properties props;
    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/tpox/";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src/test/resources");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(SchemaRepositoryImpl.class);
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
			collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					2, "CLN_Custom", "", "custom", true);
			schema.addCollection(collection);
			xdmRepo.setSchema(schema);

			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient("client", "guest");
			xdmRepo.setClientId(client_id);
		}

		props = new Properties();
		props.setProperty(pn_client_id, client_id);
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI));
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
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments(null, props);
		assertEquals(0, ids.size());
		//props.setProperty(xdm_document_collections, "CLN_Security");
		storeSecurityTest();
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Security), txFinish = 0", props);
		assertEquals(4, ids.size());
		//int cnt = 0;
		//for (String id: ids) {
		//	if (ids.contains(id)) {
		//		cnt++;
		//	}
		//}
		//assertEquals(4, cnt);
	}

	@Test
	public void addCustomCollectionDocumentsTest() throws Exception {
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments(null, props);
		assertEquals(0, ids.size());
		props.setProperty(pn_document_collections, "CLN_Custom");
		assertEquals(0, uris.size());
		storeSecurityTest();
		assertEquals(4, uris.size());
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Security), txFinish = 0", props);
		assertEquals(0, ids.size());
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Custom), txFinish = 0", props);
		assertEquals(4, ids.size());
		int cnt = 0;
		for (DocumentAccessor doc: ids) {
			if (uris.contains(doc.getUri())) {
				cnt++;
			}
		}
		assertEquals(4, cnt);
	}

	@Test
	public void getCollectionDocumentsTest() throws Exception {
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments(null, props);
		assertEquals(0, ids.size());
		props.setProperty(pn_document_collections, "CLN_Security");
		storeSecurityTest();
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Security), txFinish = 0", props);
		assertEquals(4, ids.size());
		int cnt = 0;
		for (DocumentAccessor doc: ids) {
			if (uris.contains(doc.getUri())) {
				cnt++;
			}
		}
		assertEquals(4, cnt);
	}

	@Test
	public void limitCollectionDocumentsTest() throws Exception {
		addDocumentsToCollectionTest();
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Custom), txFinish = 0", props);
		assertEquals(4, ids.size());
		props.setProperty(pn_client_fetchSize, "2");
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Custom), txFinish = 0", props);
		assertEquals(2, ids.size());
	}

	@Test
	public void addDocumentsToCollectionTest() throws Exception {
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments(null, props);
		assertEquals(0, ids.size());
		assertEquals(0, uris.size());
		storeSecurityTest();
		assertEquals(4, uris.size());
		// docs in default collection
		ids = (ResultCollection) this.getDocManagement().getDocuments(null, props);
		assertEquals(4, ids.size());
		// the docs are already in CLN_Security collection 
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Security), txFinish = 0", props);
		assertEquals(4, ids.size());

		for (String uri: uris) {
			this.getDocManagement().addDocumentToCollections(uri, new String[] {"CLN_Custom"});
		}
		ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Custom), txFinish = 0", props);
		assertEquals(4, ids.size());
		int cnt = 0;
		for (DocumentAccessor doc: ids) {
			if (uris.contains(doc.getUri())) {
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
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Custom), txFinish = 0", props);
		assertEquals(0, ids.size());
	}

	@Test
	public void removeCollectionDocumentsTest() throws Exception {
		addDocumentsToCollectionTest();
		assertEquals(4, uris.size());
		
		long txId = xRepo.getTxManagement().beginTransaction();
		Iterable<DocumentAccessor> docs = getDocManagement().removeDocuments("collections.contains(CLN_Security)", props);
		xRepo.getTxManagement().commitTransaction(txId);
		int cnt = 0; 
		for (DocumentAccessor doc: docs) {
			cnt++;
		}
		assertEquals(4, cnt);
		
		ResultCollection ids = (ResultCollection) this.getDocManagement().getDocuments("collections.contains(CLN_Security), txFinish = 0", props);
		assertEquals(0, ids.size());
	}


}
