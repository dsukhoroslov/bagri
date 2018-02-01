package com.bagri.server.hazelcast.management;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

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

	protected String[] getExpectedAttributes() {
		return new String[0];
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
        String[] expected = getExpectedAttributes();
        assertEquals(expected.length, attrs.length);
        List<String> exList = Arrays.asList(expected);
        for (MBeanAttributeInfo attr: attrs) {
        	assertTrue(exList.contains(attr.getName()));
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
        	assertTrue(exList.contains(op.getName()));
        }
	}
	
	
}
