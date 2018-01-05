package com.bagri.server.hazelcast.management;

import static org.junit.Assert.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

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
        Object nodes = mbsc.getAttribute(name, "NodeNames");
        //System.out.println("got nodes: " + Arrays.toString((String[]) nodes));
        String[] sNodes = (String[]) nodes;
        assertTrue(sNodes.length > 0);
	}

	//private boolean containsDomain(String[] domains, String domain) {
	//	for (String d : domains) {
	//		if (d.equals(domain)) return true;
	//	}
	//	return false;
	//}

}
