package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_document_collections;
import static com.bagri.core.Constants.pn_node_instance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.DocumentManagementTest;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.JMXUtils;

public class DocumentManagementImplTest extends DocumentManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("bdb.log.level", "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
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
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
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
		Properties props = new Properties(); //getDocumentProperties();
		props.setProperty(pn_document_collections, "products");
		getDocManagement().storeDocumentFromString("product.xml", doc1, props);
		props.setProperty(pn_document_collections, "orders");
		getDocManagement().storeDocumentFromString("order.xml", doc2, props);
		getTxManagement().commitTransaction(txId);
	
		//System.out.println("paths: " + ((SchemaRepositoryImpl) xRepo).getModelManagement().getTypePaths(1));
		
		String query = 
				"for $ord in fn:collection(\"orders\")/order\n" +
				"for $pro in fn:collection(\"products\")/product[@id=$ord/products/product/@product_id]\n" + 
				"return <order id=\"{$ord/@id}\">\n" +
				    "    {$ord/type}\n" +
					"    {$pro}\n" +
					"    {$ord/products/product/quantity}\n" +
					"</order>";
				
		//props = new Properties();
		//props.setProperty("method", "json");
		ResultCursor docs = query(query, null, null); //props);
		assertNotNull(docs);
		int idx = 0;
		while (docs.next()) {
			String json = docs.getString();
			System.out.print(++idx + ": ");
			System.out.println(json);
		}
		docs.close();
		assertTrue(idx > 0);
	}
}
