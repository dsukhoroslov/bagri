package com.bagri.xdm.cache.hazelcast;

import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.getMemberSchemas;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SYSTEM_POOL;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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

import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.cache.hazelcast.management.ConfigManagement;
import com.bagri.xdm.cache.hazelcast.management.SchemaManagement;
import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.security.BagriJAASInvocationHandler;
import com.bagri.xdm.cache.hazelcast.security.BagriJMXAuthenticator;
import com.bagri.xdm.cache.hazelcast.store.system.ModuleCacheStore;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaInitiator;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;
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
        HazelcastInstance hz = context.getBean(hz_instance, HazelcastInstance.class);
        hz.getUserContext().put("context", context);
    	Member local = hz.getCluster().getLocalMember();
        String name = local.getStringAttribute(xdm_cluster_node_name);
        String role = local.getStringAttribute(xdm_cluster_node_role);
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
    	
    	String xport = hzInstance.getConfig().getProperty(xdm_cluster_admin_port);
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
		logger.info("JMX connector server started and listening on port: {}", xport);
    }

	private static void lookupManagedNodes(HazelcastInstance hzInstance, ApplicationContext context) {

		SchemaManagement sMgr = context.getBean("schemaService", SchemaManagement.class);
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (!member.localMember()) {
				sMgr.initMember(member);
			}
		}
	}

    @SuppressWarnings("unchecked")
	private static void initServerNode(HazelcastInstance systemInstance, Member local) {

		//check that new node has unique instance num..
        String instanceNum = local.getStringAttribute(xdm_node_instance);
        if (instanceNum == null) {
        	instanceNum = "0";
        }
        
    	Set<Member> members = systemInstance.getCluster().getMembers();
    	for (Member member: members) {
    		if (!local.getUuid().equals(member.getUuid()) && !isAdminRole(member.getStringAttribute(xdm_cluster_node_role))) {
    			if (instanceNum.equals(member.getStringAttribute(xdm_node_instance))) {
    				logger.error("The node with instance no '{}' already exists: {}; stopping application.", instanceNum, member.getUuid());
    				System.exit(1);
    			}
    		}
    	}
        
        Collection<XDMModule> cModules = null; 
        Collection<XDMLibrary> cLibraries = null; 
        Map<String, XDMSchema> schemaCache = null;
        
        Set<Member> admins = getAdmins(systemInstance);
        if (admins.size() == 0) {
	       	String confName = System.getProperty(xdm_config_filename);
	       	if (confName != null) {
	       		ConfigManagement cfg = new ConfigManagement(confName);
	       		Collection<XDMSchema> cSchemas = (Collection<XDMSchema>) cfg.getEntities(XDMSchema.class); 
	   			schemaCache = new HashMap<String, XDMSchema>(cSchemas.size());
	       		for (XDMSchema schema: cSchemas) {
	       			schemaCache.put(schema.getName(), schema);
	       	    }
	       		cModules = (Collection<XDMModule>) cfg.getEntities(XDMModule.class);
	       		cLibraries = (Collection<XDMLibrary>) cfg.getEntities(XDMLibrary.class);
	       	}
        }

        String[] aSchemas = getMemberSchemas(local);
        
        for (String name: aSchemas) {
          	String schemaName = name.trim();
       		logger.debug("initServerNode; going to deploy schema: {}", schemaName);
       		boolean initialized = false;
       		if (schemaCache != null) {
            	XDMSchema xSchema = schemaCache.get(schemaName);
            	if (xSchema != null) {
            		initialized = initSchema(systemInstance, local, xSchema);
            		//String store = xSchema.getProperty(xdm_schema_store_enabled);
            		ApplicationContext schemaContext = (ApplicationContext) getContext(schemaName, schema_context);
            		if (initialized) {
            			// set modules and libraries
            			RepositoryImpl xdmRepo = schemaContext.getBean(XDMRepository.bean_id, RepositoryImpl.class);
            			xdmRepo.setLibraries(cLibraries);
            			for (XDMModule module: cModules) {
            				try {
								ModuleCacheStore.loadModule(module);
							} catch (IOException e) {
			            		logger.warn("initServerNode; cannot load Module {} for schema '{}'!", module, schemaName);
							}
            			}
            			xdmRepo.setModules(cModules);
            			xdmRepo.afterInit();
            		}
            	}            	
           	}
       		// notify admin node about new schema Member
       		if (admins.size() > 0) {
       			notifyAdmins(systemInstance, local, schemaName, initialized);
       		}
    	}
    }
    
    private static void notifyAdmins(HazelcastInstance sysInstance, Member local, String schemaName, boolean initialized) {

    	int cnt = 0;
		IExecutorService execService = sysInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
        Set<Member> admins = getAdmins(sysInstance);

		// notify admin about new schema node (local)
		// hzInstance -> system instance, SchemaManagement is in its context
		// submit task to init member in admin..
		SchemaAdministrator adminTask = new SchemaAdministrator(schemaName, !initialized, local.getUuid());
       	Map<Member, Future<Boolean>> result = execService.submitToMembers(adminTask, admins);
        
        for (Map.Entry<Member, Future<Boolean>> e: result.entrySet()) {
   	       	try {
   				if (e.getValue().get()) {
   					cnt++;
   				} else {
   					logger.info("notifyAdmins; failed admin notification on member {}", e.getKey()); 
   				}
   			} catch (InterruptedException | ExecutionException ex) {
   				logger.error("notifyAdmins.error; ", ex);
   	        }
    	}
		logger.debug("notifyAdmins; notified {} admin nodes out of {} admins", cnt, admins.size());
    }
    
    private static Set<Member> getAdmins(HazelcastInstance hzInstance) {
    	Set<Member> admins = new HashSet<>();
    	Set<Member> members = hzInstance.getCluster().getMembers();
    	for (Member member: members) {
    		if (isAdminRole(member.getStringAttribute(xdm_cluster_node_role))) {
    			admins.add(member);
    		}
    	}
    	return admins;
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
