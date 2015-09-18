package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.xdm.api.test.ServerLauncher;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ModelManagementImplTest extends XDMManagementTest {

	//private static ServerLauncher launcher;
	//private static final String srvDir = "C:\\Work\\Bagri\\git\\bagri\\bagri-server\\bagri-server-hazelcast";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//launcher = new ServerLauncher("second", null, srvDir);
		//launcher.startServer();

		System.setProperty(pn_schema_address, "localhost:10500"); 
		System.setProperty(pn_schema_name, "default"); 
		System.setProperty(pn_schema_user, "guest");
		System.setProperty(pn_schema_password, "password");
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Hazelcast.shutdownAll();
		//launcher.stopServer();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = new RepositoryImpl();
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		//removeDocumentsTest();
		xRepo.close();
	}


	@Test
	public void testNormalizePath() throws Exception {
		String path = "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Yield/text()";
		String np = xRepo.getModelManagement().normalizePath(path);
		assertNotNull(np);
		assertEquals("unexpected normalized path: " + np + " when expected: /ns1:Security/ns1:Yield/text()", np, "/ns1:Security/ns1:Yield/text()");
		path = "/Security/Yield/text()";
		np = xRepo.getModelManagement().normalizePath(path);
		assertNotNull(np);
		assertEquals("unexpected normalized path: " + np + " when expected: " + path, np, path);
	}
	

	@Test
	public void testNormalizePaths() throws Exception {
		String path = "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Yield/text()";
		String np = xRepo.getModelManagement().normalizePath(path);
		assertNotNull(np);
		assertEquals("unexpected normalized path: " + np + " when expected: /ns1:Security/ns1:Yield/text()", np, "/ns1:Security/ns1:Yield/text()");

		path = "/{http://tpox-benchmark.com/security}Security/{http://tpox-benchmark.com/security}Yield/";
		int count = 10000;
		long time = System.currentTimeMillis();
		for (int i=0; i < count; i++) {
			String test = path + i;
			//np = ((ModelManagementImpl) xRepo.getModelManagement()).normalizePathOld(test);
			np = xRepo.getModelManagement().normalizePath(test);
			assertNotNull(np);
			assertEquals("unexpected normalized path: " + np + " when expected: /ns1:Security/ns1:Yield/" + i, np, "/ns1:Security/ns1:Yield/" + i);
		}
		time = System.currentTimeMillis() - time;
		double avg = time;
		avg = avg/count;
		System.out.println("time taken: " + time + " for " + count + " normalizations; avg time: " + avg);
	}
}


