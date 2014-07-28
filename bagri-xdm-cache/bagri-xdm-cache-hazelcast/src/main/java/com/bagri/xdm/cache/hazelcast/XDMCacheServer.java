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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.cache.hazelcast.management.PopulationManager;
import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.security.BagriJAASInvocationHandler;
import com.bagri.xdm.cache.hazelcast.security.BagriJMXAuthenticator;
import com.bagri.xdm.process.hazelcast.SpringContextHolder;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaPopulator;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;
    
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	
        String contextPath = System.getProperty("xdm.config.context.file");
        String role = System.getProperty(op_node_role);
        logger.info("Starting \"{}\" node with Context [{}]", role, contextPath);
    	
        context = new ClassPathXmlApplicationContext(contextPath);
        HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
        String name = hz.getCluster().getLocalMember().getStringAttribute(op_node_name);
        //String schemas = hz.getConfig().getProperty(op_node_schemas);
        //hz.getCluster().getLocalMember().setStringAttribute(op_node_schemas, schemas);
        logger.debug("System Cache started with Config: {}; Instance: {}", hz.getConfig(), hz);
        logger.debug("Cluster size: {}; Node: {}", hz.getCluster().getMembers().size(), name);
        
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
    
    private static void initServerNode(HazelcastInstance systemInstance) {
        int clusterSize = systemInstance.getCluster().getMembers().size();
        String schemas = systemInstance.getCluster().getLocalMember().getStringAttribute(op_node_schemas);
        String[] aSchemas = schemas.split(" ");
        IMap<String, XDMSchema> schemaCache = systemInstance.getMap("schemas");
        for (String name: aSchemas) {
          	String schemaName = name.trim();
           	if (schemaName.length() > 0) {
            	//if (clusterSize == 1) {
            		logger.debug("initServerNode; Going to deploy schema: {}", schemaName);
            		// will deploy schema here..
            	//}
            	XDMSchema xSchema = schemaCache.get(schemaName);
            	if (xSchema != null) {
            		initSchema(systemInstance, xSchema);
            	}

            	HazelcastInstance schemaInstance = Hazelcast.getHazelcastInstanceByName(schemaName);
            	if (schemaInstance != null) {
            		//ApplicationContext schemaContext = (ApplicationContext) schemaInstance.getUserContext().get("appContext");
            		ApplicationContext schemaContext = (ApplicationContext) 
            				SpringContextHolder.getContext(schemaName, "appContext");
            		PopulationManager popManager = schemaContext.getBean("popManager", PopulationManager.class);
            		popManager.checkPopulation(schemaInstance.getCluster().getMembers().size());
            	} else {
            		logger.warn("initServerNode; cannot find HazelcastInstance for schema '{}'!", schemaName);
            	}
           	}
    	}
    	
    }
    
    private static boolean initSchema(HazelcastInstance hzInstance, XDMSchema schema) {
    	
		logger.trace("initSchema.enter; schema: {}", schema);
		SchemaInitiator init = new SchemaInitiator(schema.getName(), schema.getProperties());
		IExecutorService execService = hzInstance.getExecutorService("xdm-exec-pool");
       	Future<Boolean> result = execService.submitToMember(init, hzInstance.getCluster().getLocalMember());
       	Boolean ok = false;
       	try {
			ok = result.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("initSchemaInCluster.error; ", ex);
        }
		logger.info("initSchemaInCluster.exit; schema {} {}initialized", schema, ok ? "" : "NOT ");
		return ok;
	}
    
}
