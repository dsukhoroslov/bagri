package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMSchema;
import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.bagri.xdm.process.hazelcast.SchemaDenitiator;
import com.bagri.xdm.process.hazelcast.SchemaInitiator;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=SchemaManagement", 
	description="Schema Management MBean")
public class SchemaManagement implements InitializingBean, XDMSchemaManagement {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManagement.class);
	private static final String schema_management = "SchemaManagement";
    
	private Properties defaults; 
    private HazelcastInstance hzInstance;
	private IExecutorService execService;
    private IMap<String, XDMSchema> schemaCache;
    
	public SchemaManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = schemaCache.keySet();
        for (String name: names) {
        	XDMSchema schema = schemaCache.get(name);
        	if (schema.isActive()) {
        		initSchema(schema);
        	}
        }
		
		//JMXUtils.registerMBean(schema_management, this);
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
		return schemaCache.keySet().toArray(new String[0]);
	}
	
	@Override
	public Collection<XDMSchema> getSchemas() {
		return new ArrayList<XDMSchema>(schemaCache.values());
	}

	@ManagedOperation(description="Activate/Deactivate Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name"),
		@ManagedOperationParameter(name = "activate", description = "Activate/Deactivate schema")})
	public boolean activateSchema(String schemaName, boolean activate) {
		XDMSchema schema = schemaCache.get(schemaName);
		if (schema != null) {
			if (activate) {
				if (!schema.isActive()) {
					if (initSchemaInCluster(schema) > 0) {
						// load all schema data from PS
						schema.setActive(activate);
						schemaCache.put(schemaName, schema);
						return true;
					}
				}
			} else {
				if (schema.isActive()) {
					// compare with number of schema nodes!!
					if (denitSchemaInCluster(schema) > 0) {
						schema.setActive(activate);
						schemaCache.put(schemaName, schema);
						return true;
					}
				}
			}
		}
		return false;
	}

	@ManagedOperation(description="Create new Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name"),
		@ManagedOperationParameter(name = "desription", description = "Schema description"),
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by comma")})
	public boolean createSchema(String schemaName, String description, String properties) {
		Properties props = new Properties();
		properties = properties.replaceAll(";", "\n\r");
		try {
			props.load(new StringReader(properties));
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
	
	public void setExecService(IExecutorService execService) {
		this.execService = execService;
	}

	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
	}

	@Override
	public XDMSchema addSchema(String schemaName, String description, Properties props) {
		XDMSchema schema = null;
		if (!schemaCache.containsKey(schemaName)) {
			schema = new XDMSchema(schemaName, description, true, new Date(), schema_management, props);
			//if (initSchema(schema)) {
			if (initSchemaInCluster(schema) > 0) {
				schemaCache.put(schemaName, schema);
			}
		}
		return schema;
	}
	
	private int initSchemaInCluster(XDMSchema schema) {
		
		logger.trace("initSchemaInCluster.enter; schema: {}", schema);
		SchemaInitiator init = new SchemaInitiator(schema.getName(), schema.getProperties());
		
		int cnt = 0;
        //Set<Member> members = hzInstance.getCluster().getMembers();
        //for (Member member: members) {
        //	Future<Boolean> result = execService.submitToMember(init, member);
		//	try {
		//		Boolean ok = result.get();
		//		if (ok) cnt++;
		//		logger.debug("initSchemaInCluster; Schema {}initialized on node {}", ok ? "" : "NOT ", member);
		//	} catch (InterruptedException | ExecutionException ex) {
		//		logger.error("initSchemaInCluster.error; ", ex);
		//	}
        //}
		Map<Member, Future<Boolean>> result = execService.submitToAllMembers(init);
		for (Map.Entry<Member, Future<Boolean>> entry: result.entrySet()) {
			try {
				Boolean ok = entry.getValue().get();
				if (ok) cnt++;
				logger.debug("initSchemaInCluster; Schema {}initialized on node {}", ok ? "" : "NOT ", entry.getKey());
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("initSchemaInCluster.error; ", ex);
			}
		}

		logger.info("initSchemaInCluster.exit; schema {} initialized on {} nodes", schema, cnt);
		return cnt;
	}

	private int denitSchemaInCluster(XDMSchema schema) {

		logger.trace("denitSchemaInCluster.enter; schema: {}", schema);
		SchemaDenitiator denit = new SchemaDenitiator(schema.getName());
		
		int cnt = 0;
		Map<Member, Future<Boolean>> result = execService.submitToAllMembers(denit);
		for (Map.Entry<Member, Future<Boolean>> entry: result.entrySet()) {
			try {
				Boolean ok = entry.getValue().get();
				if (ok) cnt++;
				logger.debug("denitSchemaInCluster; Schema {}de-initialized on node {}", ok ? "" : "NOT ", entry.getKey());
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("denitSchemaInCluster.error; ", ex);
			}
		}
		logger.info("denitSchemaInCluster.exit; schema {} de-initialized on {} nodes", schema, cnt);
		return cnt;
	}

	@Override
	public boolean initSchema(String schemaName, Properties props) {
    	logger.debug("initSchema.enter; schema: {}; properties: {}", schemaName, props);
    	
    	props.setProperty("xdm.schema.name", schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
    	ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
    	ctx.getEnvironment().getPropertySources().addFirst(pps);
    	ctx.setConfigLocation("spring/schema-context.xml");
    	ctx.refresh();
    	
        SchemaManager sMgr = ctx.getBean("schemaManager", SchemaManager.class);
        sMgr.setSchemaCache(schemaCache);
        HazelcastInstance hz = ctx.getBean("hzInstance", HazelcastInstance.class);
        hz.getUserContext().put("schemaManager", sMgr);
        hz.getUserContext().put("appContext", ctx);
        //hz.getConfig().getSecurityConfig().setEnabled(true);
        //hz.getConfig().getSecurityConfig().s
        
    	logger.debug("initSchema.exit; schema {} started on instance: {}; config: {}", schemaName, hz, hz.getConfig());
    	return true;
	}
	
	private boolean initSchema(XDMSchema schema) {
		return initSchema(schema.getName(), schema.getProperties());
	}

	@Override
	public XDMSchema deleteSchema(String schemaName) {
		XDMSchema schema = schemaCache.get(schemaName);
		if (schema != null) {
			if (schema.isActive()) {
				// compare with number of nodes assigned to the schema!
				if (denitSchemaInCluster(schema) > 0) {
					schemaCache.remove(schemaName);
				}
			} else {
				schemaCache.remove(schemaName);
			}
		}
		return schema;
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

	// implement it...
	public String[] getNodeSchemas(String node) {
		return null;
	}

}
