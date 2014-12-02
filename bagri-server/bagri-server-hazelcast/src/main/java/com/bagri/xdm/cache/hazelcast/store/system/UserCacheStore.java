package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.MapStore;

public class UserCacheStore extends ConfigCacheStore<String, XDMUser> implements MapStore<String, XDMUser> { 

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMUser> loadEntities() {
		Map<String, XDMUser> users = new HashMap<String, XDMUser>();
		Collection<XDMUser> cUsers = (Collection<XDMUser>) cfg.getEntities(XDMUser.class); 
		for (XDMUser user: cUsers) {
			users.put(user.getLogin(), user);
	    }
		return users;
	}

	@Override
	protected void storeEntities(Map<String, XDMUser> entities) {
		cfg.setEntities(XDMUser.class, entities.values());
	}

}
