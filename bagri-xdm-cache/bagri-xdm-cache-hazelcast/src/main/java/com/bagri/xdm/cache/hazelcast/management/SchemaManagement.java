package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import com.bagri.xdm.process.hazelcast.schema.SchemaRemover;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=SchemaManagement", 
	description="Schema Management MBean")
public class SchemaManagement implements EntryListener<String, XDMSchema>, InitializingBean, XDMSchemaManagement {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManagement.class);
    
	private Properties defaults; 
    private HazelcastInstance hzInstance;
    private IMap<String, XDMSchema> schemaCache;
    private Map<String, SchemaManager> mgrCache = new HashMap<String, SchemaManager>(); 
    private Map<String, XDMSchemaDictionary> dictCache = new HashMap<String, XDMSchemaDictionary>(); 
    
    @Autowired
	private AnnotationMBeanExporter mbeanExporter;
    
	public SchemaManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = schemaCache.keySet();
		logger.debug("afterPropertiesSet.enter; got schemas: {}", names); 
        for (String name: names) {
        	XDMSchema schema = schemaCache.get(name);
       		boolean initialized = initSchema(name, schema.getProperties());
       		SchemaManager sMgr = (SchemaManager) mgrCache.get(name);
       		if (sMgr == null) {
   				logger.debug("afterPropertiesSet; cannot get SchemaManager for schema {}; initializing a new one", name); 
       			try {
       				sMgr = initSchemaManager(name);
       			} catch (MBeanExportException | MalformedObjectNameException ex) {
       				// JMX registration failed.
       				logger.error("afterPropertiesSet.error: ", ex);
       			}
       		}
   			if (sMgr != null && !initialized) {
   				sMgr.setState("Failed schema initialization");
   			}
        }
	}

	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
		schemaCache.addEntryListener(this, false);
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
		return schemaCache.keySet().toArray(new String[0]);
	}
	
	@Override
	public Collection<XDMSchema> getSchemas() {
		return new ArrayList<XDMSchema>(schemaCache.values());
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
		if (!schemaCache.containsKey(schemaName)) {
	    	Object result = schemaCache.executeOnKey(schemaName, new SchemaCreator(description, props));
	    	logger.debug("initSchema; execution result: {}", result);
	    	schema = (XDMSchema) result;
		}
		return schema;
	}
	
	@Override
	public XDMSchema deleteSchema(String schemaName) {
		XDMSchema schema = schemaCache.get(schemaName);
		if (schema != null) {
	    	Object result = schemaCache.executeOnKey(schemaName, new SchemaRemover(schema.getVersion()));
	    	logger.debug("denitSchema; execution result: {}", result);
	    	schema = (XDMSchema) result;
		}
		return schema;
	}
	
	@Override
	public void entryAdded(EntryEvent<String, XDMSchema> event) {
		logger.trace("entryAdded; event: {}", event);
		String schemaName = event.getKey();
		try {
			initSchemaManager(schemaName);
		} catch (MBeanExportException | MalformedObjectNameException ex) {
			// JMX registration failed.
			logger.error("entryAdded.error: ", ex);
		}
	}
	
	@Override
	public void entryEvicted(EntryEvent<String, XDMSchema> event) {
		logger.trace("entryEvicted; event: {}", event);
		// make schema inactive ?
	}

	@Override
	public void entryRemoved(EntryEvent<String, XDMSchema> event) {
		logger.trace("entryRemoved; event: {}", event);
		String schemaName = event.getKey();
		if (mgrCache.containsKey(schemaName)) {
			SchemaManager sMgr = mgrCache.get(schemaName);
			mgrCache.remove(schemaName);
			// remove from dictCache ?
			try {
				mbeanExporter.unregisterManagedResource(sMgr.getObjectName());
			} catch (MalformedObjectNameException ex) {
				logger.error("entryRemoved.error: ", ex);
			}
		}
	}

	@Override
	public void entryUpdated(EntryEvent<String, XDMSchema> event) {
		logger.trace("entryUpdated; event: {}", event);
	}

	private SchemaManager initSchemaManager(String schemaName) throws MBeanExportException, MalformedObjectNameException {
		SchemaManager sMgr = null;
   	    if (!mgrCache.containsKey(schemaName)) {
			sMgr = new SchemaManager(this, schemaName);
			sMgr.setSchemaCache(schemaCache);
			XDMSchemaDictionary schemaDict = dictCache.get(schemaName);
			sMgr.setSchemaDictionary(schemaDict);
			mgrCache.put(schemaName, sMgr);
			mbeanExporter.registerManagedResource(sMgr, sMgr.getObjectName());
		}
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
    		ctx.setConfigLocation("spring/schema-context.xml");
    		ctx.refresh();

    		HazelcastInstance hz = ctx.getBean("hzInstance", HazelcastInstance.class);
    		hz.getUserContext().put("appContext", ctx);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
    		//hz.getConfig().getSecurityConfig().s
    	    XDMSchemaDictionary schemaDict = ctx.getBean("xdmDictionary", XDMSchemaDictionary.class);
    	    SchemaManager sMgr = mgrCache.get(schemaName);
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

}
