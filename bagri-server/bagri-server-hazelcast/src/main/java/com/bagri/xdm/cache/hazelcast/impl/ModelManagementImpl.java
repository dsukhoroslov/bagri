package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bagri.xdm.domain.Path;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

public class ModelManagementImpl extends com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl {
	
	private ConcurrentMap<Integer, Path> cachePath = new ConcurrentHashMap<>();
	private ConcurrentMap<Integer, Set<Path>> cacheType = new ConcurrentHashMap<>();

	public ModelManagementImpl() {
		super();
	}
	
	public ModelManagementImpl(HazelcastInstance hzInstance) {
		super();
		initialize();
	}
	
	private void initialize() {
		// init listeners here
		pathCache.addEntryListener(new PathCacheListener(), true);
	}
	
	@Override
	public Path getPath(int pathId) {
		Path result = cachePath.get(pathId);
		if (result == null) {
			result = super.getPath(pathId);
			if (result != null) {
				cachePath.putIfAbsent(pathId, result);
			}
		}
		return result;
	}
	
	@Override
	public Collection<Path> getTypePaths(int typeId) {
		Collection<Path> result = cacheType.get(typeId);
		// TODO: think why the result is empty? happens from ModelManagementImplTest only?
		if (result == null || result.isEmpty()) {
			result = super.getTypePaths(typeId);
			if (result != null) {
				Set<Path> paths = new HashSet<>(result);
				paths = new HashSet<>();
				cacheType.putIfAbsent(typeId, paths);
			}
		}
		return result;
	}
	
	//@Override
	//protected Set getTypedPathEntries(int typeId) {
		//Predicate f = Predicates.equal("typeId",  typeId);
		//Set<Map.Entry<String, XDMPath>> entries = pathCache.entrySet(f);
		//return entries;
	//	return null;
	//}
	
	//@Override
	//protected Set getTypedPathWithRegex(String regex, int typeId) {
	//	return null;
	//}	
	
	
	private class PathCacheListener implements MapClearedListener, MapEvictedListener,
		EntryAddedListener<String, Path>, EntryRemovedListener<String, Path>, EntryUpdatedListener<String, Path> {

		@Override
		public void mapEvicted(MapEvent event) {
			// don't think we have to clear everything in this case
		}

		@Override
		public void mapCleared(MapEvent event) {
			cachePath.clear();
			cacheType.clear();
		}
		
		@Override
		public void entryUpdated(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.put(path.getPathId(), path);
			Set<Path> paths = cacheType.get(path.getTypeId());
			if (paths == null) {
				paths = new HashSet<>();
				Set<Path> paths2 = cacheType.putIfAbsent(path.getTypeId(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}

		@Override
		public void entryRemoved(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.remove(path.getPathId());
			Set<Path> paths = cacheType.get(path.getTypeId());
			if (paths != null) {
				paths.remove(path);
			}
		}

		@Override
		public void entryAdded(EntryEvent<String, Path> event) {
			Path path = event.getValue();
			cachePath.putIfAbsent(path.getPathId(), path);
			Set<Path> paths = cacheType.get(path.getTypeId());
			if (paths == null) {
				paths = new HashSet<>();
				Set<Path> paths2 = cacheType.putIfAbsent(path.getTypeId(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}

	}
	
}
