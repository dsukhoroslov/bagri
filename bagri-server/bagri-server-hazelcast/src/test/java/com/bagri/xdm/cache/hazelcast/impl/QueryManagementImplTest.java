package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.pn_client_fetchSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_id;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMQueryManagementTest;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.system.XDMSchema;

public class QueryManagementImplTest extends XDMQueryManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "trace");
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
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	
	@Test
	public void selectDocumentTest() throws Exception {
		
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"for $sec in fn:doc(\"bgdm:/1\")/s:Security\n" +
		  		//"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<QName, Object> bindings = new HashMap<>();
		//bindings.put(new QName("sym"), "VFINX");
		Properties props = new Properties();
		props.setProperty(pn_client_id, "1");
		props.setProperty(pn_client_fetchSize, "0");
		Iterator itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		Assert.assertNotNull(itr);
		((ResultCursor) itr).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Assert.assertTrue(itr.hasNext());

		//query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
		//		"for $sec in fn:doc(\"bgdm:/2\")/s:Security\n" +
		//		"return $sec\n";
		//itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		//Assert.assertNull(itr);

		query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"for $sec in fn:doc(\"bgdm:/65537\")/s:Security\n" +
				"return $sec\n";
		itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		Assert.assertNotNull(itr);
		((ResultCursor) itr).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Assert.assertTrue(itr.hasNext());
		
		query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"for $sec in fn:doc(\"bgdm:/131073\")/s:Security\n" +
				"return $sec\n";
		itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		Assert.assertNotNull(itr);
		((ResultCursor) itr).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Assert.assertTrue(itr.hasNext());
	}

	@Test
	public void selectDocumentByUriTest() throws Exception {
		
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"for $sec in fn:doc(\"1.xml\")/s:Security\n" +
		  		//"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<QName, Object> bindings = new HashMap<>();
		//bindings.put(new QName("sym"), "VFINX");
		Properties props = new Properties();
		props.setProperty(pn_client_id, "1");
		props.setProperty(pn_client_fetchSize, "0");
		Iterator itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		Assert.assertNotNull(itr);
		((ResultCursor) itr).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Assert.assertTrue(itr.hasNext());
	}
	
}
