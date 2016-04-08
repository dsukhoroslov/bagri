package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_path;
import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQPreparedExpression;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.system.XDMSchema;

public class ResultCursorTest extends XDMManagementTest {
	
    private static ClassPathXmlApplicationContext context;

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
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			xdmRepo.setSchema(schema);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		//removeDocumentsTest();
	}

	@Test
	public void fetchResultsTest() throws Exception {
		storeSecurityTest();
		QueryManagementImpl qm = (QueryManagementImpl) getQueryManagement();
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sect external;\n" + 
				"declare variable $pemin external;\n" +
				"declare variable $pemax external;\n" + 
				"declare variable $yield external;\n" + 
				//"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/Security\n" +
				"for $sec in fn:collection()/Security\n" +
		  		"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
				"return	<Security>\n" +	
				"\t{$sec/Symbol}\n" +
				"\t{$sec/Name}\n" +
				"\t{$sec/SecurityType}\n" +
				"\t{$sec/SecurityInformation//Sector}\n" +
				"\t{$sec/PE}\n" +
				"\t{$sec/Yield}\n" +
				"</Security>";
		Map<QName, Object> params = new HashMap<>();
		params.put(new QName("sect"), "Technology");
	    params.put(new QName("pemin"), 25.0f);
	    params.put(new QName("pemax"), 28.0f);
	    params.put(new QName("yield"), 0.1f);
		Properties props = new Properties();
		props.setProperty(pn_client_fetchSize, "1");
		props.setProperty(pn_client_id, "dummy");
		ResultCursor rc = (ResultCursor) qm.executeQuery(query, params, props);
		rc.deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(rc.hasNext());
		assertNotNull(rc.next());
		assertFalse(rc.hasNext());
	}
	
	@Test
	public void fetchSecurityTest() throws Exception {
		storeSecurityTest();
		QueryManagementImpl qm = (QueryManagementImpl) getQueryManagement();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<QName, Object> params = new HashMap<>();
		params.put(new QName("sym"), "IBM");
		Properties props = new Properties();
		props.setProperty(pn_client_fetchSize, "1");
		props.setProperty(pn_client_id, "dummy");
		ResultCursor rc = (ResultCursor) qm.executeQuery(query, params, props);
		rc.deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(rc.hasNext());
		assertNotNull(rc.next());
		assertFalse(rc.hasNext());
	}

	@Test
	public void fetchSecuritiesTest() throws Exception {
		storeSecurityTest();
		final QueryManagementImpl qm = (QueryManagementImpl) getQueryManagement();
		final String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				//"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		final Properties props = new Properties();
		props.setProperty(pn_client_fetchSize, "1");
		props.setProperty(pn_client_id, "dummy");
		
		Thread th1 = new Thread(new Runnable() {

			@Override
			public void run() {
				Map<QName, Object> params = new HashMap<>();
				params.put(new QName("sym"), "IBM");
				ResultCursor rc;
				try {
					rc = (ResultCursor) qm.executeQuery(query, params, props);
					rc.deserialize(((RepositoryImpl) xRepo).getHzInstance());
					assertTrue(rc.hasNext());
					assertNotNull(rc.next());
					assertFalse(rc.hasNext());
				} catch (XDMException ex) {
					assertTrue(ex.getMessage(), false);
				}
			}
		});

		Thread th2 = new Thread(new Runnable() {

			@Override
			public void run() {
				Map<QName, Object> params = new HashMap<>();
				params.put(new QName("sym"), "VFINX");
				ResultCursor rc;
				try {
					rc = (ResultCursor) qm.executeQuery(query, params, props);
					rc.deserialize(((RepositoryImpl) xRepo).getHzInstance());
					assertTrue(rc.hasNext());
					assertNotNull(rc.next());
					assertFalse(rc.hasNext());
				} catch (XDMException ex) {
					assertTrue(ex.getMessage(), false);
				}
			}
		});
		
	}
}
