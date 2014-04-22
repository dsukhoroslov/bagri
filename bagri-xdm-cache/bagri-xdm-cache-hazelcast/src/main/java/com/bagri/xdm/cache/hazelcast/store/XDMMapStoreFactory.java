package com.bagri.xdm.cache.hazelcast.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertySource;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.spring.mongodb.MongoMapStore;

public class XDMMapStoreFactory implements ApplicationContextAware, MapStoreFactory<Object, Object> {
	
    private static final transient Logger logger = LoggerFactory.getLogger(XDMMapStoreFactory.class);
    private static final String st_mongo = "MONGO";
    
    private Map<String, MapStore> stores = new HashMap<String, MapStore>();
	private PropertySource msProps;
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		// it is the first one!
		msProps = ((ConfigurableApplicationContext) context).getEnvironment().getPropertySources().iterator().next(); //get("TPoX");
		logger.debug("setApplicationContext.exit; got properties: {}", msProps);
	}

	@Override
	public MapLoader<Object, Object> newMapStore(String mapName, Properties properties) {
		String type = properties.getProperty("xdm.schema.store.type");
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapStore mStore = null;
		try {
			if (type != null) {
				mStore = stores.get(type);
				if (mStore == null) { 
					if (st_mongo.equals(type)) {
			    		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
			    		ctx.getEnvironment().getPropertySources().addFirst(msProps);
			    		ctx.setConfigLocation("spring/mongo-context.xml");
			    		ctx.refresh();
			    		mStore = ctx.getBean("mongoCacheStore", MongoMapStore.class);
					} else {
						// 
					}
					
					stores.put(type, mStore);
				}
			}
		} catch (Exception ex) {
    		logger.error("newMapStore.error: ", ex.getMessage(), ex);
		}
		logger.debug("newMapStore.exit; returning: {}", mStore);
		return mStore; 
	}

}
