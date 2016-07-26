package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.util.FileUtils.readTextFile;
import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.BagriManagementTest;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.cache.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.xdm.domain.Path;
import com.bagri.xdm.system.Schema;

public class ModelManagementImplTest extends BagriManagementTest {
	
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
		}
	}

	@After
	public void tearDown() throws Exception {
		xRepo.close();
	}
	
	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	//protected XDMModelManagement mDictionary;
	
	public void registerSecuritySchemaTest() throws Exception { 
		//String schema = sampleRoot + "security.xsd";
		String schema = readTextFile(sampleRoot + "security.xsd");
		getModelManagement().registerSchema(schema);
	}
	
	public void registerCustaccSchemaTest() throws Exception { 
		//String schema = sampleRoot + "custacc.xsd";
		String schema = readTextFile(sampleRoot + "custacc.xsd");
		getModelManagement().registerSchema(schema);
	}
	
	//public void registerCommonSchemaTest() throws IOException { 
		//String schema = sampleRoot + "custacc.xsd";
	//	String schema = readTextFile(sampleRoot + "Common.xsd");
	//	mDictionary.registerSchema(schema);
	//}
	
	protected Collection<Path> getPath(String namespace, String template) {
		String prefix = getModelManagement().getNamespacePrefix(namespace);
		String path = String.format(template, prefix);
		int docType = getModelManagement().getDocumentType(path);
		return getModelManagement().getTypePaths(docType);
	}
	
	public Collection<Path> getSecurityPath() {
		return getPath("http://tpox-benchmark.com/security", "/%s:Security");
	}
	
	public Collection<Path> getCustomerPath() {
		return getPath("http://tpox-benchmark.com/custacc", "/%s:Customer");
	}

	@Test
	public void registerSecurityPathTest() throws Exception {
		registerSecuritySchemaTest();
		Collection<Path> sec = getSecurityPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}

	@Test
	public void registerCustomerPathTest() throws Exception {
		registerCustaccSchemaTest();
		Collection<Path> sec = getCustomerPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}

	@Test
	//@Ignore
	public void getSecurityPathTest() throws Exception {
		storeSecurityTest();
		Collection<Path> sec = getSecurityPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}

	@Test
	//@Ignore
	public void getCustomerPathTest() throws Exception {
		storeCustomerTest();
		Collection<Path> sec = getCustomerPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}
	
	//@Test
	//public void testParse() throws IOException {
	//	String sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	//	String fileName = sampleRoot + "security.xsd";
	//	XDMSchemaProcessor proc = new XDMSchemaProcessor();
	//	List<XDMPath> xpl = proc.parse(fileName);
	//	assertNotNull(xpl);
	//}
	
	@Test
	public void testNormalizePath() throws Exception {
		String path = "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Yield/text()";
		String ns = ((SchemaRepository) xRepo).getModelManagement().translateNamespace("http://tpox-benchmark.com/security");
		String np = ((SchemaRepository) xRepo).getModelManagement().normalizePath(path);
		assertNotNull(np);
		String rp = "/" + ns + ":Security/" + ns + ":Yield/text()";
		assertEquals("unexpected normalized path: " + np + " when expected: " + rp, rp, np);
		path = "/Security/Yield/text()";
		np = ((SchemaRepository) xRepo).getModelManagement().normalizePath(path);
		assertNotNull(np);
		assertEquals("unexpected normalized path: " + np + " when expected: " + path, path, np);
	}
	

	@Test
	public void testNormalizePaths() throws Exception {
		String path = "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Yield/text()";
		String ns = ((SchemaRepository) xRepo).getModelManagement().translateNamespace("http://tpox-benchmark.com/security");
		String np = ((SchemaRepository) xRepo).getModelManagement().normalizePath(path);
		assertNotNull(np);
		String rp = "/" + ns + ":Security/" + ns + ":Yield/text()";
		assertEquals("unexpected normalized path: " + np + " when expected: " + rp, rp, np);

		path = "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Yield/";
		rp = "/" + ns + ":Security/" + ns + ":Yield/";
		int count = 10000;
		long time = System.currentTimeMillis();
		for (int i=0; i < count; i++) {
			String test = path + i;
			np = ((SchemaRepository) xRepo).getModelManagement().normalizePath(test);
			assertNotNull(np);
			assertEquals("unexpected normalized path: " + np + " when expected: " + rp + i, rp + i, np);
		}
		time = System.currentTimeMillis() - time;
		double avg = time;
		avg = avg/count;
		System.out.println("time taken: " + time + " for " + count + " normalizations; avg time: " + avg);
	}
	
}
