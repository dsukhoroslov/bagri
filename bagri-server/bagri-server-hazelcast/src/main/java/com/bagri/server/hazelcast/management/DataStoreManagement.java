package com.bagri.server.hazelcast.management;

import static com.bagri.support.util.PropUtils.propsFromString;

import java.io.IOException;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.system.DataStore;
import com.bagri.server.hazelcast.task.store.DataStoreCreator;
import com.bagri.server.hazelcast.task.store.DataStoreRemover;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Denis Sukhoroslov 
 *
 */
@ManagedResource(objectName="com.bagri.db:type=Management,name=DataStoreManagement", 
	description="Data Store Management MBean")
public class DataStoreManagement extends EntityManagement<DataStore> {

    public DataStoreManagement(HazelcastInstance hzInstance) {
    	super(hzInstance);
    }

	@Override
	protected EntityManager<DataStore> createEntityManager(String storeName) {
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
	
	@ManagedOperation(description="Creates a new Data Store")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "DataStore name to create"),
		@ManagedOperationParameter(name = "storeClass", description = "DocumentStore implementation class name"),
		@ManagedOperationParameter(name = "description", description = "DataStore description"),
		@ManagedOperationParameter(name = "properties", description = "DataStore properties with their default values")})
	public boolean addDataStore(String name, String storeClass, String description, String properties) {
		logger.trace("addDataStore.enter; name: {}", name);
		DataStore store = null;
		if (!entityCache.containsKey(name)) {
			try {
				Object result = entityCache.executeOnKey(name, new DataStoreCreator(getCurrentUser(), storeClass, description,
						propsFromString(properties)));
		    	store = (DataStore) result;
			} catch (IOException ex) {
				logger.error("", ex);
			}
		}
		logger.trace("addStore.exit; dataStore created: {}", store);
		return store != null;
	}
	
	@ManagedOperation(description="Removes an existing Data Store")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Data Store name to delete")})
	public boolean deleteDataStore(String name) {
		logger.trace("deleteDataStore.enter; name: {}", name);
		DataStore store = entityCache.get(name);
		if (store != null) {
	    	Object result = entityCache.executeOnKey(name, new DataStoreRemover(store.getVersion(), getCurrentUser()));
	    	store = (DataStore) result;
		}
		logger.trace("deleteDataStore.exit; dataStore deleted: {}", store);
		return store != null;
	}



}
