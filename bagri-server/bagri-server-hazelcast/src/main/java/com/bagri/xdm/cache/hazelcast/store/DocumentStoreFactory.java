package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.*;
import static com.bagri.xdm.common.XDMConstants.*;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.cache.api.DocumentStore;
import com.bagri.xdm.cache.hazelcast.config.SystemConfig;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.system.DataStore;
import com.bagri.xdm.system.Schema;
import com.bagri.xdm.system.Entity;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.core.Member;

public class DocumentStoreFactory implements MapStoreFactory<DocumentKey, Document> { 
	
    private static final Logger logger = LoggerFactory.getLogger(DocumentStoreFactory.class);
    private static final String defaultStoreClass = "com.bagri.xdm.cache.hazelcast.store.FileDocumentCacheStore";
    
    private Collection<? extends Entity> getConfigEntities(Class<? extends Entity> cls, String cache) {
		ApplicationContext context = findSystemContext();
		Collection<? extends Entity> result = null;
		boolean lite = true;
		if (context != null) {
			HazelcastInstance hzInstance = context.getBean(HazelcastInstance.class);
			lite = !hasStorageMembers(hzInstance);
			if (lite) {
				SystemConfig config = context.getBean(SystemConfig.class);
				if (config.isLoaded()) {
					result = config.getEntities(cls);
				}
			} else {
	    		IMap<String, ? extends Entity> entities = hzInstance.getMap(cache);
	    		result = entities.values();
	    	}
		}
		logger.debug("getConfigEntities; returning {} from {} node", result, lite ? "LOCAL" : "ADMIN");
		return result;
    }
    
    @SuppressWarnings("unchecked")
	private DataStore getDataStore(String storeType) {
    	Collection<DataStore> stores = (Collection<DataStore>) getConfigEntities(DataStore.class, "stores");
    	if (stores != null) {
			for (DataStore store: stores) {
				if (store.getName().equals(storeType)) {
					return store;
				}
			}
		}
    	return null;
    }

    @SuppressWarnings("unchecked")
	private Schema getSchema(String schemaName) {
    	Collection<Schema> schemas = (Collection<Schema>) getConfigEntities(Schema.class, "schemas");
    	if (schemas != null) {
			for (Schema schema: schemas) {
				if (schema.getName().equals(schemaName)) {
					return schema;
				}
			}
		}
    	return null;
    }
    
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MapLoader<DocumentKey, Document> newMapStore(String mapName, Properties properties) {
		String storeClass = null;
		String schemaName = properties.getProperty(xdm_schema_name);
		String storeType = properties.getProperty(xdm_schema_store_type);
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapStore<DocumentKey, Document> mStore = null;
		DataStore store = getDataStore(storeType);
		if (store != null) {
			storeClass = store.getStoreClass();
			Schema schema = getSchema(schemaName);
			if (schema != null) {
				// override store properties with their schema values
				for (Map.Entry prop: store.getProperties().entrySet()) {
					String pName = (String) prop.getKey();
					properties.setProperty(pName, schema.getProperties().getProperty(pName, (String) prop.getValue()));
				}
			} else {
				properties.putAll(store.getProperties());
			}
		} else {
			storeClass = defaultStoreClass;
		}
		
		Object instance = null;
		try {
			Class clazz = Class.forName(storeClass);
			instance = clazz.newInstance();
		} catch (ClassNotFoundException ex) {
			logger.error("newMapStore; unknown class: " + storeClass);
		} catch (InstantiationException | IllegalAccessException ex) {
			logger.error("newMapStore; cannot instantiate: " + storeClass, ex);
		}

		if (instance != null) {
			if (instance instanceof MapStore) {
				mStore = (MapStore<DocumentKey, Document>) instance;
			} else if (instance instanceof DocumentStore) {
				mStore = new DocumentStoreAdapter((DocumentStore) instance);
			} else {
				logger.warn("newMapStore; unknown store instance: " + instance);
			}
		}
		
		if (mStore == null) {
			throw new IllegalArgumentException("Configuration error: no DataStore found for type " + storeType + ", class " + storeClass);
		}
		logger.info("newMapStore.exit; returning: {}", mStore);
		return mStore; 
	}

}
