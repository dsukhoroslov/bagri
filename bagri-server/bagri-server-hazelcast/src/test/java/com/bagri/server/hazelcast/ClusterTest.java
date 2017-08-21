package com.bagri.server.hazelcast;

import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.core.HazelcastInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;
import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_node_instance;

public class ClusterTest {

	private static final int cluster_size = 3; 
	private static ClassPathXmlApplicationContext[] contexts = new ClassPathXmlApplicationContext[cluster_size];

	private SchemaRepositoryImpl[] repos = new SchemaRepositoryImpl[cluster_size];
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty(pn_log_level, "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src/test/resources");

		for (int i=0; i < cluster_size; i++) {
			System.setProperty(pn_node_instance, String.valueOf(i));
			contexts[i] = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
		}
	}
    
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for (int i=0; i < cluster_size; i++) {
			contexts[i].close();
		}
	}

	@Before
	public void setUp() throws Exception {
		for (int i=0; i < cluster_size; i++) {
			repos[i] = contexts[i].getBean(SchemaRepositoryImpl.class);
		}
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
		HazelcastInstance hz1 = repos[0].getHzInstance();
		HazelcastInstance hz2 = repos[1].getHzInstance();
		//
		assertEquals(cluster_size, hz2.getCluster().getMembers().size());
		assertEquals(hz1.getCluster().getMembers().size(), hz2.getCluster().getMembers().size());
	}
	
	
}
