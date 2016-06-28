package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.hasStorageMembers;
import static com.bagri.xdm.common.XDMConstants.xdm_access_filename;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.security.Encryptor;
import com.bagri.xdm.cache.hazelcast.config.AccessConfig;
import com.bagri.xdm.system.Permission;
import com.bagri.xdm.system.PermissionAware;
import com.bagri.xdm.system.Role;
import com.bagri.xdm.system.User;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

public class AccessManagementBridge implements MembershipListener {

	private static final transient Logger logger = LoggerFactory.getLogger(AccessManagementBridge.class);

	private HazelcastInstance hzInstance;
	private Map<String, Role> roles = new HashMap<>();
	private Map<String, User> users = new HashMap<>();

	public void setHazelcastInstance(HazelcastInstance hzInstance) {
		logger.trace("setHazelcastInstance.enter");
		this.hzInstance = hzInstance;
		hzInstance.getCluster().addMembershipListener(this);
		setupCaches();
	}
	
	@SuppressWarnings("unchecked")
	private void setupCaches() {
		boolean lite = !hasStorageMembers(hzInstance);
		if (lite) {
	       	String confName = System.getProperty(xdm_access_filename);
	       	if (confName != null) {
	       		// TODO: get it from Spring context?
	       		AccessConfig cfg = new AccessConfig(confName);
	       		Collection<Role> rCache = (Collection<Role>) cfg.getEntities(Role.class); 
	       		for (Role role: rCache) {
	       			roles.put(role.getName(), role);
	       	    }
	       		Collection<User> uCache = (Collection<User>) cfg.getEntities(User.class); 
	       		for (User user: uCache) {
	       			users.put(user.getLogin(), user);
	       	    }
	       	}
		} else {
			copyCache(hzInstance.getMap("roles"), roles);
			copyCache(hzInstance.getMap("users"), users);
		}
		logger.trace("setupCaches.exit; lite: {}; initiated roles: {}; users {}", lite, roles.size(), users.size());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void copyCache(IMap source, Map target) {
		target.clear();
		if (source != null) {
			target.putAll(source);
			source.addEntryListener(new EntityListener(target), true);
		}
	}
	
	public Boolean authenticate(String schemaname, String username, String password) {
		logger.trace("authenticate.enter; user: {}, password: {}", username, password);
		Boolean result = null;
		// check username/password against access DB
		User user = users.get(username);
		if (user != null) {
			boolean auth = password.equals(user.getPassword()); 
			if (!auth) {
				// try double-encrypted pwd
				String pwd = Encryptor.encrypt(user.getPassword());
				auth = password.equals(pwd);
			} 
			result = auth && checkSchemaPermission(user, schemaname, Permission.Value.read);
		}
		// throw NotFound exception?
		logger.trace("authenticate.exit; returning: {}", result);
		return result;
	}

	public Boolean hasPermission(String schemaname, String username, Permission.Value perm) {
		logger.trace("hasPermission.enter; schema: {}, user: {}, permission: {}", schemaname, username, perm);
		Boolean result = null;
		User user = users.get(username);
		if (user != null) {
			result = checkSchemaPermission(user, schemaname, perm);
		}
		// throw NotFound exception?
		logger.trace("hasPermission.exit; returning: {}", result);
		return result;
	}
	
	private Boolean checkSchemaPermission(PermissionAware test, String schemaName, Permission.Value check) {
		String schema = "com.bagri.xdm:name=" + schemaName + ",type=Schema";
		Permission perm = test.getPermissions().get(schema);
		if (perm != null && perm.hasPermission(check)) {
			return true;
		}
		schema = "com.bagri.xdm:name=*,type=Schema";
		perm = test.getPermissions().get(schema);
		if (perm != null && perm.hasPermission(check)) {
			return true;
		}
		
		for (String role: test.getIncludedRoles()) {
			Role xdmr = roles.get(role);
			if (xdmr != null) {
				Boolean result = checkSchemaPermission(xdmr, schemaName, check);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		setupCaches();
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		// no-op ?
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		// no-op
	}

	private static class EntityListener implements EntryAddedListener<String, PermissionAware>, 
		EntryUpdatedListener<String, PermissionAware>, EntryRemovedListener<String, PermissionAware> { 

		private final Map<String, PermissionAware> cache;
		
		private EntityListener(Map cache) {
			this.cache = cache;
		}
	
		@Override
		public void entryAdded(EntryEvent<String, PermissionAware> event) {
			cache.put(event.getKey(), event.getValue());
			logger.trace("entryAdded; entry: {}", event.getKey());
		}
		
		@Override
		public void entryUpdated(EntryEvent<String, PermissionAware> event) {
			cache.put(event.getKey(), event.getValue());
			logger.trace("entryUpdated; entry: {}", event.getKey());
		}
	
		@Override
		public void entryRemoved(EntryEvent<String, PermissionAware> event) {
			cache.remove(event.getKey());
			logger.trace("entryRemoved; entry: {}", event.getKey());
		}
	
	}

}
