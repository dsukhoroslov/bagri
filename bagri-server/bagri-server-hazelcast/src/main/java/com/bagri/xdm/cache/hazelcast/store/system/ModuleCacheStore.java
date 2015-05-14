package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMModule;
import com.hazelcast.core.MapStore;

public class ModuleCacheStore extends ConfigCacheStore<String, XDMModule> implements MapStore<String, XDMModule> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMModule> loadEntities() {
		Map<String, XDMModule> modules = new HashMap<String, XDMModule>();
		//Collection<XDMModule> cModules = (Collection<XDMModule>) cfg.getEntities(XDMModule.class); 
		//for (XDMModule module: cModules) {
		//	modules.put(module.getName(), module);
	    //}
		return modules;
	}

	@Override
	protected void storeEntities(Map<String, XDMModule> entities) {
		cfg.setEntities(XDMModule.class, entities.values());
	}


}
