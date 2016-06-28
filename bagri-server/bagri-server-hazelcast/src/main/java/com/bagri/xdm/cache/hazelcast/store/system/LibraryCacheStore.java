package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.Library;
import com.hazelcast.core.MapStore;

public class LibraryCacheStore extends ConfigCacheStore<String, Library> implements MapStore<String, Library> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Library> loadEntities() {
		Collection<Library> libs = (Collection<Library>) cfg.getEntities(Library.class); 
		Map<String, Library> result = new HashMap<String, Library>(libs.size());
		for (Library lib: libs) {
			result.put(lib.getName(), lib);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, Library> entities) {
		cfg.setEntities(Library.class, entities.values());
	}

}
