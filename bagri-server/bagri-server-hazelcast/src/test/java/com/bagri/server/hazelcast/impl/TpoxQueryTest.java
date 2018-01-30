package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.ClientQueryManagementTest;
import com.bagri.support.util.JMXUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.xquery.XQItemAccessor;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

//@Ignore
public class TpoxQueryTest extends ClientQueryManagementTest {

    private static ClassPathXmlApplicationContext context;
	private static final String[] aNames = new String[] {"Vanguard 500 Index Fund", "Internatinal Business Machines Corporation", "PIMCO Total Return A"};
	private static final List<String> names = Arrays.asList(aNames); 
	
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
		Thread.sleep(3000);
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
			Collection collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
			collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					2, "CLN_Customer", "/{http://tpox-benchmark.com/custacc}Customer", "customers", true);
			schema.addCollection(collection);
			collection = new Collection(1, new Date(), JMXUtils.getCurrentUser(), 
					3, "CLN_Order", "/{http://www.fixprotocol.org/FIXML-4-4}FIXML", "orders", true);
			schema.addCollection(collection);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
			storeCustomerTest();
			storeOrderTest();
			storeSecurityTest();
		}
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void getPriceTest() throws Exception {
		checkCursorResult(getPrice("VFINX"), "The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars");
		checkCursorResult(getPrice("IBM"), "The open price of the security \"Internatinal Business Machines Corporation\" is 86.23 dollars");
		checkCursorResult(getPrice("PTTAX"), "The open price of the security \"PIMCO Total Return A\" is 36.23 dollars");
	}

	@Test
	public void getSecurityTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getSecurity("VFINX")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(4289, xml.length());
			assertFalse(itr.hasNext());
		}

		try (ResultCursor<XQItemAccessor> sec = getSecurity("IBM")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(3502, xml.length());
			assertFalse(itr.hasNext());
		}

		try (ResultCursor<XQItemAccessor> sec = getSecurity("PTTAX")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(2774, xml.length());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void searchSecurityTest() throws Exception {
		checkCursorResult(searchSecurity("Technology", 25, 28, 0), null);
		
		try (ResultCursor<XQItemAccessor> sec = searchSecurity("Technology", 25, 28, 1)) {
			assertNotNull(sec);
			assertTrue(sec.isEmpty());
		}

		try (ResultCursor<XQItemAccessor> sec = searchSecurity("Technology", 28, 29, 0)) {
			assertNotNull(sec);
			assertTrue(sec.isEmpty());
		}
	}

	@Test
	public void getOrderTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getOrder("103404")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(1990, xml.length());
			assertFalse(itr.hasNext());
		}

		try (ResultCursor<XQItemAccessor> sec = getOrder("103935")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(2041, xml.length());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getCustomerTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getCustomer("1011")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(6699, xml.length());
			assertFalse(itr.hasNext());
		}
	}
	
	@Test
	public void getCustomerProfileTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getCustomerProfile("1011")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(2048, xml.length());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getCustomerAccounts("1011")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			String xml = itr.next().getItemAsString(null);
			assertEquals(775, xml.length());
			assertFalse(itr.hasNext());
		}
	}
	
	@Test
	public void getTodayOrderPriceTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getTodayOrderPrice("103935")) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			Properties props = new Properties();
			props.setProperty("method", "text");
			String text = itr.next().getItemAsString(props);
			assertEquals("164230.5448", text.trim());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getOrderCustomersTest() throws Exception {
		checkCursorResult(getOrderCustomers(2000, "Portugal"), "Marjo Villoldo"); 
	}

	@Test
	public void getCustomerByAddressTest() throws Exception {
		checkCursorResult(getCustomerByAddress(56137, 1, true), null); 
		//<Customer xmlns="http://www.fixprotocol.org/FIXML-4-4">
		//Villoldo - <Phone xmlns="http://tpox-benchmark.com/custacc"
        //  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        //  primary="Yes"
        //  type="Temporary">
		//			<CountryCode>196</CountryCode>
		//			<AreaCode>248</AreaCode>
		//			<Number>4744196</Number>
		//		</Phone>
		//</Customer>
	}
	
	@Test
	public void getMaxOrderPriceTest() throws Exception {
		checkCursorResult(getMaxOrderPrice(1011), "1479.06"); 
	}

	@Test
	public void getMaxIndustryOrderTest() throws Exception {
		checkCursorResult(getMaxOrderForIndustry("ComputerHardware", "California"), "1479.06"); 
	}
	
	@Test
	public void getCustomerSecuritiesTest() throws Exception {
		try (ResultCursor<XQItemAccessor> sec = getCustomerSecurities(1011)) {
			assertNotNull(sec);
			Iterator<XQItemAccessor> itr = sec.iterator();
			assertTrue(itr.hasNext());
			Properties props = new Properties();
			props.setProperty("method", "text");
			String text = itr.next().getItemAsString(props);
			assertTrue("unknown name: '" + text + "'", names.contains(text));
	
			assertTrue(itr.hasNext());
			text = itr.next().getItemAsString(props);
			assertTrue("unknown name: '" + text + "'", names.contains(text));
			
			assertTrue(itr.hasNext());
			text = itr.next().getItemAsString(props);
			assertTrue("unknown name: '" + text + "'", names.contains(text));
			assertFalse(itr.hasNext());
		}
	}
	
	@Test
	public void getDistinctIndustriesTest() throws Exception {
	
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" + 
				"for $ind in distinct-values(collection(\"CLN_Security\")/s:Security/s:SecurityInformation/*/s:Industry)\n" + 
				"return $ind";
		try (ResultCursor<XQItemAccessor> ind = query(query, null, null)) {
			assertNotNull(ind);
			Properties props = new Properties();
			props.setProperty("method", "text");
			List<String> industries = new ArrayList<>();
			for (XQItemAccessor item: ind) {
				String text = item.getItemAsString(props);
				industries.add(text);
			}
			assertEquals(10, industries.size());
		}
	}
	
}
