package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class SchemaManagementBeanTest extends EntityManagementBeanTest {

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
        return new ObjectName("com.bagri.db:type=Management,name=SchemaManagement");
	}
	
	@Override
	protected String getEntityType() {
		return "Schema";
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Schemas", "SchemaNames", "DefaultProperties"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getSchemas", "getSchemaNames", "addSchema", "deleteSchema", "getDefaultProperties", "setDefaultProperty"};
	}

	@Override
	protected String[] getExpectedEntities() {
		return new String[] {"XMark", "TPoX", "XDM", "TPoX-J", "YCSB", "default"};
	}

	@Override
	protected Object[] getAddEntityParams() {
		return new Object[] {"Test", "schema for tests", "bdb.schema.store.enabled=true;bdb.schema.population.size=2;bdb.schema.store.data.path=../data/test"};
	}
	
	@Override
	protected String[] getAddEntityParamClasses() {
		return new String[] {String.class.getName(), String.class.getName(), String.class.getName()};
	}

}
