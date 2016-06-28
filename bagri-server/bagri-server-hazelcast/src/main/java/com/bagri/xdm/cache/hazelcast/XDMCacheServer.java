package com.bagri.xdm.cache.hazelcast;

import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.getMemberSchemas;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;
import static com.bagri.xdm.common.XDMConstants.*;
import static com.bagri.xdm.cache.api.XDMCacheConstants.PN_XDM_SYSTEM_POOL;
import static com.bagri.xdm.cache.api.XDMRepository.bean_id;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.*;

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
import com.bagri.xdm.cache.hazelcast.config.SystemConfig;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.cache.hazelcast.management.SchemaManagement;
import com.bagri.xdm.cache.hazelcast.management.UserManagement;
import com.bagri.xdm.cache.hazelcast.security.BagriJAASInvocationHandler;
import com.bagri.xdm.cache.hazelcast.security.BagriJMXAuthenticator;
import com.bagri.xdm.cache.hazelcast.store.system.ModuleCacheStore;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaInitiator;
import com.bagri.xdm.system.DataFormat;
import com.bagri.xdm.system.Library;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Schema;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class XDMCacheServer {

    private static final transient Logger logger = LoggerFactory.getLogger(XDMCacheServer.class);
    private static ApplicationContext context;
    
	public static void main(String[] args) {
    	
        String contextPath = System.getProperty(xdm_config_context_file);
        logger.info("Starting XDM node with Context [{}]", contextPath);
    	
        context = new ClassPathXmlApplicationContext(contextPath);
        HazelcastInstance hz = context.getBean(hz_instance, HazelcastInstance.class);
        hz.getUserContext().put(app_context, context);
    	Member local = hz.getCluster().getLocalMember();
        String name = local.getStringAttribute(xdm_cluster_node_name);
        String role = local.getStringAttribute(xdm_cluster_node_role);
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
    
    private static void initAdminNode(HazelcastInstance hzInstance) {
    	
    	String xport = ((Member) hzInstance.getLocalEndpoint()).getStringAttribute(xdm_cluster_admin_port);
    	int port = Integer.parseInt(xport);
    	JMXServiceURL url;
		try {
			url = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:" + xport + "/jmxrmi");
		} catch (MalformedURLException ex) {
			logger.warn("error creating JMX URL: {}", ex.getMessage());
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
		logger.info("JMX connector server started and listening on port: {}", port);
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

        String[] aSchemas = getMemberSchemas(local);
        
        for (String name: aSchemas) {
          	String schemaName = name.trim();
       		logger.debug("initServerNode; going to deploy schema: {}", schemaName);
       		boolean initialized = false;
       		if (schemaCache != null) {
            	Schema xSchema = schemaCache.get(schemaName);
            	if (xSchema != null) {
            		initialized = initSchema(systemInstance, local, xSchema);
            		//String store = xSchema.getProperty(xdm_schema_store_enabled);
            		ApplicationContext schemaContext = (ApplicationContext) getContext(schemaName, schema_context);
            		if (initialized) {
            			// set modules and libraries
            			RepositoryImpl xdmRepo = schemaContext.getBean(bean_id, RepositoryImpl.class);
            			xdmRepo.setLibraries(cLibraries);
            			for (Module module: cModules) {
            				try {
								ModuleCacheStore.loadModule(module);
							} catch (IOException e) {
			            		logger.warn("initServerNode; cannot load Module {} for schema '{}'!", module, schemaName);
							}
            			}
            			xdmRepo.setModules(cModules);
            			xdmRepo.setDataFormats(cFormats);
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
		logger.info("initSchema.exit; schema {} {}initialized", schema, ok ? "" : "NOT ");
		return ok;
	}
    
    private static boolean isAdminRole(String role) {
        return "admin".equalsIgnoreCase(role);
    }
    
}
