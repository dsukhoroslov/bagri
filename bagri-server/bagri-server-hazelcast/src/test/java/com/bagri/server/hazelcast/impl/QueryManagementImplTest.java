package com.bagri.server.hazelcast.impl;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_RESULT;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.core.DocumentKey;
import com.bagri.core.KeyFactory;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.QueryResult;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.server.hazelcast.task.query.QueryProcessor;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class QueryManagementImplTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		//System.setProperty(pn_log_level", "trace");
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
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			schema.setProperty(pn_xqj_baseURI, sampleRoot);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			com.bagri.core.system.Collection collection = new com.bagri.core.system.Collection(1, new Date(), 
					JMXUtils.getCurrentUser(), 1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
		Thread.sleep(1000);
	}

	private ModelManagement getModelManagement() {
		return ((SchemaRepository) xRepo).getModelManagement();
	}

	
	public Collection<String> getPrice(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, Object> params = new HashMap<>();
		params.put(":name", "/" + prefix + ":Security/" + prefix + ":Name/text()");
		params.put(":price", "/" + prefix + ":Security/" + prefix + ":Price/" + prefix + ":PriceToday/" + prefix + ":Open/text()");
		return ((QueryManagement) getQueryManagement()).getContent(ec, "<print>The open price of the security \":name\" is :price dollars</print>", params);
	}
	
	public Collection<String> getSecurity(String symbol) throws Exception {
		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, Object> params = new HashMap<>();
		params.put(":sec", "/" + prefix + ":Security");
		return ((QueryManagement) getQueryManagement()).getContent(ec, ":sec", params);
	}
	
	public Collection<String> getOrder(String id) throws Exception {
		String prefix = "http://www.fixprotocol.org/FIXML-4-4"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":FIXML"); // /" + prefix + ":Order");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "FIXML").
				addPathSegment(AxisType.CHILD, prefix, "Order").
				addPathSegment(AxisType.ATTRIBUTE, null, "ID");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);
		Map<String, Object> params = new HashMap<>();
		params.put(":order", "/" + prefix + ":FIXML/" + prefix + ":Order");
		return ((QueryManagement) getQueryManagement()).getContent(ec, ":order", params);
	}
	
	public Collection<String> getCustomerProfile(String id) throws Exception {
		String prefix = "http://tpox-benchmark.com/custacc"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.ATTRIBUTE, null, "id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);

		String template = "<Customer_Profile CUSTOMERID=\":id\">\n" +
				"\t:name" + 
				"\t:dob" + 
				"\t:gender" + 
				"\t:langs" + 
				"\t:addrs" + 
				"\t:email" + 
			"</Customer_Profile>";
		
		Map<String, Object> params = new HashMap<>();
		params.put(":id", "/" + prefix + ":Customer/@id");
		params.put(":name", "/" + prefix + ":Customer/" + prefix + ":Name");
		params.put(":dob", "/" + prefix + ":Customer/" + prefix + ":DateOfBirth");
		params.put(":gender", "/" + prefix + ":Customer/" + prefix + ":Gender");
		params.put(":langs", "/" + prefix + ":Customer/" + prefix + ":Languages");
		params.put(":addrs", "/" + prefix + ":Customer/" + prefix + ":Addresses");
		params.put(":email", "/" + prefix + ":Customer/" + prefix + ":EmailAddresses");
		return ((QueryManagement) getQueryManagement()).getContent(ec, template, params);
	}
	
	public Collection<String> getCustomerAccounts(String id) throws Exception {
		String prefix = "http://tpox-benchmark.com/custacc"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.ATTRIBUTE, null, "id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);

		String template = "<Customer>:id\n" +
				"\t:name" + 
				"\t<Customer_Securities>\n" +
				"\t\t<Account BALANCE=\":balance\" ACCOUNT_ID=\":accId\">\n" +
				"\t\t\t<Securities>\n" +
				"\t\t\t\t:posName" +
				"\t\t\t</Securities>\n" +
				"\t\t</Account>\n" +
				"\t</Customer_Securities>\n" + 
			"</Customer_Profile>";
		
		Map<String, Object> params = new HashMap<>();
		params.put(":id", "/" + prefix + ":Customer/@id");
		params.put(":name", "/" + prefix + ":Customer/" + prefix + ":Name");
		params.put(":balance", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/" + prefix + ":Balance/" + prefix + ":OnlineActualBal/text()");
		params.put(":accId", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/@id");
		params.put(":posName", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/" + prefix + ":Holdings/" + prefix + ":Position/" + prefix + ":Name");
		return ((QueryManagement) getQueryManagement()).getContent(ec, template, params);
	}
	
	public Collection<String> searchSecurity(String sector, float peMin, float peMax, float yieldMin) throws Exception {

		String prefix = "http://tpox-benchmark.com/security"; 
		int docType = 0; //getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.AND, path);
		ec.addExpression(docType, Comparison.AND, path);
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

        String template = "<Security>\n" +
				"\t:symbol" + 
				"\t:name" + 
				"\t:type" +  
        		//{$sec/SecurityInformation//Sector}
        		//regex = "^/" + prefix + ":Security/" + prefix + ":SecurityInformation/.*/" + prefix + ":Sector$";
        		//Collection<String> sPath = mDictionary.getPathFromRegex(docType, regex);
        		//int idx = 0;
        		//for (String path : sPath) {
        		//	params.put(":sector" + idx, path);
        		//	template += "\t:sector" + idx;
        		//	idx++;
        		//}
				"\t:sector" +  
        		"\t:pe" +
        		"\t:yield" +
		"</Security>";

		Map<String, Object> params = new HashMap<>();
		params.put(":symbol", "/" + prefix + ":Security/" + prefix + ":Symbol");
		params.put(":name", "/" + prefix + ":Security/" + prefix + ":Name");
		params.put(":type", "/" + prefix + ":Security/" + prefix + ":SecurityType");
		params.put(":sector", "/" + prefix + ":Security/" + prefix + ":SecurityInformation//" + prefix + ":Sector");
		params.put(":pe", "/" + prefix + ":Security/" + prefix + ":PE");
   		params.put(":yield", "/" + prefix + ":Security/" + prefix + ":Yield");
   		return ((QueryManagement) getQueryManagement()).getContent(ec, template, params);
	}
	
	@Test
	public void getPriceTest() throws Exception {
		storeSecurityTest();

		Collection<String> sec = getPrice("VFINX");
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

		Collection<String> sec = getSecurity("VFINX");
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

		Collection<String> sec = searchSecurity("Technology", 25, 28, 0);
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
		Collection<String> sec = getOrder("103404");
		assertNotNull(sec);
		assertEquals(1, sec.size());
		sec = getOrder("103935");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}

	@Test
	public void getCustomerProfileTest() throws Exception {
		storeCustomerTest();
		Collection<String> sec = getCustomerProfile("1011");
		assertNotNull(sec);
		assertEquals(1, sec.size());
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		storeCustomerTest();
		Collection<String> sec = getCustomerAccounts("1011");
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
			try (ResultCursor rc = query(query, params, props)) {
				assertNotNull(rc);
				assertTrue(rc.next());
			}
		}
	}
	
	@Test
	public void compareSequrityPriceTest() throws Exception {
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
		Properties props = new Properties();
		props.setProperty(pn_client_id, "1");
		props.setProperty(pn_client_fetchSize, "5");
		props.setProperty(pn_xqj_defaultElementTypeNamespace, "");
		ResultCursor rc = query(query, params, props);
		assertNotNull(rc);
		assertTrue(rc.next());
		XQProcessor xqp = ((SchemaRepositoryImpl) xRepo).getXQProcessor();
		Object result = rc.getObject();
		assertNotNull(result);
		String text = xqp.convertToString(result, null);
		assertEquals("<print>The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars</print>", text);
		assertFalse(rc.next());
		props = new Properties();
		//props.setProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
		//props.setProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
		props.setProperty(javax.xml.transform.OutputKeys.METHOD, "text");
		text = xqp.convertToString(result, props);
		assertEquals("The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars", text);
		rc.close();
	}
	
	@Test
	@Ignore
	public void processSequrityQueryTest() throws Exception {
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
		Properties props = new Properties();
		//props.setProperty(pn_client_id, "1");
		//props.setProperty(pn_client_fetchSize, "5");
		//props.setProperty(pn_xqj_defaultElementTypeNamespace, "");
		KeyFactory f = context.getBean(KeyFactory.class);
		HazelcastInstance hz = context.getBean(HazelcastInstance.class);
		IMap qrCache = hz.getMap(CN_XDM_RESULT);
		QueryProcessor qp = new QueryProcessor(true, query, params, props);
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

		ResultCursor rc = (ResultCursor) qrCache.executeOnKey(dk.getHash(), qp);
		assertNotNull(rc);
		assertTrue(rc.next());
		String text = rc.getString();
		assertEquals("<print>The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars</print>", text);
		assertFalse(rc.next());
		rc.close();
	}
}
