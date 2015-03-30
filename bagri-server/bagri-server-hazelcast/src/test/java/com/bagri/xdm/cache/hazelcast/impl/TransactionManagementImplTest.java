package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;

import java.io.IOException;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.domain.XDMPath;

public class TransactionManagementImplTest extends XDMManagementTest {

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
	}

	@Test
	public void rollbackTransactionTest() throws IOException {
		long txId = xRepo.getTxManagement().beginTransaction();
		storeSecurityTest();

		Collection<String> sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);

		xRepo.getTxManagement().rollbackTransaction(txId);
		
		sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);
	}
	
}
