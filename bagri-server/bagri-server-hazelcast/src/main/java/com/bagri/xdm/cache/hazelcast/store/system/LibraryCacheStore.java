package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMLibrary;
import com.hazelcast.core.MapStore;

public class LibraryCacheStore extends ConfigCacheStore<String, XDMLibrary> implements MapStore<String, XDMLibrary> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMLibrary> loadEntities() {
		Collection<XDMLibrary> libs = (Collection<XDMLibrary>) cfg.getEntities(XDMLibrary.class); 
		Map<String, XDMLibrary> result = new HashMap<String, XDMLibrary>(libs.size());
		for (XDMLibrary lib: libs) {
			result.put(lib.getName(), lib);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, XDMLibrary> entities) {
		cfg.setEntities(XDMLibrary.class, entities.values());
	}

}
