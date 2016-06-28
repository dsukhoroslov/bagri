package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.Schema;
import com.hazelcast.core.MapStore;

public class SchemaCacheStore extends ConfigCacheStore<String, Schema> implements MapStore<String, Schema> { 

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Schema> loadEntities() {
		Map<String, Schema> schemas = new HashMap<String, Schema>();
		Collection<Schema> cSchemas = (Collection<Schema>) cfg.getEntities(Schema.class); 
		for (Schema schema: cSchemas) {
			schemas.put(schema.getName(), schema);
	    }
		return schemas;
	}

	@Override
	protected void storeEntities(Map<String, Schema> entities) {
		cfg.setEntities(Schema.class, entities.values());
	}

}
