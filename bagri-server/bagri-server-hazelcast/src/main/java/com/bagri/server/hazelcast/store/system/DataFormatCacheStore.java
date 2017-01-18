package com.bagri.server.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.system.DataFormat;
import com.hazelcast.core.MapStore;

public class DataFormatCacheStore extends ConfigCacheStore<String, DataFormat> implements MapStore<String, DataFormat> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, DataFormat> loadEntities() {
		Collection<DataFormat> formats = (Collection<DataFormat>) cfg.getEntities(DataFormat.class); 
		Map<String, DataFormat> result = new HashMap<String, DataFormat>(formats.size());
		for (DataFormat format: formats) {
			result.put(format.getName(), format);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, DataFormat> entities) {
		cfg.setEntities(DataFormat.class, entities.values());
	}

}
