package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMPopulateSchemaTask;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.hazelcast.impl.HazelcastSchemaDictionary;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;

public class SchemaPopulator extends SchemaDenitiator {
	
	public SchemaPopulator() {
		super();
	}
	
	public SchemaPopulator(String schemaName) {
		super(schemaName);
	}

	@Override
	public Boolean call() throws Exception {
    	logger.trace("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			try {
				populateSchema(hz);
				result = true;
			} catch (Exception ex) {
		    	logger.error("call.error; on Schema population", ex);
			}
		}
    	logger.trace("call.exit; schema {} populated: {}", schemaName, result);
		return result;
	}

	private void populateSchema(HazelcastInstance hz) {

    	logger.trace("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = (ApplicationContext) hz.getUserContext().get("appContext");
		ApplicationContext storeCtx = (ApplicationContext) hz.getUserContext().get("storeContext");
    	
    	//IMap dtCache = hz.getMap("dict-document-type");
		//MapConfig dtConfig = hz.getConfig().getMapConfig("dict-document-type");
		//MapStoreFactory msFactory = (MapStoreFactory) dtConfig.getMapStoreConfig().getFactoryImplementation();
		//MapLoader populator = msFactory.newMapStore("dict-document-type", properties);
    	
    	//((ConfigurableApplicationContext) ctx).

		XDMSchemaDictionary schemaDict = schemaCtx.getBean("xdmDictionary", HazelcastSchemaDictionary.class);
		//<bean id="eltCacheStore" class="com.bagri.xdm.cache.hazelcast.store.xml.ElementCacheStore">
		MapLoaderLifecycleSupport eltCacheStore = storeCtx.getBean("eltCacheStore", MapLoaderLifecycleSupport.class);
		
		String cacheName = "xdm-elements";
		Properties props = new Properties();
		props.put("xdmDictionary", schemaDict);
		eltCacheStore.init(hz, props, cacheName);

		int size = 0;
		IMap<String, XDMDocument> xddCache = hz.getMap("xdm-documents");
		Set<String> dKeys = xddCache.localKeySet();
		if (dKeys.size() == 0) {
			MapLoader docCacheStore = storeCtx.getBean("docCacheStore", MapLoader.class);
			dKeys = docCacheStore.loadAllKeys();
			size = xddCache.size();
		}
		Map<String, XDMDocument> docs = xddCache.getAll(dKeys);
    	logger.trace("populateSchema; documents keys loaded: {}; documents returned: {}; cache size: {}", dKeys.size(), docs.size(), size);
		
    	size = 0;
		IMap<XDMDataKey, XDMElement> xdmCache = hz.getMap(cacheName);
		Set<XDMDataKey> eKeys = xdmCache.localKeySet();
		if (eKeys.size() == 0) {
			eKeys = ((MapLoader) eltCacheStore).loadAllKeys();
			size = xdmCache.size();
		}
		Map<XDMDataKey, XDMElement> elts = xdmCache.getAll(eKeys);
    	logger.trace("populateSchema.exit; elements keys loaded: {}; elements returned: {}; cache size: {}", eKeys.size(), elts.size(), size);
	}

	@Override
	public int getId() {
		return cli_XDMPopulateSchemaTask;
	}

}
