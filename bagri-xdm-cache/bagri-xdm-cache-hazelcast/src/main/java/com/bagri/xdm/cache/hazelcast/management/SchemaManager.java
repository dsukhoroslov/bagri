package com.bagri.xdm.cache.hazelcast.management;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.process.hazelcast.SchemaDenitiator;
import com.bagri.xdm.process.hazelcast.SchemaInitiator;
import com.bagri.xdm.process.hazelcast.SchemaProcessor;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Manager MBean")
public class SchemaManager extends XDMSchemaManagerBase implements SelfNaming {

    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManager.class);
    private static final String type_schema = "Schema";
	
    private HazelcastInstance hzInstance;
	private IExecutorService execService;
    private IMap<String, XDMSchema> schemaCache;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(HazelcastInstance hzInstance, String schemaName) {
		super();
		this.hzInstance = hzInstance;
		this.schemaName = schemaName;
	}
	
	public void setExecService(IExecutorService execService) {
		this.execService = execService;
	}
	
	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
	}
	
	@ManagedAttribute(description="Returns Document Types registered in the Schema")
	public String[] getRegisteredTypes() {
		Collection<XDMDocumentType> types = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes();
		String[] result = new String[types.size()];
		Iterator<XDMDocumentType> itr = types.iterator();
		for (int i=0; i < types.size(); i++) {
			result[i] = itr.next().getRootPath();
		}
		Arrays.sort(result);
		return result;
	}

	@ManagedAttribute(description="Returns Schema state")
	public boolean isActive() {
		return getSchema().isActive();
	}
	
	@ManagedAttribute(description="Returns Schema persistence type")
	public String getPersistenceType() {
		String result = getSchema().getProperty("xdm.schema.store.type");
		if (result == null) {
			result = "NONE";
		}
		return result;
	}

	@Override
	@ManagedAttribute(description="Returns registered Schema name")
	public String getSchemaName() {
		return super.getSchemaName();
	}
	
	@Override
	protected XDMSchema getSchema() {
		XDMSchema schema = schemaCache.get(schemaName);
		logger.trace("getSchema. returning: {}", schema);
		return schema;
	}

	@ManagedAttribute(description="Returns registered Schema properties")
	public CompositeData getSchemaProperties() {
		return super.getAllProperties();
	}
	
	@Override
	protected void flushSchema(XDMSchema schema) {
		schemaCache.put(schemaName, schema);
	}
	
	@ManagedOperation(description="Activate/Deactivate Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "activate", description = "Activate/Deactivate")})
	public boolean activateSchema(boolean activate) {
		XDMSchema schema = getSchema();
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

	@ManagedOperation(description="Register Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaFile", description = "A full path to XSD file to register")})
	public int registerSchema(String schemaFile) {
		int size = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size(); 
		schemaDictionary.registerSchemaUri(schemaFile);
		return ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size() - size;
	}
	
	@ManagedOperation(description="Register Schemas")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaCatalog", description = "A full path to the directory containing XSD files to register")})
	public int registerSchemas(String schemasCatalog) {
		int size = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size(); 
		((XDMSchemaDictionaryBase) schemaDictionary).registerSchemas(schemasCatalog);
		return ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes().size() - size;
	}
	
	@Override
	@ManagedOperation(description="Returns named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to return")})
	public String getProperty(String name) {
		return super.getProperty(name);
	}
	
	@Override
	@ManagedOperation(description="Set named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the property to set")})
	public void setProperty(String name, String value) {
		super.setProperty(name, value);
	}
	
	@Override
	@ManagedOperation(description="Removes named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to remove")})
	public void removeProperty(String name) {
		super.removeProperty(name);
	}
	
	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		logger.debug("getObjectName.enter; schemaName: {}", schemaName);
		return JMXUtils.getObjectName("type=" + type_schema + ",name=" + schemaName);
	}

	int initSchemaInCluster(XDMSchema schema) {
		
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

	int denitSchemaInCluster(XDMSchema schema) {

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
	public boolean initSchema(Properties props) {
    	logger.debug("initSchema.enter; schema: {}; properties: {}", schemaName, props);
    	
    	Object result = schemaCache.executeOnKey(schemaName, new SchemaProcessor());
    	logger.debug("initSchema; execution result: {}", result);
    	
    	props.setProperty("xdm.schema.name", schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
    	try {
    		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
    		ctx.getEnvironment().getPropertySources().addFirst(pps);
    		ctx.setConfigLocation("spring/schema-context.xml");
    		ctx.refresh();

    		//SchemaManager sMgr = ctx.getBean("schemaManager", SchemaManager.class);
    		//sMgr.setSchemaCache(schemaCache);
    		HazelcastInstance hz = ctx.getBean("hzInstance", HazelcastInstance.class);
    		//hz.getUserContext().put("schemaManager", sMgr);
    		hz.getUserContext().put("appContext", ctx);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
    		//hz.getConfig().getSecurityConfig().s
        
    		logger.debug("initSchema.exit; schema {} started on instance: {}; config: {}", schemaName, hz, hz.getConfig());
    		return true;
    	} catch (Exception ex) {
    		logger.error("initSchema.error; " + ex.getMessage(), ex);
    		return false;
    	}
	}
	
	//private boolean initSchema(XDMSchema schema) {
	//	return initSchema(schema.getProperties());
	//}

	@Override
	public boolean denitSchema() {
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

}
