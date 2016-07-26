package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.Constants.xdm_config_path;
import static com.bagri.xdm.common.Constants.xdm_config_properties_file;
import static com.bagri.xdm.common.Constants.pn_baseURI;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.xquery.XQItem;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.test.ClientQueryManagementTest;
import com.bagri.xdm.system.Collection;
import com.bagri.xdm.system.Schema;

//@Ignore
public class TpoxQueryTest extends ClientQueryManagementTest {

    private static ClassPathXmlApplicationContext context;
	private static final String[] aNames = new String[] {"Vanguard 500 Index Fund", "Internatinal Business Machines Corporation", "PIMCO Total Return A"};
	private static final List<String> names = Arrays.asList(aNames); 
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\tpox\\";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		System.setProperty(xdm_config_path, "src\\test\\resources");
		//context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
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
			schema.setProperty(pn_baseURI, sampleRoot);
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
		ResultCursor sec = getPrice("VFINX");
		assertNotNull(sec);
		assertTrue(sec.next());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars", text);
		sec.close();

		sec = getPrice("IBM");
		assertNotNull(sec);
		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		text = item.getItemAsString(props);
		assertEquals("The open price of the security \"Internatinal Business Machines Corporation\" is 86.23 dollars", text);
		sec.close();

		sec = getPrice("PTTAX");
		assertNotNull(sec);
		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		text = item.getItemAsString(props);
		assertEquals("The open price of the security \"PIMCO Total Return A\" is 36.23 dollars", text);
		sec.close();
	}

	@Test
	public void getSecurityTest() throws Exception {
		ResultCursor sec = getSecurity("VFINX");
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		assertEquals(4289, xml.length());
		assertFalse(sec.next());
		sec.close();

		sec = getSecurity("IBM");
		assertNotNull(sec);
		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		xml = item.getItemAsString(null);
		assertEquals(3502, xml.length());
		assertFalse(sec.next());
		sec.close();

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		xml = item.getItemAsString(null);
		assertEquals(2774, xml.length());
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void searchSecurityTest() throws Exception {
		ResultCursor sec = searchSecurity("Technology", 25, 28, 0);
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		//<Symbol>IBM</Symbol>
		//<Name>Internatinal Business Machines Corporation</Name>
		//<SecurityType>Stock</SecurityType>
		//<Sector>Technology</Sector>
		//<PE>27.13</PE>
		//<Yield>0.74</Yield>
		sec.close();

		sec = searchSecurity("Technology", 25, 28, 1);
		assertNotNull(sec);
		assertFalse(sec.next());
		sec.close();

		sec = searchSecurity("Technology", 28, 29, 0);
		assertNotNull(sec);
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getOrderTest() throws Exception {
		ResultCursor sec = getOrder("103404");
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		assertEquals(1990, xml.length());
		assertFalse(sec.next());
		sec.close();

		sec = getOrder("103935");
		assertNotNull(sec);
		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		xml = item.getItemAsString(null);
		assertEquals(2041, xml.length());
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getCustomerTest() throws Exception {
		ResultCursor sec = getCustomer("1011");
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		assertEquals(6699, xml.length());
		assertFalse(sec.next());
		sec.close();
	}
	
	@Test
	public void getCustomerProfileTest() throws Exception {
		ResultCursor sec = getCustomerProfile("1011");
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		assertEquals(2048, xml.length());
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		ResultCursor sec = getCustomerAccounts("1011");
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		assertEquals(775, xml.length());
		assertFalse(sec.next());
		sec.close();
	}
	
	@Test
	public void getTodayOrderPriceTest() throws Exception {
		ResultCursor sec = getTodayOrderPrice("103935");
		assertNotNull(sec);
		assertTrue(sec.next());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("164230.5448", text.trim());
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getOrderCustomersTest() throws Exception {
		ResultCursor sec = getOrderCustomers(2000, "Portugal");
		assertNotNull(sec);
		assertTrue(sec.next());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("Marjo Villoldo", text);
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getCustomerByAddressTest() throws Exception {
		ResultCursor sec = getCustomerByAddress(56137, 1, true);
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String xml = item.getItemAsString(null);
		assertFalse(sec.next());
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
		sec.close();
	}
	
	@Test
	public void getMaxOrderPriceTest() throws Exception {
		ResultCursor sec = getMaxOrderPrice(1011);
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String price = item.getItemAsString(null);
		assertEquals("1479.06", price);
		assertFalse(sec.next());
		sec.close();
	}

	@Test
	public void getMaxIndustryOrderTest() throws Exception {
		ResultCursor sec = getMaxOrderForIndustry("ComputerHardware", "California");
		assertNotNull(sec);
		assertTrue(sec.next());
		XQItem item = (XQItem) sec.getXQItem();
		String price = item.getItemAsString(null);
		assertEquals("1479.06", price);
		assertFalse(sec.next());
		sec.close();
	}
	
	@Test
	public void getCustomerSecuritiesTest() throws Exception {
		ResultCursor sec = getCustomerSecurities(1011);
		assertNotNull(sec);
		assertTrue(sec.next());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.getXQItem();
		String text = item.getItemAsString(props);
		assertTrue("unknown name: '" + text + "'", names.contains(text));

		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		text = item.getItemAsString(props);
		assertTrue("unknown name: '" + text + "'", names.contains(text));
		
		assertTrue(sec.next());
		item = (XQItem) sec.getXQItem();
		text = item.getItemAsString(props);
		assertTrue("unknown name: '" + text + "'", names.contains(text));
		assertFalse(sec.next());
		sec.close();
	}
	
	@Test
	public void getDistinctIndustriesTest() throws Exception {
	
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" + 
				"for $ind in distinct-values(collection(\"CLN_Security\")/s:Security/s:SecurityInformation/*/s:Industry)\n" + 
				"return $ind";
		ResultCursor ind = query(query, null, null);
		assertNotNull(ind);
		Properties props = new Properties();
		props.setProperty("method", "text");
		List<String> industries = new ArrayList<>();
		while (ind.next()) {
			XQItem item = (XQItem) ind.getXQItem();
			String text = item.getItemAsString(props);
			industries.add(text);
		}
		assertEquals(10, industries.size());
		ind.close();
	}
	
}
