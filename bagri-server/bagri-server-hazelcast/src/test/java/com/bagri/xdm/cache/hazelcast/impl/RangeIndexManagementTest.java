package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static com.bagri.xdm.common.Constants.xs_ns;
import static com.bagri.xdm.common.Constants.xs_prefix;

import java.util.Date;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.BagriManagementTest;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.system.Index;
import com.bagri.xdm.system.Schema;

public class RangeIndexManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
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
			Index index = new Index(1, new Date(), xRepo.getUserName(), "IDX_Security_PE", "/{http://tpox-benchmark.com/security}Security", 
				typePath, "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}PE/text()", new QName(xs_ns, "decimal", xs_prefix), 
				true, true, false, "Security PE", true);
			xdmRepo.addSchemaIndex(index);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	
}



