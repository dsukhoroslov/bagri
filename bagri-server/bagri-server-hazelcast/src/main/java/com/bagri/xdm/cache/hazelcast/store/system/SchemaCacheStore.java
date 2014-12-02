package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.MapStore;

public class SchemaCacheStore extends ConfigCacheStore<String, XDMSchema> implements MapStore<String, XDMSchema> { 

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMSchema> loadEntities() {
		Map<String, XDMSchema> schemas = new HashMap<String, XDMSchema>();
		Collection<XDMSchema> cSchemas = (Collection<XDMSchema>) cfg.getEntities(XDMSchema.class); 
		for (XDMSchema schema: cSchemas) {
			schemas.put(schema.getName(), schema);
	    }
		return schemas;
	}

	@Override
	protected void storeEntities(Map<String, XDMSchema> entities) {
		cfg.setEntities(XDMSchema.class, entities.values());
	}

}
