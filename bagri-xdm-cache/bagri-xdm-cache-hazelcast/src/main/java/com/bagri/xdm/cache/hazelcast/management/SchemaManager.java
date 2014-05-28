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
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.api.XDMSchemaManagerBase;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.process.hazelcast.schema.SchemaActivator;
import com.bagri.xdm.process.hazelcast.schema.SchemaUpdater;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.IMap;

@ManagedResource(description="Schema Manager MBean")
//public class SchemaManager extends XDMSchemaManagerBase implements SelfNaming {
public class SchemaManager extends EntityManager<XDMSchema> {

    private static final String state_ok = "working";

    private String state = state_ok;
    private SchemaManagement parent;
	protected XDMDocumentManagerServer docManager;
	protected XDMSchemaDictionary schemaDictionary;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(SchemaManagement parent, String schemaName) {
		super(schemaName);
		this.parent = parent;
	}
	
	public XDMDocumentManagerServer getDocumentManager() {
		return docManager;
	}
	
	public void setDocumentManager(XDMDocumentManagerServer docManager) {
		this.docManager = docManager;
	}
	
	public XDMSchemaDictionary getSchemaDictionary() {
		return schemaDictionary;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	@ManagedAttribute(description="Returns short Schema description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	@ManagedAttribute(description="Returns short Schema description")
	public void setDescription(String description) {
		XDMSchema schema = getEntity();
		schema.setDescription(description);
		flushEntity(schema);
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
		String result = getEntity().getProperty("xdm.schema.store.type");
		if (result == null) {
			result = "NONE";
		}
		return result;
	}

	@ManagedAttribute(description="Returns registered Schema name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns registered Schema properties")
	public CompositeData getProperties() {
		Properties props = getEntity().getProperties();
		return JMXUtils.propsToComposite(entityName, "properties", props);
	}
	
	@ManagedAttribute(description="Returns registered Schema version")
	public int getVersion() {
		return getEntity().getVersion();
	}

	@ManagedAttribute(description="Returns Schema state")
	public boolean isActive() {
		return getEntity().isActive(); //?? prabably we have to calc it..
	}

	@ManagedAttribute(description="Returns registered Schema state")
	public String getState() {
		return state;
	}
	
	void setState(String state) {
		this.state = state;
	}
	
	@ManagedOperation(description="Activate Schema")
	public boolean activateSchema() {
		XDMSchema schema = getEntity();
		if (schema != null && !schema.isActive()) {
			String user = JMXUtils.getCurrentUser();
	    	Object result = entityCache.executeOnKey(entityName, new SchemaActivator(schema.getVersion(), user, true));
	    	logger.trace("activateSchema; execution result: {}", result);
	    	return result != null;
		} 
		return false;
	}		
		
	@ManagedOperation(description="Deactivate Schema")
	public boolean deactivateSchema() {
		XDMSchema schema = getEntity();
		if (schema != null && schema.isActive()) {
			String user = JMXUtils.getCurrentUser();
	    	Object result = entityCache.executeOnKey(entityName, new SchemaActivator(schema.getVersion(), user, false));
	    	logger.trace("deactivateSchema; execution result: {}", result);
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
	
	//@Override
	@ManagedOperation(description="Returns named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to return")})
	public String getProperty(String name) {
		return getEntity().getProperty(name);
	}
	
	//@Override
	@ManagedOperation(description="Set named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the property to set")})
	public void setProperty(String name, String value) {
		XDMSchema schema = getEntity();
		if (schema != null) {
			Properties props = new Properties();
			props.setProperty(name, value);
	    	Object result = entityCache.executeOnKey(entityName, new SchemaUpdater(schema.getVersion(), 
	    			JMXUtils.getCurrentUser(), false, props));
	    	logger.trace("setProperty; execution result: {}", result);
		}
	}
	
	//@Override
	@ManagedOperation(description="Removes named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to remove")})
	public void removeProperty(String name) {
		// override property with default value
		String defValue = parent.getDefaultProperty(name);
		if (defValue == null) {
			defValue = ""; // throw exception ???
		}
		setProperty(name, defValue);
	}
	
	@ManagedOperation(description="Update Schema properties")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by comma")})
	public boolean updateProperties(String properties) {
		XDMSchema schema = getEntity();
		if (schema != null) {
			Properties props;
			try {
				props = FileUtils.propsFromString(properties);
			} catch (IOException ex) {
				logger.error("updateProperties.error: ", ex);
				return false;
			}
			
	    	Object result = entityCache.executeOnKey(entityName, new SchemaUpdater(schema.getVersion(), 
	    			JMXUtils.getCurrentUser(), true, props));
	    	logger.trace("updateProperties; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected String getEntityType() {
		return "Schema";
	}

}
