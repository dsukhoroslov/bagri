package com.bagri.server.hazelcast.impl;

import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.task.query.QueryProcessor;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQItemAccessor;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class QueryManagementImplTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

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
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			schema.setProperty(pn_xqj_baseURI, sampleRoot);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			com.bagri.core.system.Collection collection = new com.bagri.core.system.Collection(1, new Date(), 
					JMXUtils.getCurrentUser(), 1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", null, "securities", true);
			schema.addCollection(collection);
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		((SchemaRepositoryImpl) xRepo).setClientId(client_id);
		removeDocumentsTest();
		Thread.sleep(1000);
	}

	
	public Collection<Long> getPrice(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	public Collection<Long> getSecurity(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	public Collection<Long> getOrder(String id) throws Exception {
		String prefix = "http://www.fixprotocol.org/FIXML-4-4"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":FIXML"); // /" + prefix + ":Order");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "FIXML").
				addPathSegment(AxisType.CHILD, prefix, "Order").
				addPathSegment(AxisType.ATTRIBUTE, null, "ID");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	public Collection<Long> getCustomerProfile(String id) throws Exception {
		String prefix = "http://tpox-benchmark.com/custacc"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.ATTRIBUTE, null, "id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	public Collection<Long> getCustomerAccounts(String id) throws Exception {
		String prefix = "http://tpox-benchmark.com/custacc"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.ATTRIBUTE, null, "id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);
		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	public Collection<Long> searchSecurity(String sector, float peMin, float peMax, float yieldMin) throws Exception {

		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.AND, path);
		ec.addExpression(docType, Comparison.AND, path); //why this one?
		path.addPathSegment(AxisType.CHILD, prefix, "SecurityInformation").
				addPathSegment(AxisType.CHILD, null, "*").
				addPathSegment(AxisType.CHILD, prefix, "Sector").
				addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.EQ, path, "$sec", sector);
		path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "PE");
		ec.addExpression(docType, Comparison.AND, path);
		path.addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.GE, path, "$peMin", new BigDecimal(peMin));
		ec.addExpression(docType, Comparison.LT, path, "$peMax", new BigDecimal(peMax));
		path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Yield").
				addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.GT, path, "$yMin", new BigDecimal(yieldMin));
   		return ((QueryManagement) getQueryManagement()).getDocumentIds(ec);
	}
	
	@Test
	public void getPriceTest() throws Exception {
		storeSecurityTest();

		Collection<Long> sec = getPrice("VFINX");
		assertNotNull(sec);
		assertEquals(1, sec.size());

		sec = getPrice("IBM");
		assertNotNull(sec);
		assertEquals(1, sec.size());

		sec = getPrice("PTTAX");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}

	@Test
	public void getSecurityTest() throws Exception {
		storeSecurityTest();

		Collection<Long> sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertEquals(1, sec.size());

		sec = getSecurity("IBM");
		assertNotNull(sec);
		assertEquals(1, sec.size());

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}

	@Test
	public void searchSecurityTest() throws Exception {
		storeSecurityTest();

		Collection<Long> sec = searchSecurity("Technology", 25, 28, 0);
		assertNotNull(sec);
		assertEquals(1, sec.size());

		sec = searchSecurity("Technology", 25, 28, 1);
		assertNotNull(sec);
		assertEquals(0, sec.size());

		sec = searchSecurity("Technology", 28, 29, 0);
		assertNotNull(sec);
		assertEquals(0, sec.size());
	}

	@Test
	public void getOrderTest() throws Exception {
		storeOrderTest();
		Collection<Long> sec = getOrder("103404");
		assertNotNull(sec);
		assertEquals(1, sec.size());
		sec = getOrder("103935");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}

	@Test
	public void getCustomerProfileTest() throws Exception {
		storeCustomerTest();
		Collection<Long> sec = getCustomerProfile("1011");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		storeCustomerTest();
		Collection<Long> sec = getCustomerAccounts("1011");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}
	
	@Test
	public void selectDocumentByUriTest() throws Exception {
		
		storeSecurityTest();
		Map<String, Object> params = new HashMap<>();
		Properties props = new Properties();
		props.setProperty(pn_client_id, "1");
		String[] uris = new String[] {"security1500.xml", "security5621.xml", "security9012.xml"};
		for (String uri: uris) {
			String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"for $sec in fn:doc(\"" + uri + "\")/s:Security\n" +
				"return $sec\n";
			checkCursorResult(query, params, props, null);
		}
	}
	
	@Test
	public void compareSecurityPriceTest() throws Exception {
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
			"for $sec in fn:collection()/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return \n" +
			"\t<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", "VFINX");
		Properties props = getDocumentProperties();
		props.setProperty(pn_xqj_defaultElementTypeNamespace, "");
		checkCursorResult(query, params, props, "The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars");
	}
	
	@Test
	@Ignore
	public void processSecurityQueryTest() throws Exception {
		storeSecurityTest();
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			//"for $sec in fn:collection(\"CLN_Security\")/s:Security\n" +
			"for $sec in fn:collection()/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return \n" +
			"\t<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", "VFINX");
		Properties props = getDocumentProperties();
		//props.setProperty(pn_client_fetchSize, "5");
		//props.setProperty(pn_xqj_defaultElementTypeNamespace, "");
		KeyFactory f = context.getBean(KeyFactory.class);
		HazelcastInstance hz = context.getBean(HazelcastInstance.class);
		IMap qrCache = hz.getMap(CN_XDM_RESULT);
		QueryProcessor qp = new QueryProcessor(client_id, 0, query, params, props, true);
		//Long key = new Long("security1500.xml".hashCode());
		int hc = "security1500.xml".hashCode();
		long ch = hc;
		Long key = ch;
		DocumentKey dk = f.newDocumentKey("security1500.xml", 0, 1);
		ch = dk.getHash();
		key = ch;
		System.out.println("uri partition: " + hz.getPartitionService().getPartition(key).getPartitionId() +
				"; doc partition: " + hz.getPartitionService().getPartition(dk).getPartitionId() +
				"; hash partition: " + hz.getPartitionService().getPartition(dk.getHash()).getPartitionId());

		try (ResultCursor<XQItemAccessor> rc = (ResultCursor<XQItemAccessor>) qrCache.executeOnKey(dk.getHash(), qp)) {
			assertNotNull(rc);
			Iterator<XQItemAccessor> itr = rc.iterator();
			assertTrue(itr.hasNext());
			String text = itr.next().getAtomicValue();
			assertEquals("<print>The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars</print>", text);
			assertFalse(itr.hasNext());
		}
	}
	
}
