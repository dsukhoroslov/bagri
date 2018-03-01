package com.bagri.server.hazelcast.impl;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.Document;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.DocumentManagementTest;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.test.TestUtils.*;
import static com.bagri.support.util.FileUtils.*;
import static org.junit.Assert.*;

public class DocumentManagementImplTest extends DocumentManagementTest {

    private static ClassPathXmlApplicationContext context;

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
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void queryDocumentsTest() throws Exception {

		Schema schema = ((SchemaRepositoryImpl) xRepo).getSchema();
		Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 1, "products", "", "all products", true);
		schema.addCollection(collection);
		collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 2, "orders", "", "all orders", true);
		schema.addCollection(collection);

		String doc1 = "<product id=\"product-1\"><type>product</type><name>Pokemon Red</name><price>29.99</price></product>";
		String doc2 = "<order id=\"order-1\"><type>order</type><products><product product_id=\"product-1\"><quantity>2</quantity></product></products></order>";
		long txId = getTxManagement().beginTransaction();
		Properties props = getDocumentProperties();
		props.setProperty(pn_document_data_format, "XML");
		props.setProperty(pn_document_collections, "products");
		uris.add(getDocManagement().storeDocument("product.xml", doc1, props).getUri());
		props.setProperty(pn_document_collections, "orders");
		uris.add(getDocManagement().storeDocument("order.xml", doc2, props).getUri());
		getTxManagement().commitTransaction(txId);

		String query =
				"for $ord in fn:collection(\"orders\")/order\n" +
				"for $pro in fn:collection(\"products\")/product[@id=$ord/products/product/@product_id]\n" +
				"return <order id=\"{$ord/@id}\">\n" +
				    "    {$ord/type}\n" +
					"    {$pro}\n" +
					"    {$ord/products/product/quantity}\n" +
					"</order>";

		checkCursorResult(query, null, null, null);
	}

	@Test
	public void queryDocumentUrisTest() throws Exception {
		storeSecurityTest();
		storeOrderTest();
		DocumentManagementImpl dMgr = (DocumentManagementImpl) this.getDocManagement();
		Properties props = getDocumentProperties();
		ResultCursor<DocumentAccessor> docs = dMgr.getDocuments("uri like security16%, txFinish = 0", props);
		assertEquals(4, docs.size());
		docs = dMgr.getDocuments("uri like order%, txFinish = 0", props);
		assertEquals(2, docs.size());
		docs = dMgr.getDocuments("createdBy = guest, txFinish = 0", props);
		assertEquals(6, uris.size());
		docs = dMgr.getDocuments("uri > security16, txFinish = 0", props);
		assertEquals(3, docs.size());
	}
	
	@Test
	public void storeMultipleDocumentsTest() throws Exception {
		Map<String, Object> docs = new HashMap<>();
		docs.put("security1500.xml", readTextFile(sampleRoot + "security1500.xml"));
		docs.put("security5621.xml", readTextFile(sampleRoot + "security5621.xml"));
		docs.put("security9012.xml", readTextFile(sampleRoot + "security9012.xml"));
		docs.put("security29674.xml", readTextFile(sampleRoot + "security29674.xml"));
		Properties props = getDocumentProperties();
		props.setProperty(pn_client_txLevel, pv_client_txLevel_skip);
		ResultCursor<DocumentAccessor> results = getDocManagement().storeDocuments(docs, props);
		assertEquals(4, results.size());
	}
	
	@Test
	public void evictDocumentTest() throws Exception {
		storeSecurityTest();
		HazelcastInstance hz = ((SchemaRepositoryImpl) xRepo).getHzInstance();
		IMap<DocumentKey, Document> chDocs = hz.getMap(CN_XDM_DOCUMENT);
		assertEquals(4,  chDocs.size());
		IMap<DocumentKey, Document> chCont = hz.getMap(CN_XDM_CONTENT);
		assertEquals(4,  chCont.size());
		IMap<DocumentKey, Document> chElts = hz.getMap(CN_XDM_ELEMENT);
		int eSize = chElts.size();
		int cnt = chCont.size();
		for (Iterator<Map.Entry<DocumentKey, Document>> itr = chDocs.entrySet().iterator(); itr.hasNext();) {
			Map.Entry<DocumentKey, Document> entry = itr.next();
			chDocs.evict(entry.getKey());
			Thread.sleep(50);
			assertEquals(--cnt, chCont.size());
			assertTrue(chElts.size() < eSize);
			eSize = chElts.size();
		}
		assertEquals(0, chElts.size());
		assertEquals(0, chCont.size());
		assertEquals(0, chDocs.size());
		uris.clear();
	}

}
