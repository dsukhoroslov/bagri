package com.bagri.server.hazelcast;

import static com.bagri.core.Constants.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.schema_context;
import static org.junit.Assert.*;

import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.server.hazelcast.BagriCacheServer;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BagriCacheServerTest {
	
	private static String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:3330/jmxrmi";
	private static String user = "admin";
	private static String password = "password";
	private static String adminCtx = "spring/admin-system-context.xml";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
        //<sysproperty key="node.logdir" value="./logs"/>
        //<sysproperty key="node.name" value="admin"/>
        //<sysproperty key="com.sun.management.jmxremote" value="true"/>
        //<sysproperty key="com.sun.management.jmxremote.ssl" value="false"/>
        //<sysproperty key="bdb.cluster.node.role" value="admin"/>
		
		System.setProperty("hz.log.level", "info");
		//System.setProperty("bdb.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_access_filename, "access.xml");
		System.setProperty(pn_cluster_node_name, "admin");
		System.setProperty(pn_cluster_node_schemas, "");
		System.setProperty(pn_config_path, "src/main/resources");
		System.setProperty(pn_config_filename, "config.xml");
		System.setProperty(pn_config_context_file, adminCtx);
		System.setProperty(pn_config_properties_file, "admin.properties");
		BagriCacheServer.main(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(hz_instance);
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
		ctx.close();
	}
	
	@Test
	public void testJMXConnection() throws Exception {
		
        HashMap env = new HashMap();
        String[] creds = new String[] {user, password};
        env.put(JMXConnector.CREDENTIALS, creds);

        JMXServiceURL jmxUrl = new JMXServiceURL(url);
        //JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl, env);
        
        JMXConnector jmxc = JMXConnectorFactory.newJMXConnector(jmxUrl, null);
        jmxc.connect(env);
        
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        String[] domains = mbsc.getDomains();
        assertTrue(containsDomain(domains, "com.bagri.db"));
        
        ObjectName name = new ObjectName("com.bagri.db:type=Management,name=ClusterManagement");
        Object nodes = mbsc.getAttribute(name, "NodeNames");
        //System.out.println("got nodes: " + Arrays.toString((String[]) nodes));
        String[] sNodes = (String[]) nodes;
        assertTrue(sNodes.length > 0);
	}
	
	private boolean containsDomain(String[] domains, String domain) {
		for (String d : domains) {
			if (d.equals(domain)) return true;
		}
		return false;
	}

}
