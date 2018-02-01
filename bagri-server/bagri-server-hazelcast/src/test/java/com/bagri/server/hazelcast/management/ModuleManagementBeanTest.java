package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ModuleManagementBeanTest extends EntityManagementBeanTest {
	
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
	protected String getEntityType() {
		return "Module";
	}
	
	@Override
	protected Map<String, Object> getExpectedAttributes() {
		Map<String, Object> map = new HashMap<>(2);
		map.put("Modules", null);
		map.put("ModuleNames", null);
		return map;
	}
	
	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getModules", "getModuleNames", "addModule", "deleteModule"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"hello_module", "mmd_module", "rest_module", "trigger_module"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"test_module", "test_module.xq", "test XQuery module", "tst", "http://bagridb.com/test"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()};
	}

}
