package com.bagri.xdm.cache.hazelcast.store.xml;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMSchemaDictionary;
import com.bagri.xdm.api.XDMSchemaDictionaryBase;
import com.hazelcast.core.MapStore;

public class XsdCacheStore extends XmlCacheStore implements MapStore {

    private static final Logger logger = LoggerFactory.getLogger(XsdCacheStore.class);
    
	protected XDMSchemaDictionary schemaDictionary;
    
	public void setSchemaDictionary(XDMSchemaDictionary dictionary) {
		this.schemaDictionary = dictionary;
	}
    
	@Override
	public Object load(Object key) {
		logger.trace("load.enter; key: {}", key);
		return null;
	}

	@Override
	public Map loadAll(Collection keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		return null;
	}

	@Override
	public Set loadAllKeys() {
		logger.trace("loadAllKeys.enter; path: {}", getDataPath());
		if (schemaDictionary == null) {
			logger.debug("loadAllKeys; dictionary is not set yet.");
		} else {
			((XDMSchemaDictionaryBase) schemaDictionary).registerSchemas(getDataPath());
		}
		return null;
	}

	@Override
	public void store(Object key, Object value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAll(Map entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
	}

	@Override
	public void delete(Object key) {
		logger.trace("delete.enter; key: {}", key);
	}

	@Override
	public void deleteAll(Collection keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
	}

}
