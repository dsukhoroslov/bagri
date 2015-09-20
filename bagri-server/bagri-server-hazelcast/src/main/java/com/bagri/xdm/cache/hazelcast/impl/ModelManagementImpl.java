package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bagri.xdm.domain.XDMPath;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

public class ModelManagementImpl extends com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl {
	
	private ConcurrentMap<Integer, XDMPath> cachePath = new ConcurrentHashMap<>();
	private ConcurrentMap<Integer, Set<XDMPath>> cacheType = new ConcurrentHashMap<>();

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
	public XDMPath getPath(int pathId) {
		XDMPath result = cachePath.get(pathId);
		if (result == null) {
			result = super.getPath(pathId);
			if (result != null) {
				cachePath.putIfAbsent(pathId, result);
			}
		}
		return result;
	}
	
	@Override
	public Collection<XDMPath> getTypePaths(int typeId) {
		Collection<XDMPath> result = cacheType.get(typeId);
		if (result == null) {
			result = super.getTypePaths(typeId);
			if (result != null) {
				Set<XDMPath> paths = new HashSet<>(result);
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
		EntryAddedListener<String, XDMPath>, EntryRemovedListener<String, XDMPath>, EntryUpdatedListener<String, XDMPath> {

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
		public void entryUpdated(EntryEvent<String, XDMPath> event) {
			XDMPath path = event.getValue();
			cachePath.put(path.getPathId(), path);
			Set<XDMPath> paths = cacheType.get(path.getTypeId());
			if (paths == null) {
				paths = new HashSet<>();
				Set<XDMPath> paths2 = cacheType.putIfAbsent(path.getTypeId(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}

		@Override
		public void entryRemoved(EntryEvent<String, XDMPath> event) {
			XDMPath path = event.getValue();
			cachePath.remove(path.getPathId());
			Set<XDMPath> paths = cacheType.get(path.getTypeId());
			if (paths != null) {
				paths.remove(path);
			}
		}

		@Override
		public void entryAdded(EntryEvent<String, XDMPath> event) {
			XDMPath path = event.getValue();
			cachePath.putIfAbsent(path.getPathId(), path);
			Set<XDMPath> paths = cacheType.get(path.getTypeId());
			if (paths == null) {
				paths = new HashSet<>();
				Set<XDMPath> paths2 = cacheType.putIfAbsent(path.getTypeId(), paths);
				if (paths2 != null) {
					paths = paths2;
				}
			}
			paths.add(path);
		}

	}
	
}
