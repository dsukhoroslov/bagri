package com.bagri.xdm.cache.hazelcast.store;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bagri.common.config.XDMConfigConstants.*;

import com.bagri.xdm.common.XDMDocumentStore;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;

public class DocumentStoreFactory implements MapStoreFactory<XDMDocumentKey, XDMDocument> { //, ApplicationContextAware {
	
    private static final Logger logger = LoggerFactory.getLogger(DocumentStoreFactory.class);
    private static final String defaultStoreClass = "com.bagri.xdm.cache.hazelcast.store.xml.DocumentCacheStore";
    
    //private ApplicationContext ctx;
	//private PropertySource msProps;
	
	//@Override
	//public void setApplicationContext(ApplicationContext context) throws BeansException {
		// it is the first one!?
	//	ctx = context;
	//	msProps = ((ConfigurableApplicationContext) context).getEnvironment().getPropertySources().iterator().next(); //get("TPoX");
		//logger.debug("setApplicationContext.exit; got properties: {}", ctx.getBean("xdmRepo"));
	//}
	
	@Override
	public MapLoader newMapStore(String mapName, Properties properties) {
		String storeClass = null;
		String storeType = properties.getProperty(xdm_schema_store_type);
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapStore mStore = null;
		// TODO: get it from type..
		if (storeClass == null) {
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
				mStore = (MapStore) instance;
			} else if (instance instanceof XDMDocumentStore) {
				mStore = new DocumentStoreAdapter((XDMDocumentStore) instance);
				// pass props somehow..
				// do we need DataStoreAdapter.init ?
			} else {
				logger.warn("newMapStore; unknown store instance: " + instance);
			}
		}
		
		if (mStore == null) {
			// but map-store is enabled! means: config error
			// todo: throw exception
			mStore = new DummyCacheStore();
		}
		logger.info("newMapStore.exit; returning: {}", mStore);
		return mStore; 
	}

}
