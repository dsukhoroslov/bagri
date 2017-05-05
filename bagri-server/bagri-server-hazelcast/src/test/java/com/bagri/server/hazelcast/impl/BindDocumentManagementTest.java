package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_node_instance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.setContext;
import static org.junit.Assert.*;

import java.util.ArrayList;
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

import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.Document;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.bean.SampleBean;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;

public class BindDocumentManagementTest extends BagriManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
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
			setContext(schema.getName(), context);
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
		Document bDoc = xRepo.getDocumentManagement().storeDocumentFromBean("bean_test.xml", sb1, null);
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
	    Properties props = new Properties();
		props.setProperty(pn_document_data_format, "MAP");
		Document mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test1.xml", m1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		//String xml = xRepo.getDocumentManagement().getDocumentAsString(mDoc.getUri(), null);
		//assertNotNull(xml);
		//System.out.println(xml);
		
		Map<String, Object> m2 = xRepo.getDocumentManagement().getDocumentAsMap(mDoc.getUri(), props);
		assertEquals(m1.get("intProp"), m2.get("intProp"));
		assertEquals(m1.get("boolProp"), m2.get("boolProp"));
		assertEquals(m1.get("strProp"), m2.get("strProp"));
/*
		m2.put("intProp", 2); 
		m2.put("boolProp", Boolean.TRUE);
		m2.put("strProp", "ABC");
		txId = xRepo.getTxManagement().beginTransaction();
		mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test2.xml", m2, props);
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
		
		props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) results.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("XYZ", text);
		assertFalse(results.next());
		results.close();
*/		
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
