package com.bagri.xdm.cache.hazelcast.store;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MapStore;

public class XDMDummyCacheStore implements MapStore<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(XDMDummyCacheStore.class);

	@Override
	public Object load(Object key) {
		logger.trace("load.enter; key: {}", key);
		return null;
	}

	@Override
	public Map<Object, Object> loadAll(Collection<Object> keys) {
		logger.trace("loadAll.enter; keys: {}", keys.size());
		return null;
	}

	@Override
	public Set<Object> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
		return null;
	}

	@Override
	public void delete(Object key) {
		logger.trace("delete.enter; key: {}", key);
	}

	@Override
	public void deleteAll(Collection<Object> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys.size());
	}

	@Override
	public void store(Object key, Object value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
	}

	@Override
	public void storeAll(Map<Object, Object> entries) {
		logger.trace("storeAll.enter; entries: {}", entries.size());
	}

}
