package com.bagri.xdm.cache.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.idgen.SimpleIdGenerator;
import com.bagri.xdm.domain.DocumentType;
import com.bagri.xdm.domain.Namespace;
import com.bagri.xdm.domain.Path;

public class ModelManagementImpl extends ModelManagementBase {
	
	private Map<String, Namespace> nsCache = new ConcurrentHashMap<>();
	private Map<String, Path> pathCache = new ConcurrentHashMap<>();
	private Map<String, DocumentType> typeCache = new ConcurrentHashMap<>();
	private IdGenerator<Long> nsGen = new SimpleIdGenerator(0);
	private IdGenerator<Long> pathGen = new SimpleIdGenerator(0);
	private IdGenerator<Long> typeGen = new SimpleIdGenerator(0);

	@Override
	protected Map<String, Namespace> getNamespaceCache() {
		return nsCache;
	}

	@Override
	protected Map<String, Path> getPathCache() {
		return pathCache;
	}

	@Override
	protected Map<String, DocumentType> getTypeCache() {
		return typeCache;
	}

	@Override
	protected IdGenerator<Long> getNamespaceGen() {
		return nsGen;
	}

	@Override
	protected IdGenerator<Long> getPathGen() {
		return pathGen;
	}

	@Override
	protected IdGenerator<Long> getTypeGen() {
		return typeGen;
	}

	@Override
	protected <K> boolean lock(Map<K, ?> cache, K key) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
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
	public Collection<Path> getTypePaths(int typeId) {
		List<Path> paths = new ArrayList<>();
		for (Path path: pathCache.values()) {
			if (path.getTypeId() == typeId) {
				paths.add(path);
			}
		}
		return paths;
	}

	@Override
	protected DocumentType getDocumentTypeById(int typeId) {
		for (DocumentType type: typeCache.values()) {
			if (type.getTypeId() == typeId) {
				return type;
			}
		}
		return null;
	}

	@Override
	protected Set<Map.Entry<String, Path>> getTypedPathEntries(int typeId) {
		Set<Map.Entry<String, Path>> entries = new HashSet<>();
		for (Map.Entry<String, Path> entry: pathCache.entrySet()) {
			if (entry.getValue().getTypeId() == typeId) {
				entries.add(entry);
			}
		}
		return entries;
	}

	@Override
	protected Set<Map.Entry<String, Path>> getTypedPathWithRegex(String regex, int typeId) {
		Set<Map.Entry<String, Path>> entries = new HashSet<>();
		Pattern pattern = Pattern.compile(regex);
		for (Map.Entry<String, Path> entry: pathCache.entrySet()) {
			Path path = entry.getValue();
			if (pattern.matcher(path.getPath()).matches()) {
				if (typeId > 0 && path.getTypeId() != typeId) {
					continue;
				}
				entries.add(entry);
			}
		}
		return entries;
	}

}