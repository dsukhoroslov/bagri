package com.bagri.server.hazelcast.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.junit.Test;

public abstract class EntityManagementBeanTest extends ManagementBeanTest {

	protected String[] getExpectedEntities() {
		return new String[0];
	}
	
	protected abstract String getEntityType();
	
	protected String getEntityName() {
		return getEntityType() + "s";
	}
	
	protected String getEntityNames() {
		return getEntityType() + "Names";
	}

	protected abstract Object[] getAddEntityParams();
	protected abstract String[] getAddEntityParamClasses();
	
	protected void checkEntityNames(String aName, String... expected) throws Exception {
        ObjectName oName = getObjectName();
        String[] names = (String[]) mbsc.getAttribute(oName, aName);
        assertEquals(expected.length, names.length);
		List<String> exList = Arrays.asList(expected);
		for (String name: names) {
			assertTrue(exList.contains(name));
		}
	}

	@Test
	public void testGetEntityNames() throws Exception {
		checkEntityNames(getEntityNames(), getExpectedEntities());
	}

	@Test
	public void testGetEntities() throws Exception {
		ObjectName name = getObjectName();
        TabularData entities = (TabularData) mbsc.getAttribute(name, getEntityName());
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
		//String[] attrs = getExpectedAttributes();
		checkEntityNames(getEntityNames(), added);

		// check entity properties here..
		
		result = (Boolean) mbsc.invoke(oName, methods[2], params, classes);  
		assertFalse(result);
        
		result = (Boolean) mbsc.invoke(oName, methods[3], new Object[] {eName}, new String[] {String.class.getName()});
		assertTrue(result);
		checkEntityNames(getEntityNames(), original);

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
