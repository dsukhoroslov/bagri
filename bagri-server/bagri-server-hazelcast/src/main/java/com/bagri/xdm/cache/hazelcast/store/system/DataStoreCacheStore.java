package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMDataStore;
import com.hazelcast.core.MapStore;

public class DataStoreCacheStore extends ConfigCacheStore<String, XDMDataStore> implements MapStore<String, XDMDataStore> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMDataStore> loadEntities() {
		Collection<XDMDataStore> stores = (Collection<XDMDataStore>) cfg.getEntities(XDMDataStore.class); 
		Map<String, XDMDataStore> result = new HashMap<String, XDMDataStore>(stores.size());
		for (XDMDataStore store: stores) {
			result.put(store.getName(), store);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, XDMDataStore> entities) {
		cfg.setEntities(XDMDataStore.class, entities.values());
	}



}
