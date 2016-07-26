package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static com.bagri.xdm.common.Constants.xs_ns;
import static com.bagri.xdm.common.Constants.xs_prefix;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.api.test.BagriManagementTest;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.domain.Occurrence;
import com.bagri.xdm.query.AxisType;
import com.bagri.xdm.query.Comparison;
import com.bagri.xdm.query.ExpressionContainer;
import com.bagri.xdm.query.PathBuilder;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.domain.NodeKind;
import com.bagri.xdm.system.Index;
import com.bagri.xdm.system.Schema;

public class UniqueIndexManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

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
		xRepo = context.getBean(SchemaRepositoryImpl.class);
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			xdmRepo.setSchema(schema);
			String typePath = getModelManagement().normalizePath("/{http://tpox-benchmark.com/security}Security");
			Index index = new Index(1, new Date(), xRepo.getUserName(), "IDX_Security_Symbol", "/{http://tpox-benchmark.com/security}Security", 
					typePath, "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Symbol/text()", new QName(xs_ns, "string", xs_prefix),
					true, true, true, "Security Symbol", true);
			xdmRepo.addSchemaIndex(index);
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

	public Collection<String> getSecurity(String symbol) throws Exception {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, Object> params = new HashMap<>();
		params.put(":sec", "/" + prefix + ":Security");
		return ((QueryManagement) getQueryManagement()).getContent(ec, ":sec", params);
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
			
		Collection<String> sec = getSecurity("IBM");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}

	@Test
	public void uniqueDocumentUpdateTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
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
		
		Collection<String> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	
	@Test
	public void uniqueDocumentRollbackTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().rollbackTransaction(txId);
		
		txId = getTxManagement().beginTransaction();
		doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);
		
		Collection<String> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	
	@Test
	public void uniqueDocumentDeleteTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(txId, doc.getTxStart());
		uris.add(doc.getUri());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		removeDocumentTest(doc.getUri());
		doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		getTxManagement().commitTransaction(txId);
		
		Collection<String> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	
	@Test
	public void twoDocumentsUpdateTest() throws Exception {

		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security9012.xml"));
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
