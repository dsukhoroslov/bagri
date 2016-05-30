package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.cache.hazelcast.bean.SampleBean;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMSchema;

public class BindDocumentManagementTest extends XDMManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
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
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void createBeanDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		SampleBean sb1 = new SampleBean(1, false, "XYZ");
		XDMDocument bDoc = xRepo.getDocumentManagement().storeDocumentFromBean("bean_test", sb1, null);
		assertNotNull(bDoc);
		uris.add(bDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String xml = xRepo.getDocumentManagement().getDocumentAsString(bDoc.getUri());
		assertNotNull(xml);
		
		SampleBean sb2 = (SampleBean) xRepo.getDocumentManagement().getDocumentAsBean(bDoc.getUri());
		assertEquals(sb1.getIntProperty(), sb2.getIntProperty());
		assertTrue(sb1.isBooleanProperty() == sb2.isBooleanProperty());
		assertEquals(sb1.getStringProperty(), sb2.getStringProperty());
	}
	
	@Test
	public void createMapDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		Map<String, Object> m1 = new HashMap<>();
		m1.put("intProp", 1); 
		m1.put("boolProp", Boolean.FALSE);
		m1.put("strProp", "XYZ");
		XDMDocument bDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test", m1, null);
		assertNotNull(bDoc);
		uris.add(bDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String xml = xRepo.getDocumentManagement().getDocumentAsString(bDoc.getUri());
		assertNotNull(xml);
		
		Map<String, Object> m2 = xRepo.getDocumentManagement().getDocumentAsMap(bDoc.getUri());
		assertEquals(m1.get("intProp"), m2.get("intProp"));
		assertEquals(m1.get("boolProp"), m2.get("boolProp"));
		assertEquals(m1.get("strProp"), m2.get("strProp"));
	}
		
}
