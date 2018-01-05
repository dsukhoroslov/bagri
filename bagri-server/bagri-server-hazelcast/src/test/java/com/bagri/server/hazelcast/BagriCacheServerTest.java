package com.bagri.server.hazelcast;

import static com.bagri.core.Constants.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.schema_context;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.server.hazelcast.BagriCacheServer;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BagriCacheServerTest {
	
	@Test
	public void testCacheServer() throws Exception {
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_access_filename, "access.xml");
		System.setProperty(pn_cluster_node_name, "first");
		System.setProperty(pn_node_instance, "0");
		System.setProperty(pn_config_path, "src/main/resources");
		System.setProperty(pn_config_filename, "config.xml");
        System.setProperty(pn_config_context_file, "spring/cache-system-context.xml");
		System.setProperty(pn_config_properties_file, "first.properties");
		BagriCacheServer.main(null);
		// check if schema is started..
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("default-0");
		assertNotNull(hz);
        hz = Hazelcast.getHazelcastInstanceByName(hz_instance);
		assertNotNull(hz);
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
        ctx.close();
	}
	
}
