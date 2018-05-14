package com.bagri.server.hazelcast;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.server.api.SchemaRepository.bean_id;
import static com.bagri.server.hazelcast.util.HazelcastUtils.*;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

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

import com.bagri.core.system.DataFormat;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.rest.BagriRestServer;
import com.bagri.server.hazelcast.config.SystemConfig;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.server.hazelcast.management.SchemaManagement;
import com.bagri.server.hazelcast.management.UserManagement;
import com.bagri.server.hazelcast.security.BagriJAASInvocationHandler;
import com.bagri.server.hazelcast.security.BagriJMXAuthenticator;
import com.bagri.server.hazelcast.store.system.ModuleCacheStore;
import com.bagri.server.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.server.hazelcast.task.schema.SchemaInitiator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class BagriCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriCacheServer.class);
    private static ApplicationContext context;
    private static JMXConnectorServer adminCS = null;
    
	public static void main(String[] args) {
    	
        String contextPath = System.getProperty(pn_config_context_file);
        logger.info("Starting BagriDB node with Context [{}]", contextPath);
    	
        context = new ClassPathXmlApplicationContext(contextPath);
        HazelcastInstance hz = context.getBean(hz_instance, HazelcastInstance.class);
        hz.getUserContext().put(schema_context, context);
    	Member local = hz.getCluster().getLocalMember();
        String name = local.getStringAttribute(pn_cluster_node_name);
        String role = local.getStringAttribute(pn_cluster_node_role);
        logger.debug("System Cache started with Config: {}; Instance: {}", hz.getConfig(), hz.getClass().getName());
        logger.debug("Cluster size: {}; Node: {}; Role: {}", hz.getCluster().getMembers().size(), name, role);
        
        if (isAdminRole(role)) {
        	initAdminNode(hz);
        	// discover active schema server nodes now..
        	lookupManagedNodes(hz);
        } else {
        	initServerNode(hz, local);
        }
    }
	
	public static void closeAdmin() {
		if (adminCS != null) {
			try {
				adminCS.stop();
			} catch (IOException ex) {
				logger.warn("error closing JMX connector server: {}", ex.getMessage());
			}
		}
	}
    
    private static void initAdminNode(HazelcastInstance hzInstance) {
    	
    	String xport = ((Member) hzInstance.getLocalEndpoint()).getStringAttribute(pn_cluster_admin_port);
    	int port = Integer.parseInt(xport);
    	JMXServiceURL url;
		try {
			url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + xport + "/jmxrmi");
		} catch (MalformedURLException ex) {
			logger.warn("error creating JMX URL: {} on port: {}", ex.getMessage(), port);
			throw new IllegalArgumentException("wrong JMX connection", ex);
		}
		
        Map<String, Object> env = new HashMap<String, Object>();
        //BagriJMXAuthenticator auth = new BagriJMXAuthenticator();
        BagriJMXAuthenticator auth = context.getBean(BagriJMXAuthenticator.class);
        env.put(JMXConnectorServer.AUTHENTICATOR, auth);
		logger.debug("going to start JMX connector server at: {}, with attributes: {}", url, env);

		try {
			LocateRegistry.createRegistry(port);
		} catch (RemoteException ex) {
			logger.warn("error creating JMX Registry: {}", ex.getMessage());
		}
		
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		logger.debug("Platform MBean server: {}", mbs);
		logger.debug("Spring MBean server: {}", context.getBean("mbeanServer"));
		
		try {
			adminCS = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
			UserManagement uMgr = context.getBean(UserManagement.class);
	        MBeanServerForwarder mbsf = BagriJAASInvocationHandler.newProxyInstance(uMgr);
	        adminCS.setMBeanServerForwarder(mbsf);
	        if (!adminCS.isActive()) {
	        	adminCS.start();
	        }
			logger.info("JMX connector server started and listening on port: {}", port);
		} catch (IOException ex) {
			logger.warn("error starting JMX connector server: {}", ex.getMessage());
			// it is already started, most probably..
		}
		
		BagriRestServer rest = context.getBean(BagriRestServer.class);
		rest.start();
		logger.info("REST server started on port: {}; provider: {}", rest.getPort(), rest.getRepositoryProvider());
    }

	private static void lookupManagedNodes(HazelcastInstance hzInstance) {

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
        String instanceNum = local.getStringAttribute(pn_node_instance);
        if (instanceNum == null) {
        	instanceNum = "0";
        }
        
    	Set<Member> members = systemInstance.getCluster().getMembers();
    	for (Member member: members) {
    		if (!local.getUuid().equals(member.getUuid()) && !isAdminRole(member.getStringAttribute(pn_cluster_node_role))) {
    			if (instanceNum.equals(member.getStringAttribute(pn_node_instance))) {
    				logger.error("initServerNode; The node with instance no '{}' already exists: {}; stopping application.", instanceNum, member.getUuid());
    				System.exit(1);
    			}
    		}
    	}
        
        Collection<Module> cModules = null; 
        Collection<Library> cLibraries = null; 
        Collection<DataFormat> cFormats = null; 
        Map<String, Schema> schemaCache = null;
        
    	SystemConfig cfg = context.getBean(SystemConfig.class);

    	Set<Member> admins = getAdmins(systemInstance);
        if (admins.size() == 0) {
	       	if (cfg.isLoaded()) {
	       		Collection<Schema> cSchemas = (Collection<Schema>) cfg.getEntities(Schema.class); 
	   			schemaCache = new HashMap<String, Schema>(cSchemas.size());
	       		for (Schema schema: cSchemas) {
	       			schemaCache.put(schema.getName(), schema);
	       	    }
	       		cModules = (Collection<Module>) cfg.getEntities(Module.class);
	       		cLibraries = (Collection<Library>) cfg.getEntities(Library.class);
	       		cFormats = (Collection<DataFormat>) cfg.getEntities(DataFormat.class);
	       	}
        }
    	// else it's ok, correct case..

        String[] aSchemas = getMemberSchemas(local);
        
        for (String name: aSchemas) {
          	String schemaName = name.trim();
       		logger.debug("initServerNode; going to deploy schema: {}", schemaName);
       		boolean initialized = false;
       		if (schemaCache != null) {
            	Schema xSchema = schemaCache.get(schemaName);
            	if (xSchema != null) {
            		initialized = initSchema(systemInstance, local, xSchema);
            		//String store = xSchema.getProperty(pn_schema_store_enabled);
            		ApplicationContext schemaContext = getContext(schemaName);
            		if (initialized) {
            			// set modules and libraries
            			SchemaRepositoryImpl xRepo = schemaContext.getBean(bean_id, SchemaRepositoryImpl.class);
            			xRepo.setLibraries(cLibraries);
            			for (Module module: cModules) {
            				try {
								ModuleCacheStore.loadModule(module);
							} catch (IOException e) {
			            		logger.warn("initServerNode; cannot load Module {} for schema '{}'!", module, schemaName);
							}
            			}
            			xRepo.setModules(cModules);
            			xRepo.setDataFormats(cFormats);
            			xRepo.afterInit();
            			//xRepo.getHzInstance().getCluster().changeClusterState(ClusterState.ACTIVE);
            			logger.info("initServerNode; schema {} initialization complete", schemaName);
            		} else {
            			logger.info("initServerNode; schema {} initialization skipped", schemaName);
            		}
            	} else {
        			logger.info("initServerNode; NO schema found for name {}", schemaName);
            	}
           	} else {
    			logger.info("initServerNode; NO schema cache found");
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
    		if (isAdminRole(member.getStringAttribute(pn_cluster_node_role))) {
    			admins.add(member);
    		}
    	}
    	return admins;
    }
    
    private static boolean initSchema(HazelcastInstance hzInstance, Member member, Schema schema) {
    	
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
		logger.info("initSchema.exit; schema {} {}initialized", schema.getName(), ok ? "" : "NOT ");
		return ok;
	}
    
    private static boolean isAdminRole(String role) {
        return "admin".equalsIgnoreCase(role);
    }
    
}
