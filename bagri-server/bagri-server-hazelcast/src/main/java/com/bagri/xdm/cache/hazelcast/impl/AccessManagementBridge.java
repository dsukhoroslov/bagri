package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_access_filename;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.security.Encryptor;
import com.bagri.xdm.cache.hazelcast.management.AccessManagement;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.bagri.xdm.system.XDMPermissionAware;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

public class AccessManagementBridge { //implements HazelcastInstanceAware {

	private static final transient Logger logger = LoggerFactory.getLogger(AccessManagementBridge.class);
	
	private Map<String, XDMRole> roles = new HashMap<>();
	private Map<String, XDMUser> users = new HashMap<>();

	//@Override
	public void setHazelcastInstance(HazelcastInstance hzInstance) {
		logger.trace("setHazelcastInstance.enter");
		if (hzInstance != null) {
			IMap<String, XDMRole> rCache = hzInstance.getMap("roles");
			setRoles(rCache);
			rCache.addEntryListener(new EntityListener(roles), true);
			IMap<String, XDMUser> uCache = hzInstance.getMap("users");
			setUsers(uCache);
			uCache.addEntryListener(new EntityListener(users), true);
		} 
		if (roles.size() == 0 && users.size() == 0) {
			// started as standalone server
	       	String confName = System.getProperty(xdm_access_filename);
	       	if (confName != null) {
	       		AccessManagement cfg = new AccessManagement(confName);
	       		Collection<XDMRole> rCache = (Collection<XDMRole>) cfg.getEntities(XDMRole.class); 
	       		for (XDMRole role: rCache) {
	       			roles.put(role.getName(), role);
	       	    }
	       		Collection<XDMUser> uCache = (Collection<XDMUser>) cfg.getEntities(XDMUser.class); 
	       		for (XDMUser user: uCache) {
	       			users.put(user.getLogin(), user);
	       	    }
	       	}
		}
		logger.trace("setHazelcastInstance.exit; initiated roles: {}; users {}", roles.size(), users.size());
	}
	
	private void setRoles(Map<String, XDMRole> roles) {
		this.roles.clear();
		if (roles != null) {
			this.roles.putAll(roles);
		}
	}
	
	private void setUsers(Map<String, XDMUser> users) {
		this.users.clear();
		if (users != null) {
			this.users.putAll(users);
		}
	}
	
	public Boolean authenticate(String schemaname, String username, String password) {
		logger.trace("authenticate.enter; user: {}, password: {}", username, password == null ? null : "*****");
		Boolean result = null;
		// check username/password against access DB
		XDMUser user = users.get(username);
		if (user != null) {
			String pwd = Encryptor.encrypt(password);
			if (pwd.equals(user.getPassword())) {
				Boolean granted = checkSchemaAccess(user, schemaname);
				if (granted != null) {
					result = granted;
				}
			} else {
				result = false;
			}
		}
		// throw NotFound exception?
		logger.trace("authenticate.exit; returning: {}", result);
		return result;
	}
	
	private Boolean checkSchemaAccess(XDMPermissionAware test, String schemaName) {
		String schema = "com.bagri.xdm:name=" + schemaName + ",type=Schema";
		XDMPermission perm = test.getPermissions().get(schema);
		if (perm != null) {
			return perm.hasPermission(Permission.read);
		}
		schema = "com.bagri.xdm:name=*,type=Schema";
		perm = test.getPermissions().get(schema);
		if (perm != null) {
			return perm.hasPermission(Permission.read);
		}
		
		for (String role: test.getIncludedRoles()) {
			XDMRole xdmr = roles.get(role);
			if (xdmr != null) {
				Boolean check = checkSchemaAccess(xdmr, schemaName);
				if (check != null) {
					return check;
				}
			}
		}
		return null;
	}

	private static class EntityListener implements 	EntryAddedListener<String, XDMPermissionAware>, 
		EntryUpdatedListener<String, XDMPermissionAware>, EntryRemovedListener<String, XDMPermissionAware> { 

		private final Map<String, XDMPermissionAware> cache;
		
		private EntityListener(Map cache) {
			this.cache = cache;
		}
	
		@Override
		public void entryAdded(EntryEvent<String, XDMPermissionAware> event) {
			cache.put(event.getKey(), event.getValue());
			logger.trace("entryAdded; entry: {}", event.getKey());
		}
		
		@Override
		public void entryUpdated(EntryEvent<String, XDMPermissionAware> event) {
			cache.put(event.getKey(), event.getValue());
			logger.trace("entryUpdated; entry: {}", event.getKey());
		}
	
		@Override
		public void entryRemoved(EntryEvent<String, XDMPermissionAware> event) {
			cache.remove(event.getKey());
			logger.trace("entryRemoved; entry: {}", event.getKey());
		}
	
	}

	
}
