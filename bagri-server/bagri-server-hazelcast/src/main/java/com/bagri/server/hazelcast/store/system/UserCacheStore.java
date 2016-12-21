package com.bagri.server.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.system.User;
import com.hazelcast.core.MapStore;

public class UserCacheStore extends ConfigCacheStore<String, User> implements MapStore<String, User> { 

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, User> loadEntities() {
		Map<String, User> users = new HashMap<String, User>();
		Collection<User> cUsers = (Collection<User>) cfg.getEntities(User.class); 
		for (User user: cUsers) {
			users.put(user.getLogin(), user);
	    }
		return users;
	}

	@Override
	protected void storeEntities(Map<String, User> entities) {
		cfg.setEntities(User.class, entities.values());
	}

}
