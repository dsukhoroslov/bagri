package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.Document;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.core.xquery.api.XQProcessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class SimpleQueryManagementTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
    
    private XQProcessor xqProc; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/tpox/";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src/test/resources");
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
		xqProc = context.getBean("xqProcessor", XQProcessor.class);
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			schema.setProperty(pn_xqj_baseURI, sampleRoot);
			xdmRepo.setSchema(schema);
			//XDMCollection collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
			//		1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			//schema.addCollection(collection);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void simplestQueryTest() throws Exception {
		
		String uri = "test_document.xml";
		String xml = "<content>XML Content</content>";

		long txId = xRepo.getTxManagement().beginTransaction();
		Properties props = new Properties();
		props.setProperty(pn_document_data_format, "XML");
		DocumentAccessor xDoc = xRepo.getDocumentManagement().storeDocument(uri, xml, props);
		xRepo.getTxManagement().commitTransaction(txId);
		assertNotNull(xDoc);
		assertEquals(uri, xDoc.getUri());
		assertEquals(txId, xDoc.getTxStart());
		uris.add(xDoc.getUri());
		
		String query = "for $doc in fn:collection()\n" +
				"return $doc\n";

		ResultCursor rc = query(query, null, null);
		assertTrue(rc.next());
		
		props = new Properties();
		props.setProperty(pn_document_data_format, "XML");
		props.setProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
		props.setProperty(javax.xml.transform.OutputKeys.INDENT, "no");
		props.setProperty(javax.xml.transform.OutputKeys.METHOD, "text");
		String text = xqProc.convertToString(rc.getXQItem(), props);
		assertEquals("XML Content", text);
		rc.close();
		
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
		xDoc = xRepo.getDocumentManagement().getDocument(uri, props);
		assertEquals(xml, xDoc.getContent());
	}

}
