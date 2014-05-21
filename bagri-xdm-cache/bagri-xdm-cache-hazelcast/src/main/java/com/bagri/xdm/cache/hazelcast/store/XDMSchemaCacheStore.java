package com.bagri.xdm.cache.hazelcast.store;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bagri.xdm.cache.hazelcast.management.ConfigManagement;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class XDMSchemaCacheStore extends ConfigCacheStore<String, XDMSchema> implements MapStore<String, XDMSchema> { 

	@Override
	protected Map<String, XDMSchema> loadEntities() {
		Map<String, XDMSchema> schemas = new HashMap<String, XDMSchema>();
		for (XDMSchema schema: cfg.getSchemas()) {
			schemas.put(schema.getName(), schema);
	    }
		return schemas;
	}

	@Override
	protected void storeEntities(Map<String, XDMSchema> entities) {
		cfg.setSchemas(entities.values());
	}

}
