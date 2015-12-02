package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.pn_client_id;
import static com.bagri.xdm.common.XDMConstants.pn_client_fetchSize;
import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;
import static com.bagri.xqj.BagriXQUtils.getBaseTypeForTypeName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMManagementTest;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.system.XDMSchema;

public class QueryCacheManagementTest extends XDMManagementTest {

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
	}

	@After
	public void tearDown() throws Exception {
		removeDocumentsTest();
	}

	@Test
	public void invalidateQueryCacheTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		storeSecurityTest();
		xRepo.getTxManagement().commitTransaction(txId);
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return $sec\n";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("sym"), "VFINX");
		Properties props = new Properties();
		props.setProperty(pn_client_id, "1");
		props.setProperty(pn_client_fetchSize, "1");
		Iterator itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		Assert.assertNotNull(itr);
		//((ResultCursor) itr).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Assert.assertTrue(itr.hasNext());
		// here we must have 1 result cached
		itr = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, bindings, props);		
		Assert.assertNotNull(itr);
		Assert.assertTrue(itr.hasNext());
		
		txId = xRepo.getTxManagement().beginTransaction();
		removeDocumentTest(1); 
		xRepo.getTxManagement().commitTransaction(txId);
		//updateDocumentTest(0, null, sampleRoot + getFileName("security5621.xml"));
		// here we must have 0 result cached
		itr = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, bindings, props);		
		Assert.assertNull(itr);
		//Assert.assertFalse(itr.hasNext());
	}

	@Test
	public void invalidateResultCacheTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		createDocumentTest(sampleRoot + getFileName("security5621.xml"));
		xRepo.getTxManagement().commitTransaction(txId);
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sect external;\n" + 
			"declare variable $pemin external;\n" +
			"declare variable $pemax external;\n" + 
			"declare variable $yield external;\n" + 
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/Security\n" +
		  	"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
			"return	<Security>\n" +	
			"\t{$sec/Symbol}\n" +
			"\t{$sec/Name}\n" +
			"\t{$sec/SecurityType}\n" +
			"\t{$sec/SecurityInformation//Sector}\n" +
			"\t{$sec/PE}\n" +
			"\t{$sec/Yield}\n" +
			"</Security>";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("sect"), "Technology"); 
		bindings.put(new QName("pemin"), new java.math.BigDecimal("25.0"));
		bindings.put(new QName("pemax"), new java.math.BigDecimal("28.0"));
		bindings.put(new QName("yield"), new java.math.BigDecimal("0.1"));
		
		Properties props = new Properties();
		props.setProperty(pn_client_id, "2");
		props.setProperty(pn_client_fetchSize, "1");
		Iterator itr = xRepo.getQueryManagement().executeXQuery(query, bindings, props);
		Assert.assertNotNull(itr);
		//((ResultCursor) itr).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Assert.assertTrue(itr.hasNext());
		// here we must have 1 result cached
		itr = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, bindings, props);		
		Assert.assertNotNull(itr);
		Assert.assertTrue(itr.hasNext());
		
		txId = xRepo.getTxManagement().beginTransaction();
		createDocumentTest(sampleRoot + getFileName("security9012.xml"));
		xRepo.getTxManagement().commitTransaction(txId);
		// here we must have 0 result cached
		itr = ((QueryManagementImpl) xRepo.getQueryManagement()).getQueryResults(query, bindings, props);		
		Assert.assertNull(itr);
		//Assert.assertFalse(itr.hasNext());
	}
	
}
