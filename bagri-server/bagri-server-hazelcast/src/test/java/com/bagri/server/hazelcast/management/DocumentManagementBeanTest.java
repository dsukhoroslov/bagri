package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.schema_context;
import static org.junit.Assert.*;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

//@Ignore
public class DocumentManagementBeanTest {
	
    private static MBeanServerConnection mbsc;
    private static ClassPathXmlApplicationContext servCtx;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        mbsc = startAdminServer();
		startCacheServer("0");
		System.out.println("---------- servers initialized ---------");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopAdminServer();
		//servCtx.close();
	}

	@Test
	public void testCacheServer() throws Exception {
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("default-0");
		assertNotNull(hz);
        hz = Hazelcast.getHazelcastInstanceByName(hz_instance);
		assertNotNull(hz);
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
        ctx.close();
	}
	
	//@Test
	//public void testDocumentOperations() throws Exception {
		//
	//	SchemaRepositoryImpl repo = servCtx.getBean(SchemaRepositoryImpl.class);
	//	assertEquals("guest", repo.getUserName());
	//}
	
}
