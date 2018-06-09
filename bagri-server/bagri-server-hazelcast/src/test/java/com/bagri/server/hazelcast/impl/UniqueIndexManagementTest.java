package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Index;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class UniqueIndexManagementTest extends BagriManagementTest {

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
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
			String typePath = "/{http://tpox-benchmark.com/security}Security";
			Index index = new Index(1, new Date(), xRepo.getUserName(), "IDX_Security_Symbol", "/{http://tpox-benchmark.com/security}Security", 
					typePath, "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Symbol/text()", new QName(xs_ns, "string", xs_prefix),
					true, true, true, "Security Symbol", true);
			xdmRepo.addSchemaIndex(index);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		//Assert.assertTrue(((IndexManagementImpl) ((RepositoryImpl) xRepo).getIndexManagement()).getIndexCache().size() == 0);
	}
	
	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	public Collection<Long> getSecurity(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	
	@Test
	public void uniqueDocumentCreateTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		uris.add(createDocumentTest(sampleRoot + getFileName("security5621.xml")).getUri());
		xRepo.getTxManagement().commitTransaction(txId);

		txId = xRepo.getTxManagement().beginTransaction();
		// this is an update because filename -> uri is the same, 
		// thus no unique index violation expected
		uris.add(createDocumentTest(sampleRoot + getFileName("security5621.xml")).getUri());
		xRepo.getTxManagement().commitTransaction(txId);

		txId = xRepo.getTxManagement().beginTransaction();
		try {
			uris.add(updateDocumentTest("security1500.xml", sampleRoot + getFileName("security5621.xml")).getUri());			
			xRepo.getTxManagement().commitTransaction(txId);
			assertFalse("expected unique index vialation exception", true);
		} catch (Exception ex) {
			// anticipated ex..
			xRepo.getTxManagement().rollbackTransaction(txId);
		}
			
		Collection<Long> sec = getSecurity("IBM");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}

	@Test
	public void uniqueDocumentUpdateTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		//int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		//assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
		
		Collection<Long> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	
	@Test
	public void uniqueDocumentRollbackTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().rollbackTransaction(txId);
		
		txId = getTxManagement().beginTransaction();
		doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		
		Collection<Long> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	
	@Test
	public void uniqueDocumentDeleteTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		removeDocumentTest(doc.getUri());
		doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		getTxManagement().commitTransaction(txId);
		
		Collection<Long> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	
	@Test
	public void twoDocumentsUpdateTest() throws Exception {

		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		
		txId = getTxManagement().beginTransaction();
		doc = createDocumentTest(sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		//long docId = doc.getDocumentId();
		//int version = doc.getVersion();
		//String uri = doc.getUri();
	}
	
}
