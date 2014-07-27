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
				// @TODO: ensure that partitions migration has been already finished! 
				result = populateSchema(hz);
			} catch (Exception ex) {
		    	logger.error("call.error; on Schema population", ex);
			}
		}
    	logger.trace("call.exit; schema {} populated: {}", schemaName, result);
		return result;
	}

	private boolean populateSchema(HazelcastInstance hz) {

    	logger.trace("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = (ApplicationContext) hz.getUserContext().get("appContext");
		ApplicationContext storeCtx = (ApplicationContext) hz.getUserContext().get("storeContext");
    	
		XDMSchemaDictionary schemaDict = schemaCtx.getBean("xdmDictionary", HazelcastSchemaDictionary.class);
		MapLoader docCacheStore = storeCtx.getBean("docCacheStore", MapLoader.class);
		MapLoader eltCacheStore = storeCtx.getBean("eltCacheStore", MapLoader.class);
		
		Properties props = new Properties();
		props.put("ready", true);
		((MapLoaderLifecycleSupport) docCacheStore).init(hz, props, CN_XDM_DOCUMENT);
		props.clear();
		props.put("xdmDictionary", schemaDict);
		((MapLoaderLifecycleSupport) eltCacheStore).init(hz, props, CN_XDM_ELEMENT);
		PartitionService pSvc = hz.getPartitionService();

		IMap<String, XDMDocument> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		Set<String> dKeys = docCacheStore.loadAllKeys();
		filterExternalKeys(dKeys, pSvc);
		if (dKeys.size() == 0) {
			logger.info("populateSchema; no local document keys found");
			return false;
		}
		
		IMap<XDMDataKey, XDMElement> xdmCache = hz.getMap(CN_XDM_ELEMENT);
		Set<XDMDataKey> eKeys = eltCacheStore.loadAllKeys();
		filterExternalKeys(eKeys, pSvc);
		
		Map<String, XDMDocument> docs = xddCache.getAll(dKeys);
		if (dKeys.size() > 0 && docs.size() == 0) {
			int populated = 0;
			docs = docCacheStore.loadAll(dKeys);
			for (Map.Entry<String, XDMDocument> e: docs.entrySet()) {
				xddCache.putTransient(e.getKey(), e.getValue(), 0, TimeUnit.DAYS);
				populated++;
			}
	    	logger.trace("populateSchema; documents keys left: {}; documents loaded: {}; documents put: {}", 
	    			dKeys.size(), docs.size(), populated);
		} else {
			logger.trace("populateSchema; documents keys loaded: {}; documents returned: {};", 
					dKeys.size(), docs.size());
		}
		
		Map<XDMDataKey, XDMElement> elts = xdmCache.getAll(eKeys);
		if (eKeys.size() > 0 && elts.size() == 0) {
			int populated = 0;
			elts = eltCacheStore.loadAll(eKeys);
			for (Map.Entry<XDMDataKey, XDMElement> e: elts.entrySet()) {
				xdmCache.putTransient(e.getKey(), e.getValue(), 0, TimeUnit.DAYS);
				populated++;
			}
	    	logger.trace("populateSchema; elements keys left: {}; elements loaded: {}; elements put: {}", 
	    			eKeys.size(), elts.size(), populated);
		} else {
			logger.trace("populateSchema.exit; elements keys loaded: {}; elements returned: {}", 
					eKeys.size(), elts.size());
		}
		
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
