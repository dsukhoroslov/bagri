package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_log_level;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;

public class ModelManagementImplTest extends BagriManagementTest {
	
    private static ClassPathXmlApplicationContext context;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		//System.setProperty(pn_log_level, "trace");
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
		xRepo.close();
	}
	
	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	public Collection<Path> getSecurityPath() {
		return getModelManagement().getTypePaths("/{http://tpox-benchmark.com/security}Security");
	}
	
	public Collection<Path> getCustomerPath() {
		return getModelManagement().getTypePaths("/{http://tpox-benchmark.com/custacc}Customer");
	}

	@Test
	public void getSecurityPathTest() throws Exception {
		storeSecurityTest();
		Collection<Path> sec = getSecurityPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}

	@Test
	public void getCustomerPathTest() throws Exception {
		storeCustomerTest();
		Collection<Path> sec = getCustomerPath();
		assertNotNull(sec);
		assertTrue(sec.size() > 0);
	}
		
	@Test
	public void getRootPathTest() {
		String root = getModelManagement().getPathRoot("/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Symbol/text()");
		assertEquals("/{http://tpox-benchmark.com/security}Security", root);
	}
}
