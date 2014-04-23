package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.process.hazelcast.schema.SchemaActivator;
import com.bagri.xdm.process.hazelcast.schema.SchemaUpdater;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.IMap;

@ManagedResource(description="Schema Manager MBean")
public class SchemaManager extends XDMSchemaManagerBase implements SelfNaming {

    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManager.class);
    private static final String type_schema = "Schema";
    private static final String state_ok = "working";

    private String state = state_ok;
    private SchemaManagement parent;
    private IMap<String, XDMSchema> schemaCache;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(SchemaManagement parent, String schemaName) {
		super();
		this.parent = parent;
		this.schemaName = schemaName;
	}
	
	
	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
	}
	
	@ManagedAttribute(description="Returns short Schema description")
	public String getDescription() {
		return getSchema().getDescription();
	}

	@ManagedAttribute(description="Returns short Schema description")
	public void setDescription(String description) {
		XDMSchema schema = getSchema();
		schema.setDescription(description);
		flushSchema(schema);
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

	@ManagedAttribute(description="Returns Schema persistence type")
	public String getPersistenceType() {
		String result = getSchema().getProperty("xdm.schema.store.type");
		if (result == null) {
			result = "NONE";
		}
		return result;
	}

	@ManagedAttribute(description="Returns registered Schema name")
	public String getName() {
		return super.getSchemaName();
	}
	
	@ManagedAttribute(description="Returns registered Schema properties")
	public CompositeData getProperties() {
		return super.getAllProperties();
	}
	
	@ManagedAttribute(description="Returns registered Schema version")
	public int getVersion() {
		return getSchema().getVersion();
	}

	@ManagedAttribute(description="Returns Schema state")
	public boolean isActive() {
		return getSchema().isActive();
	}

	@ManagedAttribute(description="Returns registered Schema state")
	public String getState() {
		return state;
	}
	
	void setState(String state) {
		this.state = state;
	}
	
	@Override
	protected XDMSchema getSchema() {
		XDMSchema schema = schemaCache.get(schemaName);
		//logger.trace("getSchema. returning: {}", schema);
		return schema;
	}

	@Override
	protected void flushSchema(XDMSchema schema) {
		schemaCache.put(schemaName, schema);
	}
	
	@ManagedOperation(description="Activate Schema")
	public boolean activateSchema() {
		XDMSchema schema = getSchema();
		if (schema != null && !schema.isActive()) {
	    	Object result = schemaCache.executeOnKey(schemaName, new SchemaActivator(schema.getVersion(), true));
	    	logger.trace("activateSchema; execution result: {}", result);
	    	return result != null;
		} 
		return false;
	}		
		
	@ManagedOperation(description="Deactivate Schema")
	public boolean deactivateSchema() {
		XDMSchema schema = getSchema();
		if (schema != null && schema.isActive()) {
	    	Object result = schemaCache.executeOnKey(schemaName, new SchemaActivator(schema.getVersion(), false));
	    	logger.trace("deactivateSchema; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@ManagedOperation(description="Update Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by comma")})
	public boolean updateSchemaProperties(String properties) {
		XDMSchema schema = getSchema();
		if (schema != null) {
			Properties props;
			try {
				props = FileUtils.propsFromString(properties);
			} catch (IOException ex) {
				logger.error("updateSchemaProperties.error: ", ex);
				return false;
			}
			
	    	Object result = schemaCache.executeOnKey(schemaName, new SchemaUpdater(schema.getVersion(), true, props));
	    	logger.trace("updateSchemaProperties; execution result: {}", result);
	    	return result != null;
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
		XDMSchema schema = getSchema();
		if (schema != null) {
			Properties props = new Properties();
			props.setProperty(name, value);
	    	Object result = schemaCache.executeOnKey(schemaName, new SchemaUpdater(schema.getVersion(), false, props));
	    	logger.trace("setProperty; execution result: {}", result);
		}
	}
	
	@Override
	@ManagedOperation(description="Removes named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to remove")})
	public void removeProperty(String name) {
		// override property with default value
		String defValue = parent.getDefaultProperty(name);
		if (defValue == null) {
			defValue = ""; //???
		}
		setProperty(name, defValue);
	}
	
	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		logger.debug("getObjectName.enter; schemaName: {}", schemaName);
		return JMXUtils.getObjectName("type=" + type_schema + ",name=" + schemaName);
	}

}
