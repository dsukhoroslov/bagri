package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ClusterManagementBeanTest extends EntityManagementBeanTest {
	
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
	protected String getEntityType() {
		return "Node";
	}
	
	@Override
	protected Map<String, Object> getExpectedAttributes() {
		Map<String, Object> map = new HashMap<>(2);
		map.put("Nodes", null);
		map.put("NodeNames", null);
		return map;
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getNodes", "getNodeNames", "addNode", "deleteNode"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"admin", "cache"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"rest", "bdb.cluster.node.role=rest"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName()};
	}
	
}
