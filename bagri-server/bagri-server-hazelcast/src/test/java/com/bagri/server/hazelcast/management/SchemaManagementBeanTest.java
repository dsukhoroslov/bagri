package com.bagri.server.hazelcast.management;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.support.util.JMXUtils;

public class SchemaManagementBeanTest extends AdminServerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        mbsc = startAdminServer();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopAdminServer();
	}
	
	//@Before
	//public void setUp() throws Exception {
    //    mbsc = startAdminServer();
	//}

	//@After
	//public void tearDown() throws Exception {
	//	stopAdminServer();
	//}
	
	@Override
	protected ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.bagri.db:type=Management,name=SchemaManagement");
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Schemas", "SchemaNames", "DefaultProperties"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getSchemas", "getSchemaNames", "getDefaultProperties", "setDefaultProperty", "addSchema", "deleteSchema"};
	}

	@Test
	public void testGetSchemaNames() throws Exception {
        ObjectName oName = getObjectName();
        String[] names = (String[]) mbsc.getAttribute(oName, "SchemaNames");
		assertEquals(6, names.length);
		List<String> expected = Arrays.asList("XMark", "TPoX", "XDM", "TPoX-J", "YCSB", "default");
		for (String sn: names) {
			assertTrue(expected.contains(sn));
		}
	}

	@Test
	public void testGetSchemas() throws Exception {
        ObjectName oName = getObjectName();
        TabularData schemas = (TabularData) mbsc.getAttribute(oName, "Schemas");
		assertNotNull(schemas);
		assertEquals(6, schemas.size());
		List<String> expected = Arrays.asList("XMark", "TPoX", "XDM", "TPoX-J", "YCSB", "default");
    	Set<List> keys = (Set<List>) schemas.keySet();
    	for (List key: keys) {
    		Object[] index = key.toArray();
			CompositeData schema = schemas.get(index);
			String sn = (String) schema.get("name");
			assertTrue(expected.contains(sn));
		}
	}

}
