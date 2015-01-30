package com.bagri.xdm.cache.store;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Common DataStore interface to adapt into 
 * Coherence CacheStore and Hazelcast MapStore interfaces
 * 
 * @author Denis Sukhoroslov
 *
 * @param <K>
 * @param <V>
 */
public interface DataStore<K, V> {

	public V load(K key);

	public Map<K, V> loadAll(Collection<K> keys);

	public Set<K> loadAllKeys();

	public void store(K key, V value);

	public void storeAll(Map<K, V> entries);

	public void delete(K key);

	public void deleteAll(Collection<K> keys);
	
}
