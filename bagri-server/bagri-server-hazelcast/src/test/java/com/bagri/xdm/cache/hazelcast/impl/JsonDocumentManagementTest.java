package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static com.bagri.xdm.common.Constants.xdm_document_data_format;
import static com.bagri.xdm.common.Constants.xdm_schema_format_default;

import java.util.ArrayList;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.DocumentManagementTest;
import com.bagri.xdm.system.DataFormat;
import com.bagri.xdm.system.Schema;
import com.bagri.xquery.api.XQProcessor;

public class JsonDocumentManagementTest extends DocumentManagementTest {
	
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
			schema.setProperty(xdm_schema_format_default, "JSON");
			xdmRepo.setSchema(schema);
			DataFormat df = new DataFormat(1, new java.util.Date(), "", "JSON", null, "application/json", null, 
					"com.bagri.xdm.common.df.json.JsonApiParser", "com.bagri.xdm.common.df.json.JsonBuilder", true, null);
			ArrayList<DataFormat> cFormats = new ArrayList<>(1);
			cFormats.add(df);
			xdmRepo.setDataFormats(cFormats);
		}
		// set xdm.document.format to JSON !
		//XQProcessor xqp = xdmRepo.getXQProcessor("test_client");
		//xqp.getProperties().setProperty("xdm.document.format", "JSON");
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	protected String getFileName(String original) {
		return original.substring(0, original.indexOf(".")) + ".json";
	}
	
	protected Properties getDocumentProperties() {
		Properties props = new Properties();
		props.setProperty(xdm_document_data_format, "JSON");
		return props;
	}

	
}
