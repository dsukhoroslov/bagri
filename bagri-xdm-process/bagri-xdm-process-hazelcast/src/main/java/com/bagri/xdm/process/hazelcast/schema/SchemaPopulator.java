package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMPopulateSchemaTask;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_ELEMENT;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;

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
		MapLoader docCacheStore = storeCtx.getBean("docCacheStore", MapLoader.class);
		MapLoader eltCacheStore = storeCtx.getBean("eltCacheStore", MapLoader.class);
		
		Properties props = new Properties();
		props.put("xdmDictionary", schemaDict);
		((MapLoaderLifecycleSupport) eltCacheStore).init(hz, props, CN_XDM_ELEMENT);
		PartitionService pSvc = hz.getPartitionService();

		int size = 0;
		IMap<String, XDMDocument> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		Set<String> dKeys = xddCache.localKeySet();
		if (dKeys.size() == 0) {
			dKeys = docCacheStore.loadAllKeys();
			size = xddCache.size();
		}
		Map<String, XDMDocument> docs = xddCache.getAll(dKeys);
		if (dKeys.size() > 0 && docs.size() == 0) {
			Iterator<String> itr = dKeys.iterator();
			while (itr.hasNext()) {
				String uri = itr.next();
				Partition p = pSvc.getPartition(uri);
				if (!p.getOwner().localMember()) {
					itr.remove();
				}
			}
			
			docs = docCacheStore.loadAll(dKeys);
			for (Map.Entry<String, XDMDocument> e: docs.entrySet()) {
				xddCache.putTransient(e.getKey(), e.getValue(), 0, TimeUnit.DAYS);
				size++;
			}
	    	logger.trace("populateSchema; documents keys left: {}; documents loaded: {}; documents put: {}", dKeys.size(), docs.size(), size);
		} else {
			logger.trace("populateSchema; documents keys loaded: {}; documents returned: {}; cache size: {}", dKeys.size(), docs.size(), size);
		}
		
    	size = 0;
		IMap<XDMDataKey, XDMElement> xdmCache = hz.getMap(CN_XDM_ELEMENT);
		Set<XDMDataKey> eKeys = xdmCache.localKeySet();
		if (eKeys.size() == 0) {
			eKeys = eltCacheStore.loadAllKeys();
			size = xdmCache.size();
		}
		Map<XDMDataKey, XDMElement> elts = xdmCache.getAll(eKeys);
		if (eKeys.size() > 0 && elts.size() == 0) {
			Iterator<XDMDataKey> itr = eKeys.iterator();
			while (itr.hasNext()) {
				XDMDataKey key = itr.next();
				Partition p = pSvc.getPartition(key);
				if (!p.getOwner().localMember()) {
					itr.remove();
				}
			}
			
			elts = eltCacheStore.loadAll(eKeys);
			for (Map.Entry<XDMDataKey, XDMElement> e: elts.entrySet()) {
				xdmCache.putTransient(e.getKey(), e.getValue(), 0, TimeUnit.DAYS);
				size++;
			}
	    	logger.trace("populateSchema; elements keys left: {}; elements loaded: {}; elements put: {}", eKeys.size(), elts.size(), size);
		} else {
			logger.trace("populateSchema.exit; elements keys loaded: {}; elements returned: {}; cache size: {}", eKeys.size(), elts.size(), size);
		}
	}
	
	@Override
	public int getId() {
		return cli_XDMPopulateSchemaTask;
	}

}
