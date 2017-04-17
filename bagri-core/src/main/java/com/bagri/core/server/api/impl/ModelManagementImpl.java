package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.bagri.core.model.Path;
import com.bagri.support.idgen.IdGenerator;
import com.bagri.support.idgen.SimpleIdGenerator;

public class ModelManagementImpl extends ModelManagementBase {
	
	private Map<String, Path> pathCache = new ConcurrentHashMap<>();
	private IdGenerator<Long> pathGen = new SimpleIdGenerator(0);

	@Override
	protected Map<String, Path> getPathCache() {
		return pathCache;
	}

	@Override
	protected IdGenerator<Long> getPathGen() {
		return pathGen;
	}

	//@Override
	protected <K> boolean lock(Map<K, ?> cache, K key) {
		// TODO Auto-generated method stub
		return true;
	}

	//@Override
	protected <K> void unlock(Map<K, ?> cache, K key) { 
		// TODO Auto-generated method stub
	}

	@Override
	protected <K, V> V putIfAbsent(Map<K, V> cache, K key, V value) {
		V val2 = ((ConcurrentHashMap<K, V>) cache).putIfAbsent(key, value);
		if (val2 == null) {
			return value;
		}
		logger.debug("putIfAbsent; got collision on key: {}; returning: {}", key, val2);
		return val2;
	}

	//@Override
	//protected <K, V> V putPathIfAbsent(Map<K, V> cache, K key, V value) {
	//	return putIfAbsent(cache, key, value);
	//}
	
	@Override
	public Path getPath(int pathId) {
		for (Path path: pathCache.values()) {
			if (path.getPathId() == pathId) {
				return path;
			}
		}
		return null;
	}

	@Override
	public Collection<Path> getTypePaths(String root) {
		List<Path> paths = new ArrayList<>();
		for (Path path: pathCache.values()) {
			if (path.getRoot().equals(root)) {
				paths.add(path);
			}
		}
		return paths;
	}

	@Override
	protected Set<Map.Entry<String, Path>> getTypedPathEntries(String root) {
		Set<Map.Entry<String, Path>> entries = new HashSet<>();
		for (Map.Entry<String, Path> entry: pathCache.entrySet()) {
			if (entry.getValue().getRoot().equals(root)) {
				entries.add(entry);
			}
		}
		return entries;
	}

	@Override
	protected Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, String root) {
		Set<Map.Entry<String, Path>> entries = new HashSet<>();
		Pattern pattern = Pattern.compile(regex);
		for (Map.Entry<String, Path> entry: pathCache.entrySet()) {
			Path path = entry.getValue();
			if (pattern.matcher(path.getPath()).matches()) {
				if (path.getRoot().equals(root)) {
					entries.add(entry);
				}
			}
		}
		return entries;
	}

}