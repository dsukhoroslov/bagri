package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.core.MapStore;

public class DataFormatCacheStore extends ConfigCacheStore<String, XDMDataFormat> implements MapStore<String, XDMDataFormat> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMDataFormat> loadEntities() {
		Collection<XDMDataFormat> formats = (Collection<XDMDataFormat>) cfg.getEntities(XDMDataFormat.class); 
		Map<String, XDMDataFormat> result = new HashMap<String, XDMDataFormat>(formats.size());
		for (XDMDataFormat format: formats) {
			result.put(format.getName(), format);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, XDMDataFormat> entities) {
		cfg.setEntities(XDMDataFormat.class, entities.values());
	}

}
