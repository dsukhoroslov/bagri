package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_node_instance;
import static com.bagri.core.test.TestUtils.getBasicDataFormats;
import static com.bagri.core.test.TestUtils.loadProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.JavaTrigger;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerAction;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;
import com.bagri.core.system.TriggerDefinition;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.bean.DocumentTriggerImpl;
import com.bagri.server.hazelcast.bean.TransactionTriggerImpl;

public class TriggerManagementImplTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
    private DocumentTriggerImpl trigger;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/tpox/";
		//System.setProperty(pn_log_level, "trace");
		//System.setProperty("hz.log.level", "trace");
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
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			xdmRepo.setSchema(schema);
			trigger = new DocumentTriggerImpl(); 
			TriggerDefinition docTrigger = new JavaTrigger(1, new Date(), xRepo.getUserName(), "", //null, 
					DocumentTriggerImpl.class.getName(), "/{http://tpox-benchmark.com/security}Security", true, true, 0);
			docTrigger.getActions().add(new TriggerAction(Order.before, Scope.insert));
			docTrigger.getActions().add(new TriggerAction(Order.after, Scope.insert));
			docTrigger.getActions().add(new TriggerAction(Order.before, Scope.update));
			docTrigger.getActions().add(new TriggerAction(Order.after, Scope.update));
			docTrigger.getActions().add(new TriggerAction(Order.before, Scope.delete));
			docTrigger.getActions().add(new TriggerAction(Order.after, Scope.delete));
			xdmRepo.addSchemaTrigger(docTrigger, trigger);
			TriggerDefinition txTrigger = new JavaTrigger(1, new Date(), xRepo.getUserName(), "", //null, 
					TransactionTriggerImpl.class.getName(), "", true, true, 1);
			txTrigger.getActions().add(new TriggerAction(Order.after, Scope.begin));
			txTrigger.getActions().add(new TriggerAction(Order.before, Scope.commit));
			xdmRepo.addSchemaTrigger(txTrigger);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
		}
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
		assertEquals(1, trigger.getFires(Order.after, Scope.delete));
		//Thread.sleep(1000);
	}

	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	@Test
	public void updateSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);
		String uri = doc.getUri();
		assertEquals(1, trigger.getFires(Order.after, Scope.insert));
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
		assertEquals(1, trigger.getFires(Order.after, Scope.update));

		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
		assertEquals(2, trigger.getFires(Order.after, Scope.update));
	}
	

}
