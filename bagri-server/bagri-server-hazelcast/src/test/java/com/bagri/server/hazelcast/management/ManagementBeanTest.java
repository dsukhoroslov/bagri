package com.bagri.server.hazelcast.management;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.Test;

public abstract class ManagementBeanTest {

    protected static MBeanServerConnection mbsc;

	protected abstract ObjectName getObjectName() throws MalformedObjectNameException;

	protected Map<String, Object> getExpectedAttributes() {
		return Collections.emptyMap();
	}

	protected String[] getExpectedOperations() {
		return new String[0];
	}

	@Test
	public void testManagementAttributes() throws Exception {
        ObjectName oName = getObjectName();
        MBeanInfo mbi = mbsc.getMBeanInfo(oName);
        assertNotNull(mbi);
        MBeanAttributeInfo[] attrs = mbi.getAttributes();
        Map<String, Object> expected = getExpectedAttributes();
        assertEquals(expected.size(), attrs.length);
        for (MBeanAttributeInfo attr: attrs) {
        	assertTrue("not found attribute: " + attr.getName(), expected.containsKey(attr.getName()));
        }
        for (Map.Entry<String, Object> attr: expected.entrySet()) {
        	Object o = mbsc.getAttribute(oName, attr.getKey());
        	if (attr.getValue() != null) {
        		assertEquals(attr.getValue(), o);
        	} else {
        		System.out.println("attribute: " + attr.getKey() + " value: " + o);
        	}
        }
	}
	
	@Test
	public void testManagementOperations() throws Exception {
        ObjectName oName = getObjectName();
        MBeanInfo mbi = mbsc.getMBeanInfo(oName);
        assertNotNull(mbi);
        MBeanOperationInfo[] ops = mbi.getOperations();
        String[] expected = getExpectedOperations();
        assertEquals(expected.length, ops.length);
        List<String> exList = Arrays.asList(expected);
        for (MBeanOperationInfo op: ops) {
        	assertTrue("not found operation: " + op.getName(), exList.contains(op.getName()));
        }
	}
	
	
}
