package com.bagri.server.hazelcast.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.junit.Test;

public abstract class EntityManagementBeanTest {

    protected static MBeanServerConnection mbsc;

	protected abstract ObjectName getObjectName() throws MalformedObjectNameException;

	protected String[] getExpectedAttributes() {
		return new String[0];
	}

	protected String[] getExpectedOperations() {
		return new String[0];
	}

	protected String[] getExpectedEntities() {
		return new String[0];
	}
	
	protected abstract String getEntityType();
	protected abstract Object[] getAddEntityParams();
	protected abstract String[] getAddEntityParamClasses();
	
	protected void checkExpectedNames(String aName, String... expected) throws Exception {
        ObjectName oName = getObjectName();
        String[] names = (String[]) mbsc.getAttribute(oName, aName);
        assertEquals(expected.length, names.length);
		List<String> exList = Arrays.asList(expected);
		for (String name: names) {
			assertTrue(exList.contains(name));
		}
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
	
	@Test
	public void testGetEntityNames() throws Exception {
		String[] names = getExpectedAttributes();
		checkExpectedNames(names[1], getExpectedEntities());
	}

	@Test
	public void testGetEntities() throws Exception {
		ObjectName name = getObjectName();
		String[] names = getExpectedAttributes();
        TabularData entities = (TabularData) mbsc.getAttribute(name, names[0]);
        assertNotNull(entities);
		List<String> expected = Arrays.asList(getExpectedEntities());
        assertEquals(expected.size(), entities.size());
    	Set<List> keys = (Set<List>) entities.keySet();
    	for (List key: keys) {
    		Object[] index = key.toArray();
			CompositeData schema = entities.get(index);
			String sn = (String) schema.get("name");
			assertTrue(expected.contains(sn));
		}
	}
	
	@Test
	public void testAddDeleteEntities() throws Exception {
		ObjectName oName = getObjectName();
		String[] methods = getExpectedOperations(); 
		Object[] params = getAddEntityParams();
		String eName = (String) params[0]; 
		String[] classes = getAddEntityParamClasses();
		assertEquals(params.length, classes.length);
		Boolean result = (Boolean) mbsc.invoke(oName, methods[2], params, classes);  
		assertTrue(result);
		String[] original = getExpectedEntities();
		String[] added = Arrays.copyOf(original, original.length + 1);
		added[original.length] = eName;
		String[] attrs = getExpectedAttributes();
		checkExpectedNames(attrs[1], added);

		// check entity properties here..
		
		result = (Boolean) mbsc.invoke(oName, methods[2], params, classes);  
		assertFalse(result);
        
		result = (Boolean) mbsc.invoke(oName, methods[3], new Object[] {eName}, new String[] {String.class.getName()});
		assertTrue(result);
		checkExpectedNames(attrs[1], original);

		result = (Boolean) mbsc.invoke(oName, methods[3], new Object[] {eName}, new String[] {String.class.getName()});
		assertFalse(result);
	}

	@Test
	public void testEntityManagers() throws Exception {
		for (String eName: getExpectedEntities()) {
			ObjectName oName = new ObjectName("com.bagri.db:name=" + eName + ",type=" + getEntityType());
	        String name = (String) mbsc.getAttribute(oName, "Name");
			assertEquals(eName, name);
			// test expected manager attributes
			// test expected manager operations
		}
	}

}
