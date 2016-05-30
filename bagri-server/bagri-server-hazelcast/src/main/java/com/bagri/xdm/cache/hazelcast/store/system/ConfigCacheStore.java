package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.config.EntityConfig;

public abstract class ConfigCacheStore<K, V> {
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected EntityConfig cfg;
    protected Map<K, V> entities;
	

	public void setEntityManagement(EntityConfig cfg) {
		this.cfg = cfg;
		entities = loadEntities();
	}
	
	abstract protected Map<K, V> loadEntities();
	abstract protected void storeEntities(Map<K, V> entities);

	public Map<K, V> loadAll(Collection<K> keys) {
		logger.trace("loadAll.enter; keys: {}", keys);
		Map<K, V> result = new HashMap<K, V>(keys.size());
		for (K key: keys) {
			V value = entities.get(key);
			if (value != null) {
				result.put(key, value);
			}
		}
		logger.trace("loadAll.exit; returning {} entities", result.size());
		return result;
	}
	
	public V load(K key) {
		logger.trace("load.enter; key: {}", key);
		return entities.get(key);
	}

	public Set<K> loadAllKeys() {
		logger.trace("loadAllKeys.enter;");
		Set<K> result = new HashSet<K>(entities.keySet());
		logger.trace("loadAllKeys.exit; returning {} keys", result.size());
		return result;
	}

	public void store(K key, V value) {
		logger.trace("store.enter; key: {}; value: {}", key, value);
		entities.put(key, value);
		storeEntities(entities);
	}

	public void storeAll(Map<K, V> map) {
		logger.trace("storeAll.enter; map: {}", map);
		entities.putAll(map);
		storeEntities(entities);
	}

	public void delete(K key) {
		logger.trace("delete.enter; key: {}", key);
		if (entities.remove(key) != null) {
			storeEntities(entities);
		}
	}

	public void deleteAll(Collection<K> keys) {
		logger.trace("deleteAll.enter; keys: {}", keys);
		boolean changed = false;
		for (K key: keys) {
			if (entities.remove(key) != null) {
				changed = true;
			}
		}
		if (changed) {
			storeEntities(entities);
		}
	}

}
