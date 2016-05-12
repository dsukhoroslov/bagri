package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.pn_baseURI;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xquery.api.XQProcessor;

public class SimpleQueryManagementTest extends XDMManagementTest {

    private static ClassPathXmlApplicationContext context;
    
    private XQProcessor xqProc; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Thread.sleep(3000);
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		xqProc = context.getBean("xqProcessor", XQProcessor.class);
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			schema.setProperty(pn_baseURI, sampleRoot);
			xdmRepo.setSchema(schema);
			//XDMCollection collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
			//		1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			//schema.addCollection(collection);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void simplestQueryTest() throws Exception {
		
		String uri = "test_document";
		String xml = "<content>XML Content</content>";

		long txId = xRepo.getTxManagement().beginTransaction();
		Properties props = new Properties();
		XDMDocument xDoc = xRepo.getDocumentManagement().storeDocumentFromString(uri, xml, props);
		xRepo.getTxManagement().commitTransaction(txId);
		assertNotNull(xDoc);
		assertEquals(uri, xDoc.getUri());
		assertEquals(txId, xDoc.getTxStart());
		uris.add(xDoc.getUri());
		
		String query = "for $doc in fn:collection()\n" +
				"return $doc\n";

		ResultCursor rc = (ResultCursor) xRepo.getQueryManagement().executeQuery(query, null, new Properties());
		rc.deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(rc.hasNext());
		
		props = new Properties();
		props.setProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
		props.setProperty(javax.xml.transform.OutputKeys.INDENT, "no");
		props.setProperty(javax.xml.transform.OutputKeys.METHOD, "text");
		String text = xqProc.convertToString(rc.next(), props);
		assertEquals("XML Content", text);
	}


}
