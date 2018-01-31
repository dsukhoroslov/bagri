package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class RoleManagementBeanTest extends EntityManagementBeanTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=RoleManagement");
	}

	@Override
	protected String getEntityType() {
		return "Role";
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Roles", "RoleNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getRoles", "getRoleNames", "addRole", "deleteRole"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"DataFormatManagement", "DataStoreManagement", "LibraryManagement", "ModuleManagement", "NodeManagement", 
				"SchemaManagement", "RoleManagement", "UserManagement", "AdminRole", "GuestRole"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"TestRole", "Role for tests"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName()};
	}


}
