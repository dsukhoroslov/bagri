package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;

import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.api.test.XDMQueryManagementTest;
import com.bagri.xdm.domain.XDMOccurence;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMSchema;

public class RangeIndexManagementTest extends XDMQueryManagementTest {

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
		String typePath = getModelManagement().normalizePath("/{http://tpox-benchmark.com/security}Security");
		XDMIndex index = new XDMIndex(1, new Date(), JMXUtils.getCurrentUser(), "IDX_Security_PE", "/{http://tpox-benchmark.com/security}Security", 
				typePath, "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}PE/text()", new QName(xs_ns, "decimal", xs_prefix), 
				true, true, false, "Security PE", true);
		xdmRepo.addSchemaIndex(index);
		
		///int docType = xdmRepo.getModelManagement().translateDocumentType("/{http://tpox-benchmark.com/security}Security");
		//int pathId = xdmRepo.getModelManagement().translatePath(docType, 
		//		"/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}PE/text()", 
		//		XDMNodeKind.text, XQItemType.XQBASETYPE_DOUBLE, XDMOccurence.onlyOne).getPathId();
		//if (!xdmRepo.getIndexManagement().isPathIndexed(pathId)) {
		//	System.out.println("path not indexed!!");
		//}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

}



