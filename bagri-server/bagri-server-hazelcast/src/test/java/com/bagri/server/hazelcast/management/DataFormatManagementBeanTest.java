package com.bagri.server.hazelcast.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataFormatManagementBeanTest extends AdminServerTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        mbsc = startAdminServer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopAdminServer();
	}
	
	@Override
	protected ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.bagri.db:type=Management,name=DataFormatManagement");
	}

	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"DataFormats", "DataFormatNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getDataFormats", "getDataFormatNames", "addDataFormat", "deleteDataFormat"};
	}

	@Test
	public void testGetDataFormatNames() throws Exception {
		ObjectName name = getObjectName();
		checkExpectedNames("DataFormatNames", "XML", "JSON", "MAP", "BMAP", "SMAP");
	}

	@Test
	public void testGetDataFormats() throws Exception {
		ObjectName name = getObjectName();
        TabularData nodes = (TabularData) mbsc.getAttribute(name, "DataFormats");
        assertNotNull(nodes);
        assertEquals(5, nodes.size());
		List<String> expected = Arrays.asList("XML", "JSON", "MAP", "SMAP", "BMAP");
    	Set<List> keys = (Set<List>) nodes.keySet();
    	for (List key: keys) {
    		Object[] index = key.toArray();
			CompositeData schema = nodes.get(index);
			String sn = (String) schema.get("name");
			assertTrue(expected.contains(sn));
		}
	}

	@Test
	public void testAddDeleteDataFormat() throws Exception {
		ObjectName name = getObjectName();
		Boolean result = (Boolean) mbsc.invoke(name, "addDataFormat", new Object[] {"CSV", "com.bagri.core.server.api.df.csv.CsvHandler",
				"CSV format handler", null, "csv", "bdb.schema.parser.csv.quote=true"},	new String[] {String.class.getName(), 
				String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()});
		assertTrue(result);
		checkExpectedNames("DataFormatNames", "XML", "JSON", "MAP", "SMAP", "BMAP", "CSV");

		result = (Boolean) mbsc.invoke(name, "addDataFormat", new Object[] {"CSV", "com.bagri.core.server.api.df.csv.CsvHandler",
				"CSV format handler", null, "csv", "bdb.schema.parser.csv.quote=true"},	new String[] {String.class.getName(), 
				String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()});
		assertFalse(result);
        
		result = (Boolean) mbsc.invoke(name, "deleteDataFormat", new Object[] {"CSV"}, new String[] {String.class.getName()});
		assertTrue(result);
		checkExpectedNames("DataFormatNames", "XML", "JSON", "MAP", "SMAP", "BMAP");

		result = (Boolean) mbsc.invoke(name, "deleteDataFormat", new Object[] {"CSV"}, new String[] {String.class.getName()});
		assertFalse(result);
	}


}
