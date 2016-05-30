package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;

import java.util.Date;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMSchema;

public class RangeIndexManagementTest extends XDMManagementTest {

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
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			xdmRepo.setSchema(schema);
			String typePath = getModelManagement().normalizePath("/{http://tpox-benchmark.com/security}Security");
			XDMIndex index = new XDMIndex(1, new Date(), xRepo.getUserName(), "IDX_Security_PE", "/{http://tpox-benchmark.com/security}Security", 
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

}



