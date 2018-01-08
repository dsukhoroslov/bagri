package com.bagri.server.hazelcast.management;

import static org.junit.Assert.*;

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

public class ClusterManagementBeanTest extends AdminServerTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=ClusterManagement");
	}

	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Nodes", "NodeNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getNodes", "getNodeNames", "addNode", "deleteNode"};
	}

	@Test
	public void testGetNodeNames() throws Exception {
		ObjectName name = getObjectName();
		checkExpectedNames("NodeNames", "admin", "cache");
	}

	@Test
	public void testGetNodes() throws Exception {
		ObjectName name = getObjectName();
        TabularData nodes = (TabularData) mbsc.getAttribute(name, "Nodes");
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
		List<String> expected = Arrays.asList("admin", "cache");
    	Set<List> keys = (Set<List>) nodes.keySet();
    	for (List key: keys) {
    		Object[] index = key.toArray();
			CompositeData schema = nodes.get(index);
			String sn = (String) schema.get("name");
			assertTrue(expected.contains(sn));
		}
	}

	@Test
	public void testAddDeleteNode() throws Exception {
		ObjectName name = getObjectName();
		Boolean result = (Boolean) mbsc.invoke(name, "addNode", new Object[] {"rest", "bdb.cluster.node.role=rest"}, 
				new String[] {String.class.getName(), String.class.getName()});
		assertTrue(result);
		checkExpectedNames("NodeNames", "admin", "cache", "rest");

		result = (Boolean) mbsc.invoke(name, "addNode", new Object[] {"rest", "bdb.cluster.node.role=rest"}, 
				new String[] {String.class.getName(), String.class.getName()});
		assertFalse(result);
        
		result = (Boolean) mbsc.invoke(name, "deleteNode", new Object[] {"rest"}, new String[] {String.class.getName()});
		assertTrue(result);
		checkExpectedNames("NodeNames", "admin", "cache");

        result = (Boolean) mbsc.invoke(name, "deleteNode", new Object[] {"rest"}, new String[] {String.class.getName()});
		assertFalse(result);
	}

	
}
