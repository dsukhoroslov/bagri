package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMRole;
import com.hazelcast.core.MapStore;

public class RoleCacheStore extends ConfigCacheStore<String, XDMRole> implements MapStore<String, XDMRole> { 

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMRole> loadEntities() {
		Map<String, XDMRole> roles = new HashMap<String, XDMRole>();
		Collection<XDMRole> cRoles = (Collection<XDMRole>) cfg.getEntities(XDMRole.class); 
		for (XDMRole role: cRoles) {
			roles.put(role.getName(), role);
	    }
		return roles;
	}

	@Override
	protected void storeEntities(Map<String, XDMRole> entities) {
		cfg.setEntities(XDMRole.class, entities.values());
	}


}
