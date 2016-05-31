package com.bagri.xdm.cache.hazelcast.store;

import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.*;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.common.XDMDocumentStore;
import com.bagri.xdm.common.XDMEntity;
import com.bagri.xdm.cache.hazelcast.config.SystemConfig;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMDataStore;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.core.Member;

public class DocumentStoreFactory implements MapStoreFactory<XDMDocumentKey, XDMDocument> { 
	
    private static final Logger logger = LoggerFactory.getLogger(DocumentStoreFactory.class);
    private static final String defaultStoreClass = "com.bagri.xdm.cache.hazelcast.store.FileDocumentCacheStore";
    
    private Collection<? extends XDMEntity> getConfigEntities(Class<? extends XDMEntity> cls, String cache) {
		ApplicationContext context = findSystemContext();
		Collection<? extends XDMEntity> result = null;
		boolean lite = true;
		if (context != null) {
			HazelcastInstance hzInstance = context.getBean(HazelcastInstance.class);
			for (Member member: hzInstance.getCluster().getMembers()) {
				if (!member.isLiteMember()) {
					lite = false;
					break;
				}
			}
			if (lite) {
				SystemConfig config = context.getBean(SystemConfig.class);
				if (config.isLoaded()) {
					result = config.getEntities(cls);
				}
			} else {
	    		IMap<String, ? extends XDMEntity> entities = hzInstance.getMap(cache);
	    		result = entities.values();
	    	}
		}
		logger.debug("getConfigEntities; returning {} from {} node", result, lite ? "LOCAL" : "ADMIN");
		return result;
    }
    
    @SuppressWarnings("unchecked")
	private XDMDataStore getDataStore(String storeType) {
    	Collection<XDMDataStore> stores = (Collection<XDMDataStore>) getConfigEntities(XDMDataStore.class, "stores");
    	if (stores != null) {
			for (XDMDataStore store: stores) {
				if (store.getName().equals(storeType)) {
					return store;
				}
			}
		}
    	return null;
    }

    @SuppressWarnings("unchecked")
	private XDMSchema getSchema(String schemaName) {
    	Collection<XDMSchema> schemas = (Collection<XDMSchema>) getConfigEntities(XDMSchema.class, "schemas");
    	if (schemas != null) {
			for (XDMSchema schema: schemas) {
				if (schema.getName().equals(schemaName)) {
					return schema;
				}
			}
		}
    	return null;
    }
    
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MapLoader<XDMDocumentKey, XDMDocument> newMapStore(String mapName, Properties properties) {
		String storeClass = null;
		String schemaName = properties.getProperty(xdm_schema_name);
		String storeType = properties.getProperty(xdm_schema_store_type);
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapStore<XDMDocumentKey, XDMDocument> mStore = null;
		XDMDataStore store = getDataStore(storeType);
		if (store != null) {
			storeClass = store.getStoreClass();
			XDMSchema schema = getSchema(schemaName);
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
				mStore = (MapStore<XDMDocumentKey, XDMDocument>) instance;
			} else if (instance instanceof XDMDocumentStore) {
				mStore = new DocumentStoreAdapter((XDMDocumentStore) instance);
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
