package com.bagri.xdm.cache.hazelcast;

import static com.bagri.xdm.access.api.XDMCacheConstants.PN_XDM_SYSTEM_POOL;
import static com.bagri.xdm.access.api.XDMConfigConstants.*;
import static com.bagri.xdm.system.XDMNode.op_admin_port;
import static com.bagri.xdm.system.XDMNode.op_node_name;
import static com.bagri.xdm.system.XDMNode.op_node_role;
import static com.bagri.xdm.system.XDMNode.op_node_schemas;
import static com.bagri.xdm.process.hazelcast.util.HazelcastUtils.getMemberSchemas;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.bagri.xdm.cache.hazelcast.management.ConfigManagement;
import com.bagri.xdm.cache.hazelcast.management.PopulationManager;
import com.bagri.xdm.cache.hazelcast.management.SchemaManagement;
import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.security.BagriJAASInvocationHandler;
import com.bagri.xdm.cache.hazelcast.security.BagriJMXAuthenticator;
import com.bagri.xdm.process.hazelcast.SpringContextHolder;
import com.bagri.xdm.process.hazelcast.schema.SchemaAdministrator;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;
    
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	
        String contextPath = System.getProperty(xdm_config_context_file);
        logger.info("Starting XDM node with Context [{}]", contextPath);
    	
        context = new ClassPathXmlApplicationContext(contextPath);
        HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
    	Member local = hz.getCluster().getLocalMember();
        String name = local.getStringAttribute(op_node_name);
        String role = local.getStringAttribute(op_node_role);
        logger.debug("System Cache started with Config: {}; Instance: {}", hz.getConfig(), hz.getClass().getName());
        logger.debug("Cluster size: {}; Node: {}; Role: {}", hz.getCluster().getMembers().size(), name, role);
        
        if (isAdminRole(role)) {
        	initAdminNode(hz);
        	// discover active schema server nodes now..
        	lookupManagedNodes(hz, context);
        } else {
        	initServerNode(hz, local);
        }
    }
    
    private static void initAdminNode(HazelcastInstance hzInstance) {
    	
    	String xport = hzInstance.getConfig().getProperty(op_admin_port);
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

	private static void lookupManagedNodes(HazelcastInstance hzInstance, ApplicationContext context) {

		SchemaManagement sMgr = context.getBean("schemaService", SchemaManagement.class);
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (!member.localMember()) {
				sMgr.initMember(member);
			}
		}
	}

    private static void initServerNode(HazelcastInstance systemInstance, Member local) {
        //int clusterSize = systemInstance.getCluster().getMembers().size();
        String[] aSchemas = getMemberSchemas(local);
        
        Map<String, XDMSchema> schemaCache;
       	String confName = System.getProperty(xdm_config_filename);
       	if (confName != null) {
       		ConfigManagement cfg = new ConfigManagement(confName);
       		Collection<XDMSchema> cSchemas = (Collection<XDMSchema>) cfg.getEntities(XDMSchema.class); 
   			schemaCache = new HashMap<String, XDMSchema>(cSchemas.size());
       		for (XDMSchema schema: cSchemas) {
       			schemaCache.put(schema.getName(), schema);
       	    }
       	} else {
       		schemaCache = null;
       	}
        
        for (String name: aSchemas) {
          	String schemaName = name.trim();
       		logger.debug("initServerNode; going to deploy schema: {}", schemaName);
       		boolean initialized = false;
       		if (schemaCache != null) {
            	XDMSchema xSchema = schemaCache.get(schemaName);
            	if (xSchema != null) {
            		initialized = initSchema(systemInstance, local, xSchema);
            		String store = xSchema.getProperty(xdm_schema_store_type);
            		if (!"NONE".equals(store)) {
	            		HazelcastInstance schemaInstance = Hazelcast.getHazelcastInstanceByName(schemaName);
		            	if (schemaInstance != null) {
		            		//ApplicationContext schemaContext = (ApplicationContext) schemaInstance.getUserContext().get("appContext");
		            		ApplicationContext schemaContext = (ApplicationContext) 
		            				SpringContextHolder.getContext(schemaName, "appContext");
		            		PopulationManager popManager = schemaContext.getBean("popManager", PopulationManager.class);
		            		// we need to do it here, for local (just started) node only..
		            		popManager.checkPopulation(schemaInstance.getCluster().getMembers().size());
		            		//logger.debug("initServerNode; started population for schema '{}' here..", schemaName);
		            	} else {
		            		logger.warn("initServerNode; cannot find HazelcastInstance for schema '{}'!", schemaName);
		            	}
            		}
            	}            	
           	}
       		// notify admin node about new schema Member
           	notifyAdmins(systemInstance, local, schemaName, initialized);
    	}
    }
    
    private static void notifyAdmins(HazelcastInstance hzInstance, Member local, String schemaName, boolean initialized) {

    	int cnt = 0;
		IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
    	Set<Member> members = hzInstance.getCluster().getMembers();
    	for (Member member: members) {
    		if (isAdminRole(member.getStringAttribute(op_node_role))) {
    			// notify admin about new schema node (local)
    			// hzInstance -> system instance, SchemaManagement is in its context
    			// submit task to init member in admin..
    			SchemaAdministrator adminTask = new SchemaAdministrator(schemaName, !initialized, local.getUuid());
    	       	Future<Boolean> result = execService.submitToMember(adminTask, member);
    	       	try {
    				if (result.get()) {
    					cnt++;
    				}
    			} catch (InterruptedException | ExecutionException ex) {
    				logger.error("notifyAdmins.error; ", ex);
    	        }
    		}
    	}
		logger.debug("notifyAdmins; notified {} admin nodes out of {} members", cnt, members.size());
    }
    
    private static boolean initSchema(HazelcastInstance hzInstance, Member member, XDMSchema schema) {
    	
		logger.trace("initSchema.enter; schema: {}", schema);
		SchemaInitiator init = new SchemaInitiator(schema);
		IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
       	Future<Boolean> result = execService.submitToMember(init, member);
       	Boolean ok = false;
       	try {
			ok = result.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("initSchema.error; ", ex);
        }
		logger.info("initSchema.exit; schema {} {}initialized", schema, ok ? "" : "NOT ");
		return ok;
	}
    
    private static boolean isAdminRole(String role) {
        return "admin".equals(role);
    }
    
}
