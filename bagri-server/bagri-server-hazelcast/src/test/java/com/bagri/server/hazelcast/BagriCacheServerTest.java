package com.bagri.server.hazelcast;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;
import static com.bagri.server.hazelcast.util.SpringContextHolder.schema_context;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BagriCacheServerTest {
	
	@Test
	public void testCacheServer() throws Exception {
		startCacheServer("0");
		// check if schema is started..
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("default-0");
		assertNotNull(hz);
        hz = Hazelcast.getHazelcastInstanceByName("hzInstance-0");
		assertNotNull(hz);
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
        ctx.close();
	}
	
}
