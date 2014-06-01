package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.bagri.xdm.process.hazelcast.schema.SchemaCreator;
import com.bagri.xdm.process.hazelcast.schema.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.schema.SchemaRemover;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=SchemaManagement", 
	description="Schema Management MBean")
public class SchemaManagement extends EntityManagement<String, XDMSchema> implements InitializingBean, 
	MembershipListener, XDMSchemaManagement {
	
	private IExecutorService execService;
	
	private Properties defaults; 
    private Map<String, XDMSchemaDictionary> dictCache = new HashMap<String, XDMSchemaDictionary>(); 

    public SchemaManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
		hzInstance.getCluster().addMembershipListener(this);
	}
    
    public void setExecService(IExecutorService execService) {
    	this.execService = execService;
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = entityCache.keySet();
		logger.debug("afterPropertiesSet.enter; got schemas: {}", names); 
		
        //String schemas = hz.getCluster().getLocalMember().getStringAttribute(op_node_schemas);
    	String schemaStr = System.getProperty(XDMNode.op_node_schemas);
    	String[] schemas = schemaStr.split(" ");

    	String roleStr = System.getProperty(XDMNode.op_node_role);
    	//String[] roles = roleStr.split(" ");
    	boolean isAdmin = XDMNode.NodeRole.isAdminRole(roleStr);

		for (String name: names) {
        	XDMSchema schema = entityCache.get(name);
        	boolean initialized = false;
        	boolean localSchema = Arrays.binarySearch(schemas, name) >= 0; 
        	if (localSchema) {
           		initialized = initSchema(name, schema.getProperties());
        	}
        	
       		EntityManager<XDMSchema> sMgr = mgrCache.get(name);
       		if (sMgr == null) {
       			if (localSchema || isAdmin) {
       				logger.debug("afterPropertiesSet; cannot get SchemaManager for schema {}; initializing a new one", name); 
       				try {
       					sMgr = initEntityManager(name);
       				} catch (MBeanExportException | MalformedObjectNameException ex) {
       					// JMX registration failed.
       					logger.error("afterPropertiesSet.error: ", ex);
       				}
       			}
       		}
   			if (sMgr != null) {
   				if (localSchema) {
   					if (!initialized) {
   		   				((SchemaManager) sMgr).setState("Failed schema initialization");
   					}
   				} else {
   					((SchemaManager) sMgr).setState("External schema; not initialized");
   				}
   			}
        }
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
	
	@Override
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
			props = FileUtils.propsFromString(properties);
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
	
	@Override
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
	
	@Override
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
	protected EntityManager<XDMSchema> createEntityManager(String userName) {
		SchemaManager mgr = new SchemaManager(this, userName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}
	
	@Override
	protected EntityManager<XDMSchema> initEntityManager(String schemaName) throws MalformedObjectNameException {
		SchemaManager sMgr = (SchemaManager) super.initEntityManager(schemaName);
		if (sMgr != null) {
			XDMSchemaDictionary schemaDict = dictCache.get(schemaName);
			sMgr.setSchemaDictionary(schemaDict);
		}
   	    //if (!mgrCache.containsKey(schemaName)) {
		//	sMgr = new SchemaManager(this, schemaName);
		//	sMgr.setSchemaCache(entityCache);
		//	XDMSchemaDictionary schemaDict = dictCache.get(schemaName);
		//	sMgr.setSchemaDictionary(schemaDict);
		//	mgrCache.put(schemaName, sMgr);
		//	mbeanExporter.registerManagedResource(sMgr, sMgr.getObjectName());
		//}
   	    return sMgr;
	}

	@Override
	public boolean initSchema(String schemaName, Properties props) {
    	logger.debug("initSchema.enter; schema: {}; properties: {}", schemaName, props);
    	
    	props.setProperty("xdm.schema.name", schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
    	try {
    		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
    		ctx.getEnvironment().getPropertySources().addFirst(pps);
    		ctx.setConfigLocation("spring/schema-client-context.xml");
    		ctx.refresh();

    		HazelcastInstance hz = ctx.getBean("hzInstance", HazelcastInstance.class);
    		hz.getUserContext().put("appContext", ctx);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
    		//hz.getConfig().getSecurityConfig().s
    	    XDMSchemaDictionary schemaDict = ctx.getBean("xdmDictionary", XDMSchemaDictionary.class);
    	    SchemaManager sMgr = (SchemaManager) mgrCache.get(schemaName);
       	    if (sMgr != null) {
       	    	sMgr.setSchemaDictionary(schemaDict);
       	    } else {
       	    	dictCache.put(schemaName, schemaDict);
       	    }
    		logger.debug("initSchema.exit; schema {} started on instance: {}", schemaName, hz);
    		return true;
    	} catch (Exception ex) {
    		logger.error("initSchema.error; " + ex.getMessage(), ex);
    		return false;
    	}
	}
	
	@Override
	public boolean denitSchema(String schemaName) {
    	logger.debug("denitSchema.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) hz.getUserContext().get("appContext");
			// closed via context anyway
			//hz.getLifecycleService().shutdown();
			ctx.close();
			result = true;
		}
    	logger.debug("denitSchema.exit; schema {} deactivated: {}", schemaName, result);
		return result;
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.trace("memberAdded.enter; event: {}", membershipEvent);
		// get schemas; for each schema registered to this member
		Member member = membershipEvent.getMember();
		String schemas = member.getStringAttribute(XDMNode.op_node_schemas);
		int cnt = 0;
		if (schemas == null) {
			schemas = "TPoX";
		}
		String[] aSchemas = schemas.split(" ");
		for (String name: aSchemas) {
			XDMSchema schema = entityCache.get(name);
			if (schema != null) {
				SchemaInitiator init = new SchemaInitiator(schema.getName(), schema.getProperties());
				Future<Boolean> result = execService.submitToMember(init, member);
				Boolean ok = false;
				try {
					ok = result.get();
					if (ok) {
						if (initSchema(schema.getName(), schema.getProperties())) {
							cnt++;
						}
					}
					logger.debug("memberAdded; Schema {}initialized on node {}", ok ? "" : "NOT ", member);
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("memberAdded.error; ", ex);
				}
			}
		}
		logger.trace("memberAdded.exit; {} schemas initialized", cnt);
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.trace("memberRemoved.enter; event: {}", membershipEvent);
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChanged.enter; event: {}", memberAttributeEvent);
	}

}
