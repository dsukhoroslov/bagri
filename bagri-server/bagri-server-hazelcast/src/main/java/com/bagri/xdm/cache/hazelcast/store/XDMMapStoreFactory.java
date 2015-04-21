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

import static com.bagri.common.config.XDMConfigConstants.*;
import static com.bagri.xdm.client.common.XDMCacheConstants.*;

import com.bagri.xdm.cache.hazelcast.store.xml.DocumentCacheStore;
import com.bagri.xdm.cache.hazelcast.store.xml.XsdCacheStore;
import com.bagri.xdm.cache.hazelcast.util.SpringContextHolder;
import com.bagri.xdm.cache.store.DataStore;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
//import com.hazelcast.spring.mongodb.MongoMapStore;

public class XDMMapStoreFactory implements ApplicationContextAware, MapStoreFactory {
	
    private static final Logger logger = LoggerFactory.getLogger(XDMMapStoreFactory.class);
    
    private static final String st_mongo = "MONGO";
    private static final String st_hive = "HIVE";
    private static final String st_xml = "XML";
    private static final String st_json = "JSON";
    private static final String st_none = "NONE";
    
    private ApplicationContext parentCtx;
    private Map<String, ClassPathXmlApplicationContext> contexts = new HashMap<String, ClassPathXmlApplicationContext>();
	private PropertySource msProps;
	//private XDMSchemaDictionary schemaDict;
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		// it is the first one!?
		parentCtx = context;
		//schemaDict = parentCtx.getBean("xdmDictionary", HazelcastSchemaDictionary.class);
		msProps = ((ConfigurableApplicationContext) context).getEnvironment().getPropertySources().iterator().next(); //get("TPoX");
		logger.debug("setApplicationContext.exit; got properties: {}", msProps);
	}
	
	private ClassPathXmlApplicationContext loadContext(String type, String contextPath) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(parentCtx);
		ctx.getEnvironment().getPropertySources().addFirst(msProps);
		ctx.setConfigLocation(contextPath);
		ctx.refresh();
		contexts.put(type, ctx);
		return ctx;
	}

	@Override
	public MapLoader newMapStore(String mapName, Properties properties) {
		String schemaName = properties.getProperty(xdm_schema_name);
		String type = properties.getProperty(xdm_schema_store_type);
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapStore mStore = null;
		try {
			if (type != null) {
				ClassPathXmlApplicationContext ctx = contexts.get(type);
				if (ctx == null) {
					if (st_mongo.equals(type)) {
			    		ctx = loadContext(st_mongo, "spring/store-mongo-context.xml");
					} else if (st_hive.equals(type)) {
			    		ctx = loadContext(st_hive, "spring/store-hive-context.xml");
					} else if (st_xml.equals(type)) {
			    		ctx = loadContext(st_xml, "spring/store-xml-context.xml");
					} else if (st_json.equals(type)) {
			    		ctx = loadContext(st_json, "spring/store-xml-context.xml");
					}
				}
				logger.debug("newMapStore; got context: {}", ctx);
				
				if (ctx != null) {
					// deadlocks here
					//HazelcastInstance hz = parentCtx.getBean("hzInstance", HazelcastInstance.class);
		    		//hz.getUserContext().put("storeContext", ctx);
					SpringContextHolder.setAbsentContext(schemaName, "storeContext", ctx);
					
					if (st_mongo.equals(type)) {
						if (CN_XDM_ELEMENT.equals(mapName)) {
							DataStore store = ctx.getBean("elementCacheStore",  DataStore.class); 
							mStore = new DataStoreAdapter(store);
						} else {
							mStore = null; //ctx.getBean("mongoCacheStore", MongoMapStore.class);
						}
					} else if (st_hive.equals(type)) {
						DataStore store = ctx.getBean("hiveCacheStore", DataStore.class);
						mStore = new DataStoreAdapter(store);
					} else if (st_xml.equals(type) || st_json.equals(type)) {
						// TODO: move XML stores to extra module too
						if (CN_XDM_DOCUMENT.equals(mapName)) {
							mStore = ctx.getBean("docCacheStore", DocumentCacheStore.class);
						} else if (CN_XDM_DOCTYPE_DICT.equals(mapName)) {
							mStore = ctx.getBean("xsdCacheStore", XsdCacheStore.class);
						} else {
							mStore = new DummyCacheStore();
						}
					} else {
						//
					}
				}
			}
		
			if (st_none.equals(type) || type == null) {
				// but map-store is enabled! means: config error
				// todo: throw exception
				mStore = new DummyCacheStore();
			}
		} catch (Exception ex) {
    		logger.error("newMapStore.error: ", ex.getMessage(), ex);
		}
		logger.debug("newMapStore.exit; returning: {}", mStore);
		return mStore; 
	}

}
