package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.common.util.PropUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.common.StringStringKey;
import com.bagri.xdm.process.hazelcast.schema.SchemaCreator;
import com.bagri.xdm.process.hazelcast.schema.SchemaMemberExtractor;
import com.bagri.xdm.process.hazelcast.schema.SchemaRemover;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.cluster.MemberAttributeOperationType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

import static com.bagri.xdm.process.hazelcast.util.HazelcastUtils.getMemberSchemas;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=SchemaManagement", 
	description="Schema Management MBean")
public class SchemaManagement extends EntityManagement<String, XDMSchema> implements MembershipListener { 
	
	private IExecutorService execService;
	private ClusterManagement srvCluster;
	
	private Properties defaults; 
	private Map<StringStringKey, String> memberMap = new HashMap<StringStringKey, String>();
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
		this.defaults = defaults;
	}
	
	@ManagedAttribute(description="Registered Schema Names")
	public String[] getSchemaNames() {
		return entityCache.keySet().toArray(new String[0]);
	}
	
	public XDMSchema getSchema(String schemaName) {
		return entityCache.get(schemaName);
	}

	public Collection<XDMSchema> getSchemas() {
		return super.getEntities();
	}

	@ManagedOperation(description="Create new Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name"),
		@ManagedOperationParameter(name = "desription", description = "Schema description"),
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by comma")})
	public boolean createSchema(String schemaName, String description, String properties) {

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
		return addSchema(schemaName, description, props) != null;
	}
	
	@ManagedOperation(description="Destroy Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name")})
	public boolean destroySchema(String schemaName) {
		return deleteSchema(schemaName) != null;
	}
	
	public XDMSchema addSchema(String schemaName, String description, Properties props) {
		XDMSchema schema = null;
		if (!entityCache.containsKey(schemaName)) {
			// get current user from context...
			String user = JMXUtils.getCurrentUser();
	    	Object result = entityCache.executeOnKey(schemaName, new SchemaCreator(user, description, props));
	    	logger.debug("addSchema; execution result: {}", result);
	    	schema = (XDMSchema) result;
		}
		return schema;
	}
	
	public XDMSchema deleteSchema(String schemaName) {
		XDMSchema schema = entityCache.get(schemaName);
		if (schema != null) {
			String user = JMXUtils.getCurrentUser();
	    	Object result = entityCache.executeOnKey(schemaName, new SchemaRemover(schema.getVersion(), user));
	    	logger.debug("deleteSchema; execution result: {}", result);
	    	schema = (XDMSchema) result;
		}
		return schema;
	}
	
	@Override
	protected EntityManager<XDMSchema> createEntityManager(String schemaName) {
		SchemaManager mgr = new SchemaManager(this, schemaName);
		mgr.setEntityCache(entityCache);
		//XDMSchemaDictionary schemaDict = dictCache.get(schemaName);
		//mgr.setSchemaDictionary(schemaDict);
		return mgr;
	}

	private void adjustConnectionProps(Properties props) {
		String members = props.getProperty("xdm.schema.members");
		String[] servers = members.split(", ");
		if (servers.length > 0) {
			String port = props.getProperty("xdm.schema.ports.first");
			if (port != null) {
				props.setProperty("xdm.schema.members", servers[0] + ":" + port);
			}
		}
	}
	
	public HazelcastInstance initSchema(String schemaName, Properties props) {
    	logger.debug("initSchema.enter; schema: {}; properties: {}", schemaName, props);
    	
    	ClassPathXmlApplicationContext ctx = ctxCache.get(schemaName);
    	if (ctx != null) {
        	logger.debug("initSchema; schema {} already initialized", schemaName);
    		return ctx.getBean("hzInstance", HazelcastInstance.class);
    	}
    	
    	adjustConnectionProps(props);
    	props.setProperty("xdm.schema.name", schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
    	try {
    		ctx = new ClassPathXmlApplicationContext();
    		ctx.getEnvironment().getPropertySources().addFirst(pps);
            //String contextPath = System.getProperty("xdm.config.context.file");
    		ctx.setConfigLocation("spring/schema-admin-context.xml");
    		ctx.refresh();
    		
    		ctxCache.put(schemaName, ctx);
    		HazelcastInstance hz = ctx.getBean("hzInstance", HazelcastInstance.class);
    		//hz.getUserContext().put("appContext", ctx);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
    	    XDMSchemaDictionary schemaDict = ctx.getBean("xdmDictionary", XDMSchemaDictionary.class);
    	    SchemaManager sMgr = (SchemaManager) mgrCache.get(schemaName);
       	    if (sMgr != null) {
       	    	sMgr.setClientContext(ctx);
       	    	sMgr.setSchemaDictionary(schemaDict);
       	    	
        	    DocumentManagement dMgr = ctx.getBean("docManager", DocumentManagement.class);
				mbeanExporter.registerManagedResource(dMgr, dMgr.getObjectName());
        	    QueryManagement qMgr = ctx.getBean("queryManager", QueryManagement.class);
				mbeanExporter.registerManagedResource(qMgr, qMgr.getObjectName());
       	    } else {
       	    	//dictCache.put(schemaName, schemaDict);
       	    }
       	    
    		logger.debug("initSchema.exit; client schema {} started on instance: {}", schemaName, hz);
    		return hz;
    	} catch (Exception ex) {
    		logger.error("initSchema.error; " + ex.getMessage(), ex);
    		return null;
    	}
	}
	
	public boolean denitSchema(String schemaName, Set<Member> members) {
    	logger.debug("denitSchema.enter; schema: {}", schemaName);
    	boolean result = false;
    	
    	// do this if we don't have schema nodes any more!
    	ClassPathXmlApplicationContext ctx = ctxCache.get(schemaName);
    	if (ctx != null) {
    		HazelcastInstance hzClient = ctx.getBean("hzInstance", HazelcastInstance.class);
    		//int size = hzClient.getCluster().getMembers().size();
    		if (!isSchemaActive(schemaName, members)) {
       		//if (size == 0) {
    			try {
    				DocumentManagement dMgr = ctx.getBean("docManager", DocumentManagement.class);
    				mbeanExporter.unregisterManagedResource(dMgr.getObjectName());
    				QueryManagement qMgr = ctx.getBean("queryManager", QueryManagement.class);
    				mbeanExporter.unregisterManagedResource(qMgr.getObjectName());
    				
    				hzClient.shutdown();

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
			XDMSchema schema = entityCache.get(name);
			if (schema != null) {
				HazelcastInstance hzClient = initSchema(schema.getName(), schema.getProperties());
				if (hzClient != null) {
					cnt++;
					Future<String> future = execService.submitToMember(new SchemaMemberExtractor(name), member);
					String uuid;
					try {
						uuid = future.get(); //10, TimeUnit.SECONDS);
					} catch (InterruptedException | ExecutionException ex) { // | TimeoutException ex) {
						logger.error("initMember.error 1; ", ex);
						continue;
					}
					
					memberMap.put(new StringStringKey(member.getUuid(), name), uuid);
						
					try {
						DocumentManager dMgr = new DocumentManager(hzClient, name, uuid); 
						mbeanExporter.registerManagedResource(dMgr, dMgr.getObjectName());

						QueryManager qMgr = new QueryManager(name, uuid); 
						mbeanExporter.registerManagedResource(qMgr, qMgr.getObjectName());
					} catch (MalformedObjectNameException ex) {
						logger.error("initMember.error 2; ", ex);
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
			XDMSchema schema = entityCache.get(name);
			if (schema != null) {
				// use there membershipEvent.members() !!
				if (denitSchema(name, members)) {
					cnt++;
					logger.debug("memberRemoved; Schema {} de-initialized on node {}", name, member);
				}
				
				// we de-register mbeans for removed member
				String uuid = memberMap.get(new StringStringKey(member.getUuid(), name));
				if (uuid != null) {
					try {
						ObjectName queryName = JMXUtils.getObjectName("type=Schema,name=" + name + 
								",kind=QueryManagement,node=" + uuid);
						mbeanExporter.unregisterManagedResource(queryName);
		
						ObjectName docName = JMXUtils.getObjectName("type=Schema,name=" + name + 
								",kind=DocumentManagement,node=" + uuid);
						mbeanExporter.unregisterManagedResource(docName);
					} catch (MalformedObjectNameException ex) {
						logger.error("denitMember.error; ", ex);
					}
				} else {
					logger.info("denitMember; can't get member '{}:{}' mapping, skipping de-registration", 
							member.getUuid(), name);
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
		if (XDMNode.op_node_schemas.equals(memberAttributeEvent.getKey())) {
			Member member = memberAttributeEvent.getMember();
			String nodeName = member.getStringAttribute(XDMNode.op_node_name);
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
