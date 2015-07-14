package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMSchema;

public class UniqueIndexManagementTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Hazelcast.shutdownAll();
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
		}
		XDMIndex index = new XDMIndex(1, new Date(), JMXUtils.getCurrentUser(), "IDX_Security_Symbol", "/{http://tpox-benchmark.com/security}Security", 
				"/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Symbol/text()", "xs:string", true, true, true,  
				"Security Symbol", true);
		xdmRepo.addSchemaIndex(index);
	}

	@Test
	public void uniqueIndexTest() throws IOException {
		long txId = xRepo.getTxManagement().beginTransaction();
		ids.add(createDocumentTest(sampleRoot + getFileName("security1500.xml")).getDocumentKey());
		xRepo.getTxManagement().commitTransaction(txId);

		txId = xRepo.getTxManagement().beginTransaction();
		try {
			ids.add(createDocumentTest(sampleRoot + getFileName("security1500.xml")).getDocumentKey());
			xRepo.getTxManagement().commitTransaction(txId);
			//throw new 
		} catch (Exception ex) {
			// anticipated ex..
		}
		
		Collection<String> sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue("expected 1 but got " + sec.size() + " test documents", sec.size() == 1);
	}
	

}
