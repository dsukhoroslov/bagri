package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.startAdminServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.startCacheServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.stopAdminServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.stopCacheServer;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class IndexManagementBeanTest extends ManagementBeanTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("bdb.log.level", "trace");
        mbsc = startAdminServer();
		startCacheServer("0");
		System.out.println("----- servers started -------");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopCacheServer(); //"0");
		stopAdminServer();
		//servCtx.close();
	}

	@Override
	protected ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.bagri.db:type=Schema,name=default,kind=IndexManagement");
	}
	
	@Override
	protected Map<String, Object> getExpectedAttributes() {
		Map<String, Object> map = new HashMap<>(7);
		map.put("Schema", "default");
		map.put("Indexes", null);
		map.put("UsageStatistics", null);
		return map;
	}
	
	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getSchema", "getIndexes", "getUsageStatistics", "addIndex", "dropIndex", "enableIndex", "rebuildIndex"};
	}

}
