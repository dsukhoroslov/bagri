package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMPopulateSchemaTask;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.process.hazelcast.SpringContextHolder;
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
    	logger.debug("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			try {
				// @TODO: ensure that partitions migration has been already finished! 
				result = populateSchema(hz);
			} catch (Exception ex) {
		    	logger.error("call.error; on Schema population", ex);
			}
		}
    	logger.debug("call.exit; schema {} populated: {}", schemaName, result);
		return result;
	}

	private boolean populateSchema(HazelcastInstance hz) {

    	logger.trace("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
		ApplicationContext storeCtx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "storeContext");
    	
		if (storeCtx == null) {
			// schema configured with no persistent store
	    	logger.trace("populateSchema.exit; No persistent store configured");
			return false;
		}
		
		MapLoader docCacheStore = storeCtx.getBean("docCacheStore", MapLoader.class);
		//MapLoader eltCacheStore = storeCtx.getBean("eltCacheStore", MapLoader.class);
		
		Properties props = new Properties();
		//props.put("ready", true);
		//props.put("documentIdGenerator", schemaCtx.getBean("xdm.document"));
		props.put("xdmDictionary", schemaCtx.getBean("xdmDictionary"));
		props.put("keyFactory", schemaCtx.getBean("xdmFactory"));
		//props.put("dataPath", "get path from schema properties..");
		props.put("xdmManager", schemaCtx.getBean("xdmManager"));
		((MapLoaderLifecycleSupport) docCacheStore).init(hz, props, CN_XDM_DOCUMENT);
		
		//props.clear();
		//props.put("xdmDictionary", schemaCtx.getBean("xdmDictionary"));
		//props.put("elementIdGenerator", schemaCtx.getBean("eltGen"));
		//props.put("keyFactory", schemaCtx.getBean("xdmFactory"));
		//((MapLoaderLifecycleSupport) eltCacheStore).init(hz, props, CN_XDM_ELEMENT);

		//PartitionService pSvc = hz.getPartitionService();
		//
		IMap<Long, XDMDocument> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		xddCache.loadAll(false);
    	logger.trace("populateSchema; documents size after loadAll: {}", xddCache.size());
		
		//Set<Long> dKeys = docCacheStore.loadAllKeys();
		//filterExternalKeys(dKeys, pSvc);
		//if (dKeys.size() == 0) {
		//	logger.info("populateSchema; no local document keys found");
		//	return false;
		//}
		
		//IMap<XDMDataKey, XDMElements> xdmCache = hz.getMap(CN_XDM_ELEMENT);
		//xdmCache.loadAll(false);
    	//logger.trace("populateSchema; elements size after loadAll: {}", xdmCache.size());

    	//Set<XDMDataKey> eKeys = eltCacheStore.loadAllKeys();
		//filterExternalKeys(eKeys, pSvc);
/*		
		Map<Long, XDMDocument> docs = xddCache.getAll(dKeys);
		if (docs.size() > 0 && xddCache.size() == 0) {
			int populated = 0;
			//docs = docCacheStore.loadAll(dKeys);
			for (Map.Entry<Long, XDMDocument> e: docs.entrySet()) {
				xddCache.putTransient(e.getKey(), e.getValue(), 0, TimeUnit.DAYS);
				populated++;
			}
	    	logger.trace("populateSchema; documents keys left: {}; documents loaded: {}; documents put: {}", 
	    			dKeys.size(), docs.size(), populated);
		} else {
			logger.trace("populateSchema; documents keys loaded: {}; documents returned: {}; cache size: {}", 
					dKeys.size(), docs.size(), xddCache.size());
		}
		
		Map<XDMDataKey, XDMElements> elts = xdmCache.getAll(eKeys);
		if (elts.size() > 0 && xdmCache.size() == 0) {
			int populated = 0;
			//elts = eltCacheStore.loadAll(eKeys);
			for (Map.Entry<XDMDataKey, XDMElements> e: elts.entrySet()) {
				xdmCache.putTransient(e.getKey(), e.getValue(), 0, TimeUnit.DAYS);
				populated++;
			}
	    	logger.trace("populateSchema; elements keys left: {}; elements loaded: {}; elements put: {}", 
	    			eKeys.size(), elts.size(), populated);
		} else {
			logger.trace("populateSchema.exit; elements keys loaded: {}; elements returned: {}; cache size: {}", 
					eKeys.size(), elts.size(), xdmCache.size());
		}
*/		
		return true;
	}
	
	private void filterExternalKeys(Set keys, PartitionService pSvc) {
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			Object key = itr.next();
			Partition p = pSvc.getPartition(key);
			if (!p.getOwner().localMember()) {
				itr.remove();
			}
		}
		
	}
	
	@Override
	public int getId() {
		return cli_XDMPopulateSchemaTask;
	}

}
