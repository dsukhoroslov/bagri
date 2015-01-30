package com.bagri.xdm.cache.hazelcast.store;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.cache.store.DataStore;
import com.hazelcast.core.MapStore;


public class DataStoreAdapter<K, V> implements MapStore<K, V> {
	
	private DataStore<K, V> dataStore;
	
	public DataStoreAdapter(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	public V load(K key) {
		return dataStore.load(key);
	}

	@Override
	public Map<K, V> loadAll(Collection<K> keys) {
		return dataStore.loadAll(keys);
	}

	@Override
	public Set<K> loadAllKeys() {
		return dataStore.loadAllKeys();
	}

	@Override
	public void store(K key, V value) {
		dataStore.store(key, value);
	}

	@Override
	public void storeAll(Map<K, V> map) {
		dataStore.storeAll(map);
	}

	@Override
	public void delete(K key) {
		dataStore.delete(key);
	}

	@Override
	public void deleteAll(Collection<K> keys) {
		dataStore.deleteAll(keys);
	}

}
