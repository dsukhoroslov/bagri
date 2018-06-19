package com.bagri.server.hazelcast.management;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.system.Schema;
import com.bagri.rest.BagriRestServer;
import com.bagri.server.hazelcast.task.schema.SchemaCreator;
import com.bagri.server.hazelcast.task.schema.SchemaMemberExtractor;
import com.bagri.server.hazelcast.task.schema.SchemaRemover;
import com.bagri.support.util.JMXUtils;
import com.bagri.support.util.PropUtils;
import com.hazelcast.cluster.MemberAttributeOperationType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

import static com.bagri.core.Constants.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.getMemberSchemas;

@ManagedResource(objectName="com.bagri.db:type=Management,name=SchemaManagement", 
	description="Schema Management MBean")
public class SchemaManagement extends EntityManagement<Schema> implements MembershipListener { 
	
	private BagriRestServer restServer;
	private IExecutorService execService;
	private ClusterManagement srvCluster;
	private UserManagement srvUser;
	
	private Properties defaults; 
    private Map<String, ClassPathXmlApplicationContext> ctxCache = new HashMap<String, ClassPathXmlApplicationContext>(); 

    public SchemaManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
		hzInstance.getCluster().addMembershipListener(this);
	}
    
    public ClusterManagement getClusterService() {
    	return srvCluster;
    }
    
    public void setClusterService(ClusterManagement srvCluster) {
    	this.srvCluster = srvCluster;
    }
	
    public void setExecService(IExecutorService execService) {
    	this.execService = execService;
    }
	
    public BagriRestServer getRestService() {
    	return restServer;
    }
    
    public void setRestService(BagriRestServer restServer) {
    	this.restServer = restServer;
    }
	
    public void setUserService(UserManagement srvUser) {
    	this.srvUser = srvUser;
    }
	
	public String getDefaultProperty(String name) {
		return defaults.getProperty(name);
	}
	
	@ManagedAttribute(description="Default Schema Properties")
	public CompositeData getDefaultProperties() {
		return JMXUtils.propsToComposite("Schema defaults", "Schema defaults", defaults);
	}
	
	@ManagedOperation(description="Set Default Property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Property name"),
		@ManagedOperationParameter(name = "value", description = "Property value")})
	public void setDefaultProperty(String name, String value) {
		defaults.setProperty(name, value);
	}
	
	public void setDefaultProperties(Properties defaults) {
		this.defaults = new Properties(defaults);
	}
	
	@ManagedAttribute(description="Registered Schema Names")
	public String[] getSchemaNames() {
		return getEntityNames();
	}
	
	@ManagedAttribute(description="Return registered Schemas")
	public TabularData getSchemas() {
		return getEntities("schema", "Schema definition");
    }
	
	public Schema getSchema(String schemaName) {
		return entityCache.get(schemaName);
	}

	public Collection<Schema> getSchemas2() {
		return super.getEntities();
	}

	@ManagedOperation(description="Create new Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name"),
		@ManagedOperationParameter(name = "desription", description = "Schema description"),
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by semicolon")})
	public boolean addSchema(String schemaName, String description, String properties) {
		Properties props;
		try {
			props = PropUtils.propsFromString(properties);
		} catch (IOException ex) {
			logger.error("createSchema.error: ", ex);
			return false;
		}
		
		// set defaults for absent properties
		for (String prop: defaults.stringPropertyNames()) {
			if (!props.containsKey(prop)) {
				props.setProperty(prop, defaults.getProperty(prop));
			}
		}
		return createSchema(schemaName, description, props) != null;
	}
	
	@ManagedOperation(description="Destroy Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name")})
	public boolean deleteSchema(String schemaName) {
		return removeSchema(schemaName) != null;
	}
	
	public Schema createSchema(String schemaName, String description, Properties props) {
		Schema schema = null;
		if (!entityCache.containsKey(schemaName)) {
			// get current user from context...
			String user = srvUser.getCurrentUser();
	    	Object result = entityCache.executeOnKey(schemaName, new SchemaCreator(user, description, props));
	    	logger.debug("addSchema; execution result: {}", result);
	    	schema = (Schema) result;
		}
		return schema;
	}
	
	public Schema removeSchema(String schemaName) {
		Schema schema = entityCache.get(schemaName);
		if (schema != null) {
			String user = srvUser.getCurrentUser();
	    	Object result = entityCache.executeOnKey(schemaName, new SchemaRemover(schema.getVersion(), user));
	    	logger.debug("deleteSchema; execution result: {}", result);
	    	schema = (Schema) result;
		}
		return schema;
	}
	
	@Override
	protected EntityManager<Schema> createEntityManager(String schemaName) {
		SchemaManager mgr = new SchemaManager(hzInstance, schemaName, this);
		mgr.setEntityCache(entityCache);
		return mgr;
	}
	
	public boolean initSchema(String schemaName, Properties props) {
    	logger.debug("initSchema.enter; schema: {}; properties: {}", schemaName, props);
    	
    	ClassPathXmlApplicationContext ctx = ctxCache.get(schemaName);
    	if (ctx != null) {
        	logger.debug("initSchema; schema {} already initialized", schemaName);
    		return true; 
    	}

    	props.setProperty(pn_schema_name, schemaName);
		String port = props.getProperty(pn_schema_ports_first);
    	String members = props.getProperty(pn_schema_members);
    	String[] servers = members.split(", ");
    	for (int i=0; i < servers.length; i++) {
   			props.setProperty(pn_schema_members, servers[i] + ":" + port);
   			PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
	    	try {
	    		ctx = new ClassPathXmlApplicationContext();
	    		ctx.getEnvironment().getPropertySources().addFirst(pps);
	            String contextPath = System.getProperty(pn_config_path);
	    		ctx.setConfigLocation("file:" + contextPath + "/spring/admin-schema-context.xml");
	    		ctx.refresh();
	    		
	    		ctxCache.put(schemaName, ctx);
	    	    SchemaManager sMgr = (SchemaManager) mgrCache.get(schemaName);
	       	    if (sMgr != null) {
	       	    	setupXQConnection(ctx);
       		    	sMgr.setClientContext(ctx);
	       	    	registerFeatureManagers(ctx, sMgr);
	       	    }
	       	    
	    		logger.debug("initSchema.exit; client schema {} started", schemaName);
	    		return true;
	    	} catch (Exception ex) {
	    		logger.error("initSchema.error; " + ex.getMessage(), ex);
	    	}
    	}
		return false;
	}
	
	private void setupXQConnection(ApplicationContext ctx) throws XQException {
		XQDataSource xqds = ctx.getBean("xqDataSource", XQDataSource.class);
		String username = srvUser.getCurrentUser();
		String password = srvUser.getUserPassword(username);
		if (password == null) {
			throw new XQException("no credentials found for user " + username);
		}
		
		XQConnection xqConn = xqds.getConnection(username, password);
		QueryManagement qMgr = ctx.getBean("queryManager", QueryManagement.class);
	    qMgr.setXQConnection(xqConn);
	}
	
	public boolean denitSchema(String schemaName, Set<Member> members) {
    	logger.debug("denitSchema.enter; schema: {}", schemaName);
    	boolean result = false;
    	
    	// do this if we don't have schema nodes any more!
    	ClassPathXmlApplicationContext ctx = ctxCache.get(schemaName);
    	if (ctx != null) {
    		//HazelcastInstance hzClient = ctx.getBean(hz_instance, HazelcastInstance.class);
    		//int size = hzClient.getCluster().getMembers().size();
    		if (!isSchemaActive(schemaName, members)) {
       		//if (size == 0) {
    			try {
    				unregisterFeatureManagers(ctx);
    				//hzClient.shutdown();

        			ctx.close();
        			ctxCache.remove(schemaName);
        			SchemaManager sMgr = (SchemaManager) mgrCache.get(schemaName);
        			if (sMgr != null) {
        				sMgr.setClientContext(null);
        			}
        			result = true;
    			} catch (Exception ex) {
					logger.error("denitSchema.error; ", ex);
				}
       	    }			
		}
    	logger.debug("denitSchema.exit; schema {} deactivated: {}", schemaName, result);
		return result;
	}
	
	private void registerFeatureManagers(ApplicationContext ctx, SchemaManager sMgr) throws MBeanExportException, MalformedObjectNameException {
	    ClientManagement cMgr = ctx.getBean("clientManager", ClientManagement.class);
	    cMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(cMgr, cMgr.getObjectName());
	    DocumentManagement dMgr = ctx.getBean("docManager", DocumentManagement.class);
	    dMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(dMgr, dMgr.getObjectName());
	    IndexManagement iMgr = ctx.getBean("indexManager", IndexManagement.class);
	    iMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(iMgr, iMgr.getObjectName());
	    TriggerManagement trMgr = ctx.getBean("triggerManager", TriggerManagement.class);
	    trMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(trMgr, trMgr.getObjectName());
		ModelManagement mMgr = ctx.getBean("modelManager", ModelManagement.class);
	    mMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(mMgr, mMgr.getObjectName());
	    PopulationManagement pMgr = ctx.getBean("popManager", PopulationManagement.class);
	    pMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(pMgr, pMgr.getObjectName());
		QueryManagement qMgr = ctx.getBean("queryManager", QueryManagement.class);
	    qMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(qMgr, qMgr.getObjectName());
	    ResourceManagement rMgr = ctx.getBean("resourceManager", ResourceManagement.class);
	    rMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(rMgr, rMgr.getObjectName());
		TransactionManagement tMgr = ctx.getBean("transManager", TransactionManagement.class);
	    tMgr.setSchemaManager(sMgr);
		mbeanExporter.registerManagedResource(tMgr, tMgr.getObjectName());
	}
	
	private void unregisterFeatureManagers(ApplicationContext ctx) throws MalformedObjectNameException {
		ClientManagement cMgr = ctx.getBean("clientManager", ClientManagement.class);
		mbeanExporter.unregisterManagedResource(cMgr.getObjectName());
		DocumentManagement dMgr = ctx.getBean("docManager", DocumentManagement.class);
		mbeanExporter.unregisterManagedResource(dMgr.getObjectName());
		IndexManagement iMgr = ctx.getBean("indexManager", IndexManagement.class);
		mbeanExporter.unregisterManagedResource(iMgr.getObjectName());
		TriggerManagement trMgr = ctx.getBean("triggerManager", TriggerManagement.class);
		mbeanExporter.unregisterManagedResource(trMgr.getObjectName());
		ModelManagement mMgr = ctx.getBean("modelManager", ModelManagement.class);
		mbeanExporter.unregisterManagedResource(mMgr.getObjectName());
	    PopulationManagement pMgr = ctx.getBean("popManager", PopulationManagement.class);
		mbeanExporter.unregisterManagedResource(pMgr.getObjectName());
		QueryManagement qMgr = ctx.getBean("queryManager", QueryManagement.class);
		mbeanExporter.unregisterManagedResource(qMgr.getObjectName());
	    ResourceManagement rMgr = ctx.getBean("resourceManager", ResourceManagement.class);
		mbeanExporter.unregisterManagedResource(rMgr.getObjectName());
		TransactionManagement tMgr = ctx.getBean("transManager", TransactionManagement.class);
		mbeanExporter.unregisterManagedResource(tMgr.getObjectName());
	}

	private boolean isSchemaActive(String schemaName, Set<Member> members) {

		// does not work via cluster size for some reason..
		for (Member member: members) {
			logger.trace("isSchemaActive; options: {}; on Member: {}", member.getAttributes(), member);
			String[] aSchemas = getMemberSchemas(member);
			for (String name: aSchemas) {
	    		if (schemaName.equals(name.trim())) {
	    			// check that node is not removed one
	    			return true;
		    	}
			}
		}
		return false;
	}
	
	public int initMember(final Member member) {
		
		// get schemas; for each schema registered on this member

		int cnt = 0;
		String[] aSchemas = getMemberSchemas(member);
		for (String name: aSchemas) {
			Schema schema = entityCache.get(name);
			if (schema != null) {
				Properties props = schema.getProperties();
				if (initSchema(schema.getName(), props)) {
					cnt++;
					Future<String> future = execService.submitToMember(new SchemaMemberExtractor(name), member);
					try {
						String uuid = future.get(); //10, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException ex) { // | TimeoutException ex) {
						logger.error("initMember.error 1; ", ex);
						continue;
					}
				}
			} else {
				logger.info("initMember.error; no schema found for name: {}; " + 
						"looks like invalid node configuration", name);
			}
		}
		
		return cnt;
	}

	public int denitMember(Set<Member> members, Member member) {
		int cnt = 0;
		String[] aSchemas = getMemberSchemas(member);
		for (String name: aSchemas) {
			Schema schema = entityCache.get(name);
			if (schema != null) {
				// use there membershipEvent.members() !!
				if (denitSchema(name, members)) {
					cnt++;
					logger.debug("memberRemoved; Schema {} de-initialized on node {}", name, member);
				}
			}
		}
		return cnt;
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.trace("memberAdded.enter; event: {}", membershipEvent);
		//int cnt = initMember(membershipEvent.getMember());
		//logger.trace("memberAdded.exit; {} clients initialized", cnt);
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.trace("memberRemoved.enter; event: {}", membershipEvent);
		int cnt = denitMember(membershipEvent.getMembers(), membershipEvent.getMember());
		logger.trace("memberRemoved.exit; {} schemas de-initialized", cnt);
	}
	
	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChanged.enter; event: {}; attribute: {}; value: {}", 
				memberAttributeEvent, memberAttributeEvent.getKey(), memberAttributeEvent.getValue());
		// if attribute is schemas then deploy schema on member ?
		if (pn_cluster_node_schemas.equals(memberAttributeEvent.getKey())) {
			Member member = memberAttributeEvent.getMember();
			String nodeName = member.getStringAttribute(pn_cluster_node_name);
			if (memberAttributeEvent.getOperationType() == MemberAttributeOperationType.PUT) {
				// set
				String newSchemas = (String) memberAttributeEvent.getValue();
				// do this via memberAdded??
			} else {
				// remove all
			}
		}
	}

}
