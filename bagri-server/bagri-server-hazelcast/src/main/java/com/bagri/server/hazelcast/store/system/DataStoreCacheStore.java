package com.bagri.server.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.system.DataStore;
import com.hazelcast.core.MapStore;

public class DataStoreCacheStore extends ConfigCacheStore<String, DataStore> implements MapStore<String, DataStore> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, DataStore> loadEntities() {
		Collection<DataStore> stores = (Collection<DataStore>) cfg.getEntities(DataStore.class); 
		Map<String, DataStore> result = new HashMap<String, DataStore>(stores.size());
		for (DataStore store: stores) {
			result.put(store.getName(), store);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, DataStore> entities) {
		cfg.setEntities(DataStore.class, entities.values());
	}



}
