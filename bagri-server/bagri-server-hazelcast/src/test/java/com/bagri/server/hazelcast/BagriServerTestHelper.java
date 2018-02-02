package com.bagri.server.hazelcast;

import static com.bagri.core.Constants.pn_access_filename;
import static com.bagri.core.Constants.pn_cluster_node_name;
import static com.bagri.core.Constants.pn_cluster_node_role;
import static com.bagri.core.Constants.pn_cluster_node_schemas;
import static com.bagri.core.Constants.pn_config_context_file;
import static com.bagri.core.Constants.pn_config_filename;
import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_config_properties_file;
import static com.bagri.core.Constants.pn_node_instance;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.schema_context;

import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class BagriServerTestHelper {

	private static String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:3330/jmxrmi";
	private static String user = "admin";
	private static String password = "password";
	
	private static ClassPathXmlApplicationContext systemCtx;

	public static MBeanServerConnection startAdminServer() throws Exception {
		
		System.setProperty("hz.log.level", "info");
		//System.setProperty("bdb.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_access_filename, "access.xml");
		System.setProperty(pn_cluster_node_name, "admin");
		System.setProperty(pn_cluster_node_role, "admin");
		System.setProperty(pn_cluster_node_schemas, "");
		System.setProperty(pn_config_path, "src/main/resources");
		System.setProperty(pn_config_filename, "config.xml");
		System.setProperty(pn_config_context_file, "spring/admin-system-context.xml");
		System.setProperty(pn_config_properties_file, "admin.properties");
		BagriCacheServer.main(null);
		
		// now get MBeanServerConnection..
        HashMap<String, Object> env = new HashMap<>();
        String[] creds = new String[] {user, password};
        env.put(JMXConnector.CREDENTIALS, creds);
        JMXServiceURL jmxUrl = new JMXServiceURL(url);
        JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl, env);
        //JMXConnector jmxc = JMXConnectorFactory.newJMXConnector(jmxUrl, null);
        //jmxc.connect(env);
        
        return jmxc.getMBeanServerConnection();
	}
	
	public static void stopAdminServer() {
		BagriCacheServer.closeAdmin();
        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(hz_instance);
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
		ctx.close();
	}
	
	public static ClassPathXmlApplicationContext startCacheServer(String instance) throws Exception {

		String schemaName = "default";
		System.setProperty("hz.log.level", "info");
		//System.setProperty("bdb.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_access_filename, "access.xml");
		//System.setProperty(pn_cluster_node_name, "cache");
		System.setProperty(pn_cluster_node_name, "first");
		System.setProperty(pn_cluster_node_role, "server");
		System.setProperty(pn_config_path, "src/main/resources");
		System.setProperty(pn_config_filename, "config.xml");
        System.setProperty(pn_config_context_file, "spring/cache-system-context.xml");
		System.setProperty(pn_config_properties_file, "first.properties");
		System.setProperty(pn_cluster_node_schemas, schemaName);
		System.setProperty(pn_node_instance, instance);
		BagriCacheServer.main(null);

        HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("hzInstance-" + instance);
        systemCtx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
        hz = Hazelcast.getHazelcastInstanceByName(schemaName + "-" + instance);
        ClassPathXmlApplicationContext schemaCtx = (ClassPathXmlApplicationContext) hz.getUserContext().get(schema_context);
        Thread.sleep(1000);
        return schemaCtx;
	}
	
	public static void stopCacheServer() {
		//BagriCacheServer.closeAdmin();
		systemCtx.close();
	}

}
