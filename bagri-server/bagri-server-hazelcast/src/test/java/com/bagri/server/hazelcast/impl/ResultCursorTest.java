package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.support.util.JMXUtils;

public class ResultCursorTest extends BagriManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
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
		SchemaRepositoryImpl repo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = repo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			//schema.setProperty(pn_baseURI, sampleRoot);
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
			repo.setSchema(schema);
			repo.setDataFormats(getBasicDataFormats());
			repo.setLibraries(new ArrayList<Library>());
			repo.setModules(new ArrayList<Module>());
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void fetchResultsTest() throws Exception {
		storeSecurityTest();
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sect external;\n" + 
				"declare variable $pemin external;\n" +
				"declare variable $pemax external;\n" + 
				"declare variable $yield external;\n" + 
				"for $sec in fn:collection(\"CLN_Security\")/Security\n" +
				//"for $sec in fn:collection()/Security\n" +
		  		"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
				"return	<Security>\n" +	
				"\t{$sec/Symbol}\n" +
				"\t{$sec/Name}\n" +
				"\t{$sec/SecurityType}\n" +
				"\t{$sec/SecurityInformation//Sector}\n" +
				"\t{$sec/PE}\n" +
				"\t{$sec/Yield}\n" +
				"</Security>";
		Map<String, Object> params = new HashMap<>();
		params.put("sect", "Technology");
	    params.put("pemin", 25.0f);
	    params.put("pemax", 28.0f);
	    params.put("yield", 0.1f);
		Properties props = new Properties();
		props.setProperty(pn_client_fetchSize, "1");
		props.setProperty(pn_client_id, "dummy");
		try (ResultCursor rc = query(query, params, props)) {
			assertTrue(rc.next());
			assertNotNull(rc.getObject());
			assertFalse(rc.next());
		}
	}
	
	@Test
	public void fetchSecurityTest() throws Exception {
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", "IBM");
		Properties props = new Properties();
		props.setProperty(pn_client_fetchSize, "1");
		props.setProperty(pn_client_id, "dummy");
		try (ResultCursor rc = query(query, params, props)) {
			assertTrue(rc.next());
			assertNotNull(rc.getObject());
			assertFalse(rc.next());
		}
	}

	@Test
	public void fetchSecuritiesTest() throws Exception {
		storeSecurityTest();
		final String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		
		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				Map<String, Object> params = new HashMap<>();
				params.put("sym", "IBM");
				Properties props = new Properties();
				props.setProperty(pn_client_fetchSize, "1");
				props.setProperty(pn_client_id, "thread1");
				try (ResultCursor rc = query(query, params, props)) {
					assertTrue(rc.next());
					assertNotNull(rc.getObject());
					assertFalse(rc.next());
				} catch (Exception ex) {
					assertTrue(ex.getMessage(), false);
				}
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				Map<String, Object> params = new HashMap<>();
				params.put("sym", "VFINX");
				Properties props = new Properties();
				props.setProperty(pn_client_fetchSize, "1");
				props.setProperty(pn_client_id, "thread2");
				try (ResultCursor rc = query(query, params, props)) {
					assertTrue(rc.next());
					assertNotNull(rc.getObject());
					assertFalse(rc.next());
				} catch (Exception ex) {
					assertTrue(ex.getMessage(), false);
				}
			}
		});
		
		th1.start();
		th2.start();
		
	}
}
