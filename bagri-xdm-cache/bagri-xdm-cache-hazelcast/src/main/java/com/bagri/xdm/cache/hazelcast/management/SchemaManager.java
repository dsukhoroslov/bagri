package com.bagri.xdm.cache.hazelcast.management;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
import com.bagri.xdm.XDMDocumentType;
import com.bagri.xdm.XDMSchema;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@ManagedResource(description="Schema Manager MBean")
public class SchemaManager extends XDMSchemaManagerBase implements SelfNaming {

    private static final transient Logger logger = LoggerFactory.getLogger(SchemaManager.class);
    private static final String type_schema = "Schema";
	
    private HazelcastInstance hzInstance;
    private IMap<String, XDMSchema> schemaCache;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(HazelcastInstance hzInstance) {
		super();
		this.hzInstance = hzInstance;
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

}
