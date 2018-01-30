package com.bagri.server.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class UserManagementBeanTest extends EntityManagementBeanTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=UserManagement");
	}

	@Override
	protected String getEntityType() {
		return "User";
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Users", "UserNames", "CurrentUser"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getUsers", "getUserNames", "addUser", "deleteUser", "getCurrentUser"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"admin", "guest"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"test", "password"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName()};
	}


}
