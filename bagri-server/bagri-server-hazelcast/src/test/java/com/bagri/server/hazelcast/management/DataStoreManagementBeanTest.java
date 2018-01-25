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

public class DataStoreManagementBeanTest extends AdminServerTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=DataStoreManagement");
	}

	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"DataStores", "DataStoreNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getDataStores", "getDataStoreNames", "addDataStore", "deleteDataStore"};
	}

	@Test
	public void testGetDataStoreNames() throws Exception {
		ObjectName name = getObjectName();
		checkExpectedNames("DataStoreNames", "File");
	}

	@Test
	public void testGetDataStores() throws Exception {
		ObjectName name = getObjectName();
        TabularData nodes = (TabularData) mbsc.getAttribute(name, "DataStores");
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
		List<String> expected = Arrays.asList("File");
    	Set<List> keys = (Set<List>) nodes.keySet();
    	for (List key: keys) {
    		Object[] index = key.toArray();
			CompositeData schema = nodes.get(index);
			String sn = (String) schema.get("name");
			assertTrue(expected.contains(sn));
		}
	}

	@Test
	public void testAddDeleteDataStore() throws Exception {
		ObjectName name = getObjectName();
		Boolean result = (Boolean) mbsc.invoke(name, "addDataStore", new Object[] {"JDBC", "com.bagri.ext.store.JdbcStore",
				"JDBC data-store plugin", "jdbc.url=jdbc:postgresql://localhost/world;jdbc.driverClassName=org.postgresql.Driver;" + 
				"jdbc.username=postgres;jdbc.password=postgres"}, new String[] {String.class.getName(), 
				String.class.getName(), String.class.getName(), String.class.getName()});
		assertTrue(result);
		checkExpectedNames("DataStoreNames", "File", "JDBC");

		result = (Boolean) mbsc.invoke(name, "addDataStore", new Object[] {"JDBC", "com.bagri.ext.store.JdbcStore",
				"JDBC data-store plugin", "jdbc.url=jdbc:postgresql://localhost/world;jdbc.driverClassName=org.postgresql.Driver;" + 
				"jdbc.username=postgres;jdbc.password=postgres"}, new String[] {String.class.getName(), 
				String.class.getName(), String.class.getName(), String.class.getName()});
		assertFalse(result);
        
		result = (Boolean) mbsc.invoke(name, "deleteDataStore", new Object[] {"JDBC"}, new String[] {String.class.getName()});
		assertTrue(result);
		checkExpectedNames("DataStoreNames", "File");

		result = (Boolean) mbsc.invoke(name, "deleteDataStore", new Object[] {"JDBC"}, new String[] {String.class.getName()});
		assertFalse(result);
	}


}
