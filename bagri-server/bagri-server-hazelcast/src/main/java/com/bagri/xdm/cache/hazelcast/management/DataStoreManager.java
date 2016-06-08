package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaUpdater;
import com.bagri.xdm.system.XDMDataStore;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(description="Data Store Manager MBean")
public class DataStoreManager extends EntityManager<XDMDataStore> { 

	public DataStoreManager() {
		super();
	}
    
	public DataStoreManager(HazelcastInstance hzInstance, String storeName) {
		super(hzInstance, storeName);
	}

	@ManagedAttribute(description="Returns Data Store properties")
	public CompositeData getProperties() {
		XDMDataStore store = getEntity();
		return JMXUtils.propsToComposite(entityName, "properties", store.getProperties());
	}
	
	@Override
	protected String getEntityType() {
		return "DataStore";
	}

	@ManagedAttribute(description="Returns Data Store description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	@ManagedAttribute(description="Returns registered Data Store name")
	public String getName() {
		return entityName;
	}

	@ManagedAttribute(description="Returns Data Store version")
	public int getVersion() {
		return super.getVersion();
	}
	
	@ManagedAttribute(description="Returns DocumentStore class for this Data Store")
	public String getStoreClass() {
		return getEntity().getStoreClass();
	}
	
	@ManagedOperation(description="Set Data Store property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the property to set")})
	public void setProperty(String name, String value) {
		XDMDataStore store = getEntity();
		Properties props = new Properties();
		props.setProperty(name, value);
    	//Object result = entityCache.executeOnKey(entityName, new SchemaUpdater(schema.getVersion(), 
    	//		getCurrentUser(), false, props));
    	//logger.trace("setProperty; execution result: {}", result);
	}
	
	@ManagedOperation(description="Remove Data Store property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to remove")})
	public void removeProperty(String name) {
		// override property with default value !?
		//String defValue = parent.getDefaultProperty(name);
		//if (defValue == null) {
		//	defValue = ""; // throw exception ???
		//}
		setProperty(name, ""); // defValue
	}


}
