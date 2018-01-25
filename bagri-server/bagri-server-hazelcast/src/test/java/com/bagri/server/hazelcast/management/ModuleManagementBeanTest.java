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

public class ModuleManagementBeanTest extends AdminServerTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=ModuleManagement");
	}

	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Modules", "ModuleNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getModules", "getModuleNames", "addModule", "deleteModule"};
	}

	@Test
	public void testGetModuleNames() throws Exception {
		ObjectName name = getObjectName();
		checkExpectedNames("ModuleNames", "hello_module", "mmd_module", "rest_module", "trigger_module");
	}

	@Test
	public void testGetModules() throws Exception {
		ObjectName name = getObjectName();
        TabularData nodes = (TabularData) mbsc.getAttribute(name, "Modules");
        assertNotNull(nodes);
        assertEquals(4, nodes.size());
		List<String> expected = Arrays.asList("hello_module", "mmd_module", "rest_module", "trigger_module");
    	Set<List> keys = (Set<List>) nodes.keySet();
    	for (List key: keys) {
    		Object[] index = key.toArray();
			CompositeData schema = nodes.get(index);
			String sn = (String) schema.get("name");
			assertTrue(expected.contains(sn));
		}
	}

	@Test
	public void testAddDeleteModules() throws Exception {
		ObjectName name = getObjectName();
		Boolean result = (Boolean) mbsc.invoke(name, "addModule", new Object[] {"test_module", "test_module.xq",
				"test XQuery module", "tst", "http://bagridb.com/test"}, new String[] {String.class.getName(), 
				String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()});
		assertTrue(result);
		checkExpectedNames("ModuleNames", "hello_module", "mmd_module", "rest_module", "trigger_module", "test_module");

		result = (Boolean) mbsc.invoke(name, "addModule", new Object[] {"test_module", "test_module.xq",
				"test XQuery module", "tst", "http://bagridb.com/test"}, new String[] {String.class.getName(), 
				String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()});
		assertFalse(result);
        
		result = (Boolean) mbsc.invoke(name, "deleteModule", new Object[] {"test_module"}, new String[] {String.class.getName()});
		assertTrue(result);
		checkExpectedNames("ModuleNames", "hello_module", "mmd_module", "rest_module", "trigger_module");

		result = (Boolean) mbsc.invoke(name, "deleteModule", new Object[] {"test_module"}, new String[] {String.class.getName()});
		assertFalse(result);
	}


}
