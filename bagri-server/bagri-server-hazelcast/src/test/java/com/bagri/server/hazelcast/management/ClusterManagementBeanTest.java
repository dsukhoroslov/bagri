package com.bagri.server.hazelcast.management;

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
	protected String[] getExpectedAttributes() {
		return new String[] {"Nodes", "NodeNames"};
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
