package com.bagri.server.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class LibraryManagementBeanTest extends EntityManagementBeanTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=LibraryManagement");
	}

	@Override
	protected String getEntityType() {
		return "Library";
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Libraries", "LibraryNames"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getLibraries", "getLibraryNames", "addLibrary", "deleteLibrary"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"java_library", "trigger_library"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"test_library", "myExtensionLibrary.jar", "Extensions library"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName(), String.class.getName()};
	}


}
