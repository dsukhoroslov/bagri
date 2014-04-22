package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=SchemaManagement", 
	description="Schema Management MBean")
public class SchemaManagement implements InitializingBean, XDMSchemaManagement {
	
    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManagement.class);
	private static final String schema_management = "SchemaManagement";
    
	private Properties defaults; 
    private HazelcastInstance hzInstance;
	private IExecutorService execService;
	private Map<String, SchemaManager> mgrCache = new HashMap<String, SchemaManager>();
    private IMap<String, XDMSchema> schemaCache;
    
    @Autowired
	private AnnotationMBeanExporter mbeanExporter;
    
	public SchemaManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = schemaCache.keySet();
        for (String name: names) {
        	XDMSchema schema = schemaCache.get(name);
       		initSchemaManager(schema, false);
        }
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
			schema = new XDMSchema(schemaName, 1, description, true, new Date(), schema_management, props);
			if (initSchemaManager(schema, true)) {
				schemaCache.put(schemaName, schema);
			}
		}
		return schema;
	}
	
	@Override
	public XDMSchema deleteSchema(String schemaName) {
		XDMSchema schema = schemaCache.get(schemaName);
		// lock schema here ?
		if (schema != null) {
			if (denitSchemaManager(schema)) {
				schemaCache.remove(schemaName);
			}
		}
		return schema;
	}
	
	private boolean initSchemaManager(XDMSchema schema, boolean distribute) {
		String schemaName = schema.getName();
		if (!mgrCache.containsKey(schemaName)) {
			SchemaManager sMgr = new SchemaManager(hzInstance, schemaName);
			sMgr.setExecService(execService);
			sMgr.setSchemaCache(schemaCache);
			mgrCache.put(schemaName, sMgr);
			if (distribute) {
				int cnt = sMgr.initSchemaInCluster(schema);
			} else {
				sMgr.initSchema(schema.getProperties());
			}
			//mgrCache.put(schemaName, sMgr);
			try {
				mbeanExporter.registerManagedResource(sMgr, sMgr.getObjectName());
				return true;
			} catch (MBeanExportException | MalformedObjectNameException ex) {
				logger.error("initSchemaManager.error: ", ex);
			}
		}
		return false;
	}
	
	private boolean denitSchemaManager(XDMSchema schema) {
		String schemaName = schema.getName();
		if (mgrCache.containsKey(schemaName)) {
			SchemaManager sMgr = mgrCache.get(schemaName);
			int cnt = sMgr.denitSchemaInCluster(schema);
			mgrCache.remove(schemaName);
			try {
				mbeanExporter.unregisterManagedResource(sMgr.getObjectName());
				return true;
			} catch (MalformedObjectNameException ex) {
				logger.error("denitSchemaManager.error: ", ex);
			}
		}
		return false;
	}

	@Override
	public XDMSchemaManagerBase getSchemaManager(String schemaName) {
		return mgrCache.get(schemaName);
	}

}
