package com.bagri.server.hazelcast;

import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_node_instance;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.core.HazelcastInstance;

public class ClusterTest {

	private SchemaRepositoryImpl repo1;
	private SchemaRepositoryImpl repo2;
    private static ClassPathXmlApplicationContext context1;
    private static ClassPathXmlApplicationContext context2;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src\\test\\resources");
		context1 = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
		System.setProperty(pn_node_instance, "1");
		//System.setProperty(pn_config_properties_file, "json.properties");
		context2 = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context2.close();
		context1.close();
	}

	@Before
	public void setUp() throws Exception {
		repo1 = context1.getBean(SchemaRepositoryImpl.class);
		repo2 = context2.getBean(SchemaRepositoryImpl.class);
		//SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		//Schema schema = xdmRepo.getSchema();
		//if (schema == null) {
		//	schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
		//	xdmRepo.setSchema(schema);
		//	xdmRepo.setDataFormats(getBasicDataFormats());
		//	xdmRepo.setLibraries(new ArrayList<Library>());
		//	xdmRepo.setModules(new ArrayList<Module>());
		//}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
	}


	@Test
	public void testHzCluster() {
		HazelcastInstance hz1 = repo1.getHzInstance();
		HazelcastInstance hz2 = repo2.getHzInstance();
		//
		assertEquals(hz1.getCluster().getMembers().size(), hz2.getCluster().getMembers().size());
	}
	
	
}
