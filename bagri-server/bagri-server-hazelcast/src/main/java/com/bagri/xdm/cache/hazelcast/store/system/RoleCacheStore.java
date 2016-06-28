package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.Role;
import com.hazelcast.core.MapStore;

public class RoleCacheStore extends ConfigCacheStore<String, Role> implements MapStore<String, Role> { 

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Role> loadEntities() {
		Map<String, Role> roles = new HashMap<String, Role>();
		Collection<Role> cRoles = (Collection<Role>) cfg.getEntities(Role.class); 
		for (Role role: cRoles) {
			roles.put(role.getName(), role);
	    }
		return roles;
	}

	@Override
	protected void storeEntities(Map<String, Role> entities) {
		cfg.setEntities(Role.class, entities.values());
	}


}
