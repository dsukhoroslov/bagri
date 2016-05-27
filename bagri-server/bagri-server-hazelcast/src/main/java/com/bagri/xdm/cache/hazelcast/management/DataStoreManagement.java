package com.bagri.xdm.cache.hazelcast.management;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.system.XDMDataStore;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.xdm:type=Management,name=DataStoreManagement", 
	description="Data Store Management MBean")
public class DataStoreManagement extends EntityManagement<XDMDataStore> {

    public DataStoreManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	@Override
	protected EntityManager<XDMDataStore> createEntityManager(String storeName) {
		DataStoreManager mgr = new DataStoreManager(hzInstance, storeName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}
    
	@ManagedAttribute(description="Return registered Data store names")
	public String[] getDataStoreNames() {
		return getEntityNames();
	}

	@ManagedAttribute(description="Return registered Data Stores")
	public TabularData getDataStores() {
		return getEntities("dataStore", "Data Store definition");
    }
	
	@ManagedOperation(description="Creates a new Data Format")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "DataFormat name to create"),
		@ManagedOperationParameter(name = "library", description = "Library name containing DataStore implementation"),
		@ManagedOperationParameter(name = "description", description = "DataStore description")})
	public void addDataStore(String name, String library, String description) {

		logger.trace("addDataStore.enter; name: {}", name);
		XDMDataStore store = null;
		if (!entityCache.containsKey(name)) {
	    	//Object result = entityCache.executeOnKey(name, new DataFormatCreator(getCurrentUser(), library, description));
			//return true;
	    	//store = (XDMDataStore) result;
		}
		//return false;
		logger.trace("addStore.exit; dataStore created: {}", store);
	}
	
	@ManagedOperation(description="Removes an existing Data Store")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Data Store name to delete")})
	public void deleteDataStore(String name) {
		
		logger.trace("deleteDataStore.enter; name: {}", name);
		XDMDataStore store = entityCache.get(name);
		if (store != null) {
	    	//Object result = entityCache.executeOnKey(name, new DataFormatRemover(format.getVersion(), getCurrentUser()));
	    	//return result != null;
		}
		//return false;
		logger.trace("deleteDataStore.exit; dataStore deleted");
	}



}
