package com.bagri.xdm.cache.hazelcast.store.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bagri.xdm.system.Module;
import com.hazelcast.core.MapStore;

import static com.bagri.common.util.FileUtils.*;

public class ModuleCacheStore extends ConfigCacheStore<String, Module> implements MapStore<String, Module> {

	@Override
	@SuppressWarnings("unchecked")
	protected Map<String, Module> loadEntities() {
		Collection<Module> modules = (Collection<Module>) cfg.getEntities(Module.class); 
		Map<String, Module> result = new HashMap<String, Module>(modules.size());
		for (Module module: modules) {
			result.put(module.getName(), module);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, Module> entities) {
		cfg.setEntities(Module.class, entities.values());
	}

	// TODO: add relative path handling
	
	private void deleteModule(Module module) {
    	Path path = Paths.get(module.getFileName());
		try {
			Files.deleteIfExists(path);
		} catch (IOException ex) {
			logger.error("deleteModule.error", ex);
		}
	}
	
	public static Module loadModule(Module module) throws IOException {
		if (module == null) {
			return null;
		}
		
		String body = readTextFile(module.getFileName());
		module.setBody(body);
		return module;
	}

	private void storeModule(Module module) {
		try {
			writeTextFile(module.getFileName(), module.getBody());
		} catch (IOException ex) {
			logger.warn("storeModule.error", ex.getMessage());
		}
	}
	
	@Override
	public Module load(String key) {
		Module module = super.load(key);
		try {
			return loadModule(module);
		} catch (IOException ex) {
			logger.warn("load.error: {}; at key: {}", ex.getMessage(), key);
		}
		return null;
	}

	@Override
	public Map<String, Module> loadAll(Collection<String> keys) {
		Map<String, Module> result = super.loadAll(keys);
		for (Module module: result.values()) {
			try {
				loadModule(module);
			} catch (IOException ex) {
				logger.warn("loadAll.error: {}; at module: {}", ex.getMessage(), module);
			}
		}
		return result;
	}

	@Override
	public void store(String key, Module value) {
		super.store(key, value);
		storeModule(value);
	}
	
	@Override
	public void storeAll(Map<String, Module> map) {
		super.storeAll(map);
		for (Module module: map.values()) {
			storeModule(module);
		}
	}
	
	@Override
	public void delete(String key) {
		Module module = entities.get(key);
		super.delete(key);
		if (module != null && !entities.containsKey(key)) {
			deleteModule(module);
		}
	}
	
	@Override
	public void deleteAll(Collection<String> keys) {
		List<Module> modules = new ArrayList<>(keys.size());
		for (String key: keys) {
			Module module = entities.get(key);
			if (module != null) {
				modules.add(module);
			}
		}
		super.deleteAll(keys);
		for (Module module: modules) {
			if (!entities.containsKey(module.getName())) {
				deleteModule(module);
			}
		}
	}

}