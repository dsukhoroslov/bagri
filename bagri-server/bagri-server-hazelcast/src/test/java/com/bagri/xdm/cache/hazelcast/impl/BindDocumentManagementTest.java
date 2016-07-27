package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQItem;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.test.BagriManagementTest;
import com.bagri.xdm.cache.hazelcast.bean.SampleBean;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.Schema;

public class BindDocumentManagementTest extends BagriManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
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
		Document bDoc = xRepo.getDocumentManagement().storeDocumentFromBean("bean_test", sb1, null);
		assertNotNull(bDoc);
		uris.add(bDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String xml = xRepo.getDocumentManagement().getDocumentAsString(bDoc.getUri(), null);
		assertNotNull(xml);
		
		SampleBean sb2 = (SampleBean) xRepo.getDocumentManagement().getDocumentAsBean(bDoc.getUri(), null);
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
		Document mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test1", m1, null);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String xml = xRepo.getDocumentManagement().getDocumentAsString(mDoc.getUri(), null);
		assertNotNull(xml);
		System.out.println(xml);
		
		Map<String, Object> m2 = xRepo.getDocumentManagement().getDocumentAsMap(mDoc.getUri(), null);
		assertEquals(m1.get("intProp"), m2.get("intProp"));
		assertEquals(m1.get("boolProp"), m2.get("boolProp"));
		assertEquals(m1.get("strProp"), m2.get("strProp"));

		m2.put("intProp", 2); 
		m2.put("boolProp", Boolean.TRUE);
		m2.put("strProp", "ABC");
		txId = xRepo.getTxManagement().beginTransaction();
		mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test2", m2, null);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = //"declare default element namespace \"\";\n" + 
				"declare variable $value external;\n" +
				"for $doc in fn:collection()/map\n" +
				//"where $doc/intProp = $value\n" +
				"where $doc[intProp = $value]\n" +
				"return $doc/strProp/text()";
		
		Map<String, Object> params = new HashMap<>();
		params.put("value", 0);
		ResultCursor results = query(query, params, null);
		assertFalse(results.next());
		results.close();
		
		params.put("value", 1);
		results = query(query, params, null);
		assertTrue(results.next());
		
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) results.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("XYZ", text);
		assertFalse(results.next());
		results.close();
	}
		
	@Test
	public void queryDocumentTest() throws Exception {
        String xml = "<map>\n" +
        		"  <boolProp>false</boolProp>\n" +
        		"  <strProp>XYZ</strProp>\n" +
        		"  <intProp>1</intProp>\n" +
        		"</map>";
		
		long txId = xRepo.getTxManagement().beginTransaction();
		Document mDoc = xRepo.getDocumentManagement().storeDocumentFromString("map.xml", xml, null);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = //"declare default element namespace \"\";\n" +
				"declare variable $value external;\n" +
				"for $doc in fn:collection()/map\n" +
				"where $doc/intProp = $value\n" +
				//"where $doc[intProp = $value]\n" +
				"return $doc/strProp/text()";
		
		Map<String, Object> params = new HashMap<>();
		params.put("value", 1);
		try (ResultCursor results = query(query, params, null)) {
			assertTrue(results.next());
			Properties props = new Properties();
			props.setProperty("method", "text");
			XQItem item = (XQItem) results.getXQItem();
			String text = item.getItemAsString(props);
			assertEquals("XYZ", text);
			assertFalse(results.next());
		}
	}
		
}
