package com.bagri.xdm.cache.hazelcast;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.security.BagriJMXAuthenticator;
import com.hazelcast.core.HazelcastInstance;

import static com.bagri.xdm.system.XDMNode.*;

public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;
    
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	
        context = new ClassPathXmlApplicationContext("spring/application-context.xml");
        HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
        String name = hz.getConfig().getProperty(op_node_name);
        hz.getCluster().getLocalMember().setStringAttribute(op_node_name, name);
        //String schemas = hz.getConfig().getProperty(op_node_schemas);
        //hz.getCluster().getLocalMember().setStringAttribute(op_node_schemas, schemas);
        logger.debug("System Cache started with Config: {}; Instance: {}", hz.getConfig(), hz);
        
    	String sport = System.getProperty("com.sun.management.jmxremote.port");
    	int port = Integer.parseInt(sport);
    	JMXServiceURL url;
		try {
			url = new JMXServiceURL("rmi", "localhost", port);
			//url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:3333/jmxrmi");
		} catch (MalformedURLException ex) {
			logger.warn("error creating JMX URL: {}", ex.getMessage());
			throw new IllegalArgumentException("wrong JMX connection", ex);
		}
		
        Map<String, Object> env = new HashMap<String, Object>();
        //BagriJMXAuthenticator auth = new BagriJMXAuthenticator();
        BagriJMXAuthenticator auth = context.getBean("authManager", BagriJMXAuthenticator.class);
        env.put(JMXConnectorServer.AUTHENTICATOR, auth);
        //env.put("jmx.remote.x.password.file", "");
        //env.put("jmx.remote.x.access.file", "");
		logger.debug("going to start JMX connector server at: {}, with attributes: {}", url, env);

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		logger.debug("Platform MBean server: {}", mbs);
		logger.debug("Spring MBean server: {}", context.getBean("mbeanServer"));
		
        JMXConnectorServer cs;
		try {
			cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
	        cs.start();    	
		} catch (IOException ex) {
			logger.error("error starting JMX connector server: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
		logger.debug("JMX connector server started with attributes: {}", cs.getAttributes());
    }

}
