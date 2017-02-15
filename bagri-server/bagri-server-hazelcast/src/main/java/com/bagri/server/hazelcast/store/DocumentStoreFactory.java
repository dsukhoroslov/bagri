package com.bagri.server.hazelcast.store;

import static com.bagri.core.Constants.*;
import static com.bagri.server.hazelcast.util.HazelcastUtils.*;
import static com.bagri.support.util.PropUtils.substituteProperties;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentStore;
import com.bagri.core.system.DataStore;
import com.bagri.core.system.Entity;
import com.bagri.core.system.Schema;
import com.bagri.server.hazelcast.config.SystemConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
//import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;

public class DocumentStoreFactory implements MapStoreFactory<DocumentKey, Document> { 
	
    private static final Logger logger = LoggerFactory.getLogger(DocumentStoreFactory.class);
    private static final String defaultStoreClass = FileDocumentCacheStore.class.getName();
    
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
		String schemaName = properties.getProperty(pn_schema_name);
		String storeType = properties.getProperty(pn_schema_store_type);
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapLoader<DocumentKey, Document> mStore = null;
		DataStore store = getDataStore(storeType);
		if (store != null) {
			storeClass = store.getStoreClass();
			properties.putAll(store.getProperties());
			Schema schema = getSchema(schemaName);
			if (schema != null) {
				// override store properties with their schema values
				substituteProperties(properties, schema.getProperties());
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
			if (instance instanceof MapLoader) {
				mStore = (MapLoader<DocumentKey, Document>) instance;
			} else if (instance instanceof DocumentStore) {
				if (((DocumentStore) instance).isReadOnly()) {
					mStore = new DocumentLoaderAdapter((DocumentStore) instance);
				} else {
					mStore = new DocumentStoreAdapter((DocumentStore) instance);
				}
			} else {
				logger.warn("newMapStore; unknown store instance: " + instance);
			}
		}
		
		if (mStore == null) {
			throw new IllegalArgumentException("Configuration error: no DataStore found for type " + storeType + ", class " + storeClass);
		}
		logger.info("newMapStore.exit; returning: {}; read-only: {}", mStore, !(mStore instanceof MapStore));
		return mStore; 
	}

}
