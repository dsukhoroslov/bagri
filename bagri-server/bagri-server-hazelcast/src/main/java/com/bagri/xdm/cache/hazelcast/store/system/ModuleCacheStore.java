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

import com.bagri.xdm.system.XDMModule;
import com.hazelcast.core.MapStore;

import static com.bagri.common.util.FileUtils.*;

public class ModuleCacheStore extends ConfigCacheStore<String, XDMModule> implements MapStore<String, XDMModule> {

	@Override
	@SuppressWarnings("unchecked")
	protected Map<String, XDMModule> loadEntities() {
		Collection<XDMModule> modules = (Collection<XDMModule>) cfg.getEntities(XDMModule.class); 
		Map<String, XDMModule> result = new HashMap<String, XDMModule>(modules.size());
		for (XDMModule module: modules) {
			result.put(module.getName(), module);
	    }
		return result;
	}

	@Override
	protected void storeEntities(Map<String, XDMModule> entities) {
		cfg.setEntities(XDMModule.class, entities.values());
	}

	// TODO: add relative path handling
	
	private void deleteModule(XDMModule module) {
    	Path path = Paths.get(module.getFileName());
		try {
			Files.deleteIfExists(path);
		} catch (IOException ex) {
			logger.error("deleteModule.error", ex);
		}
	}
	
	public static XDMModule loadModule(XDMModule module) throws IOException {
		if (module == null) {
			return null;
		}
		
		String body = readTextFile(module.getFileName());
		module.setBody(body);
		return module;
	}

	private void storeModule(XDMModule module) {
		try {
			writeTextFile(module.getFileName(), module.getBody());
		} catch (IOException ex) {
			logger.warn("storeModule.error", ex.getMessage());
		}
	}
	
	@Override
	public XDMModule load(String key) {
		XDMModule module = super.load(key);
		try {
			return loadModule(module);
		} catch (IOException ex) {
			logger.warn("load.error: {}; at key: {}", ex.getMessage(), key);
		}
		return null;
	}

	@Override
	public Map<String, XDMModule> loadAll(Collection<String> keys) {
		Map<String, XDMModule> result = super.loadAll(keys);
		for (XDMModule module: result.values()) {
			try {
				loadModule(module);
			} catch (IOException ex) {
				logger.warn("loadAll.error: {}; at module: {}", ex.getMessage(), module);
			}
		}
		return result;
	}

	@Override
	public void store(String key, XDMModule value) {
		super.store(key, value);
		storeModule(value);
	}
	
	@Override
	public void storeAll(Map<String, XDMModule> map) {
		super.storeAll(map);
		for (XDMModule module: map.values()) {
			storeModule(module);
		}
	}
	
	@Override
	public void delete(String key) {
		XDMModule module = entities.get(key);
		super.delete(key);
		if (module != null && !entities.containsKey(key)) {
			deleteModule(module);
		}
	}
	
	@Override
	public void deleteAll(Collection<String> keys) {
		List<XDMModule> modules = new ArrayList<>(keys.size());
		for (String key: keys) {
			XDMModule module = entities.get(key);
			if (module != null) {
				modules.add(module);
			}
		}
		super.deleteAll(keys);
		for (XDMModule module: modules) {
			if (!entities.containsKey(module.getName())) {
				deleteModule(module);
			}
		}
	}

}