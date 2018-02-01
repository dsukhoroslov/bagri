package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DocumentManagementBeanTest extends ManagementBeanTest {
	
    //private static ClassPathXmlApplicationContext servCtx;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("bdb.log.level", "trace");
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

	//@Test
	//public void testCacheServer() throws Exception {
        //HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("default-0");
		//assertNotNull(hz);
	//	HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("hzInstance-0");
	//	assertNotNull(hz);
        //ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
        //ctx.close();
	//}

	@Override
	protected ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.bagri.db:type=Schema,name=default,kind=DocumentManagement");
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Schema", "TotalCounts", "DocumentCount", "ElementCount", "SchemaSize", "Collections", "CollectionStatistics"};
	}

	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getSchema", "clear", "getTotalCounts", "getDocumentCount", "getElementCount", "getSchemaSize", "getDocumentElements",
				"getDocumentInfo", "getDocumentLocation", "getDocumentContent", "registerDocument", "updateDocument", "removeDocument",
				"registerDocuments", "getCollections", "addCollection", "removeCollection", "enableCollection", "getCollectionStatistics",
				"resetStatistics", "addDocumentToCollection", "removeDocumentFromCollection", "getCollectionDocuments", "getDocumentUris"};
	}

	
	//@Test
	//public void testDocumentOperations() throws Exception {
		//
	//	SchemaRepositoryImpl repo = servCtx.getBean(SchemaRepositoryImpl.class);
	//	assertEquals("guest", repo.getUserName());
	//}
	
}
