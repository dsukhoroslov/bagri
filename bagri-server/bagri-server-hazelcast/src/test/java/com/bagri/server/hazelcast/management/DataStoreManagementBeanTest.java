package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DataStoreManagementBeanTest extends EntityManagementBeanTest {
	
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
        return new ObjectName("com.bagri.db:type=Management,name=DataStoreManagement");
	}

	@Override
	protected String getEntityType() {
		return "DataStore";
	}
	
	@Override
	protected Map<String, Object> getExpectedAttributes() {
		Map<String, Object> map = new HashMap<>(2);
		map.put("DataStores", null);
		map.put("DataStoreNames", null);
		return map;
	}
	
	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getDataStores", "getDataStoreNames", "addDataStore", "deleteDataStore"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"File"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"JDBC", "com.bagri.ext.store.JdbcStore", "JDBC data-store plugin", 
			"jdbc.url=jdbc:postgresql://localhost/world;jdbc.driverClassName=org.postgresql.Driver;jdbc.username=postgres;jdbc.password=postgres"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName()};
	}

}
