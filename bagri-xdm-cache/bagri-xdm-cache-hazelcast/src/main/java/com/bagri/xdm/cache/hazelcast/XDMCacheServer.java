package com.bagri.xdm.cache.hazelcast;

import static com.bagri.xdm.system.XDMNode.op_node_name;
import static com.bagri.xdm.system.XDMNode.op_node_role;
import static com.bagri.xdm.system.XDMNode.op_node_schemas;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.security.BagriJAASInvocationHandler;
import com.bagri.xdm.cache.hazelcast.security.BagriJMXAuthenticator;
import com.hazelcast.core.HazelcastInstance;

public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;
    
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	
        String role = System.getProperty(op_node_role);
        String contextPath;
        if ("admin".equals(role)) {
        	contextPath = "spring/bagri-admin-context.xml";
        } else {
        	contextPath = "spring/bagri-server-context.xml";
        }
        logger.info("Starting \"{}\" node with Context [{}]", role, contextPath);
    	
        context = new ClassPathXmlApplicationContext(contextPath);
        HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
        String name = hz.getConfig().getProperty(op_node_name);
        hz.getCluster().getLocalMember().setStringAttribute(op_node_name, name);
        //String schemas = hz.getConfig().getProperty(op_node_schemas);
        //hz.getCluster().getLocalMember().setStringAttribute(op_node_schemas, schemas);
        logger.debug("System Cache started with Config: {}; Instance: {}", hz.getConfig(), hz);
        logger.debug("Cluster size: {}", hz.getCluster().getMembers().size());
        
        //String role = hz.getConfig().getProperty("xdm.cluster.node.role");
        if ("admin".equals(role)) {
        	initAdminNode(hz);
        } else {
        	initServerNode(hz);
        }
        
    }
    
    private static void initAdminNode(HazelcastInstance hz) {
    	
    	String xport = hz.getConfig().getProperty("xdm.cluster.admin.port");
    	int port = Integer.parseInt(xport);
    	JMXServiceURL url;
		try {
			//url = new JMXServiceURL("rmi", "localhost", port);
			url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + xport + "/jmxrmi");
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

		try {
			LocateRegistry.createRegistry(port);
		} catch (RemoteException ex) {
			logger.warn("error creating JMX Registry: {}", ex.getMessage());
			//throw new IllegalArgumentException("wrong JMX registry", ex);
		}
		
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		logger.debug("Platform MBean server: {}", mbs);
		logger.debug("Spring MBean server: {}", context.getBean("mbeanServer"));
		
        JMXConnectorServer cs;
		try {
			cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
			UserManagement uMgr = context.getBean(UserManagement.class);
	        MBeanServerForwarder mbsf = BagriJAASInvocationHandler.newProxyInstance(uMgr);
	        cs.setMBeanServerForwarder(mbsf);
	        cs.start();
		} catch (IOException ex) {
			logger.error("error starting JMX connector server: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
		logger.debug("JMX connector server started with attributes: {}", cs.getAttributes());
    }
    
    private static void initServerNode(HazelcastInstance hz) {
        int clusterSize = hz.getCluster().getMembers().size();
    	if (clusterSize == 1) {
            String schemas = System.getProperty(op_node_schemas);
            String[] aSchemas = schemas.split(" ");
            for (String name: aSchemas) {
            	String schema = name.trim();
            	if (schema.length() > 0) {
            		logger.debug("Going to deploy schema: {}", schema);
            		// will deploy schema here..
            	}
            }
    	}
    	
    }

}
