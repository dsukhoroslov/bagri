package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.common.XDMConstants.xdm_config_path;
import static com.bagri.xdm.common.XDMConstants.xdm_config_properties_file;
import static com.bagri.xdm.common.XDMConstants.pn_baseURI;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQItem;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.api.test.ClientQueryManagementTest;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xquery.api.XQProcessor;

//@Ignore
public class TpoxQueryTest extends ClientQueryManagementTest {

    private static ClassPathXmlApplicationContext context;
	private static final String[] aNames = new String[] {"Vanguard 500 Index Fund", "Internatinal Business Machines Corporation", "PIMCO Total Return A"};
	private static final List<String> names = Arrays.asList(aNames); 
	
    private XQProcessor xqProc; 

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
		xRepo = context.getBean(RepositoryImpl.class);
		RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
		xqProc = context.getBean("xqProcessor", XQProcessor.class);
		XDMSchema schema = xdmRepo.getSchema();
		if (schema == null) {
			schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
			schema.setProperty(pn_baseURI, sampleRoot);
			XDMCollection collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
					1, "CLN_Security", "/{http://tpox-benchmark.com/security}Security", "securities", true);
			schema.addCollection(collection);
			collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
					2, "CLN_Customer", "/{http://tpox-benchmark.com/custacc}Customer", "customers", true);
			schema.addCollection(collection);
			collection = new XDMCollection(1, new Date(), JMXUtils.getCurrentUser(), 
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
		Iterator<?> sec = getPrice("VFINX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.next();
		String text = item.getItemAsString(props);
		assertEquals("The open price of the security \"Vanguard 500 Index Fund\" is 101.12 dollars", text);

		sec = getPrice("IBM");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		text = item.getItemAsString(props);
		assertEquals("The open price of the security \"Internatinal Business Machines Corporation\" is 86.23 dollars", text);

		sec = getPrice("PTTAX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		text = item.getItemAsString(props);
		assertEquals("The open price of the security \"PIMCO Total Return A\" is 36.23 dollars", text);
	}

	@Test
	public void getSecurityTest() throws Exception {
		Iterator<?> sec = getSecurity("VFINX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		assertEquals(4289, xml.length());
		assertFalse(sec.hasNext());

		sec = getSecurity("IBM");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		xml = item.getItemAsString(null);
		assertEquals(3502, xml.length());
		assertFalse(sec.hasNext());

		sec = getSecurity("PTTAX");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		xml = item.getItemAsString(null);
		assertEquals(2774, xml.length());
		assertFalse(sec.hasNext());
	}

	@Test
	public void searchSecurityTest() throws Exception {
		Iterator<?> sec = searchSecurity("Technology", 25, 28, 0);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		//<Symbol>IBM</Symbol>
		//<Name>Internatinal Business Machines Corporation</Name>
		//<SecurityType>Stock</SecurityType>
		//<Sector>Technology</Sector>
		//<PE>27.13</PE>
		//<Yield>0.74</Yield>

		sec = searchSecurity("Technology", 25, 28, 1);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertFalse(sec.hasNext());

		sec = searchSecurity("Technology", 28, 29, 0);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertFalse(sec.hasNext());
	}

	@Test
	public void getOrderTest() throws Exception {
		Iterator<?> sec = getOrder("103404");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		assertEquals(1990, xml.length());
		assertFalse(sec.hasNext());

		sec = getOrder("103935");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		xml = item.getItemAsString(null);
		assertEquals(2041, xml.length());
		assertFalse(sec.hasNext());
	}

	@Test
	public void getCustomerTest() throws Exception {
		Iterator<?> sec = getCustomer("1011");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		assertEquals(6699, xml.length());
		assertFalse(sec.hasNext());
	}
	
	@Test
	public void getCustomerProfileTest() throws Exception {
		Iterator<?> sec = getCustomerProfile("1011");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		assertEquals(2048, xml.length());
		assertFalse(sec.hasNext());
	}

	@Test
	public void getCustomerAccountsTest() throws Exception {
		Iterator<?> sec = getCustomerAccounts("1011");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		assertEquals(775, xml.length());
		assertFalse(sec.hasNext());
	}
	
	@Test
	public void getTodayOrderPriceTest() throws Exception {
		Iterator<?> sec = getTodayOrderPrice("103935");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.next();
		String text = item.getItemAsString(props);
		assertEquals("164230.5448", text.trim());
		assertFalse(sec.hasNext());
	}

	@Test
	public void getOrderCustomersTest() throws Exception {
		Iterator<?> sec = getOrderCustomers(2000, "Portugal");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.next();
		String text = item.getItemAsString(props);
		assertEquals("Marjo Villoldo", text);
		assertFalse(sec.hasNext());
	}

	@Test
	public void getCustomerByAddressTest() throws Exception {
		Iterator<?> sec = getCustomerByAddress(56137, 1, true);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String xml = item.getItemAsString(null);
		assertFalse(sec.hasNext());
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
		Iterator<?> sec = getMaxOrderPrice(1011);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String price = item.getItemAsString(null);
		assertEquals("1479.06", price);
		assertFalse(sec.hasNext());
	}

	@Test
	public void getMaxIndustryOrderTest() throws Exception {
		Iterator<?> sec = getMaxOrderForIndustry("ComputerHardware", "California");
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		XQItem item = (XQItem) sec.next();
		String price = item.getItemAsString(null);
		assertEquals("1479.06", price);
		assertFalse(sec.hasNext());
	}
	
	@Test
	public void getCustomerSecuritiesTest() throws Exception {
		Iterator<?> sec = getCustomerSecurities(1011);
		assertNotNull(sec);
		((ResultCursor) sec).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		assertTrue(sec.hasNext());
		Properties props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) sec.next();
		String text = item.getItemAsString(props);
		assertTrue("unknown name: '" + text + "'", names.contains(text));

		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		text = item.getItemAsString(props);
		assertTrue("unknown name: '" + text + "'", names.contains(text));
		
		assertTrue(sec.hasNext());
		item = (XQItem) sec.next();
		text = item.getItemAsString(props);
		assertTrue("unknown name: '" + text + "'", names.contains(text));
		assertFalse(sec.hasNext());
	}
	
	@Test
	public void getDistinctIndustriesTest() throws Exception {
	
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" + 
				"for $ind in distinct-values(collection(\"CLN_Security\")/s:Security/s:SecurityInformation/*/s:Industry)\n" + 
				"return $ind";
		Iterator<?> ind = getQueryManagement().executeQuery(query, null, new Properties());
		assertNotNull(ind);
		((ResultCursor) ind).deserialize(((RepositoryImpl) xRepo).getHzInstance());
		Properties props = new Properties();
		props.setProperty("method", "text");
		List<String> industries = new ArrayList<>();
		while (ind.hasNext()) {
			XQItem item = (XQItem) ind.next();
			String text = item.getItemAsString(props);
			industries.add(text);
		}
		assertEquals(10, industries.size());
	}
	
}
