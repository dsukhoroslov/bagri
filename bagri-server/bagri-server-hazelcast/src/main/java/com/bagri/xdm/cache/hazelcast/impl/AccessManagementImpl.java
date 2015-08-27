package com.bagri.xdm.cache.hazelcast.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.security.Encryptor;
import com.bagri.xdm.api.XDMAccessManagement;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.bagri.xdm.system.XDMPermissionAware;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class AccessManagementImpl implements XDMAccessManagement, InitializingBean {

	private static final transient Logger logger = LoggerFactory.getLogger(AccessManagementImpl.class);
	
	private String schemaName;
	private String schemaPass;
	
	private Map<String, XDMRole> roles = new HashMap<>();
	private Map<String, XDMUser> users = new HashMap<>();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		logger.trace("afterPropertiesSet.enter");
		HazelcastInstance dataInstance = Hazelcast.getHazelcastInstanceByName("hzInstance");
		if (dataInstance != null) {
			IMap<String, XDMRole> rCache = dataInstance.getMap("roles");
			setRoles(rCache);
			IMap<String, XDMUser> uCache = dataInstance.getMap("users");
			setUsers(uCache);
		}
		logger.trace("afterPropertiesSet.exit; initiated roles: {}; users {}", roles.size(), users.size());
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public String getSchemaPass() {
		return schemaPass;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public void setSchemaPass(String schemaPass) {
		this.schemaPass = schemaPass;
	}
	
	public void setRoles(Map<String, XDMRole> roles) {
		this.roles.clear();
		if (roles != null) {
			this.roles.putAll(roles);
		}
	}
	
	public void setUsers(Map<String, XDMUser> users) {
		this.users.clear();
		if (users != null) {
			this.users.putAll(users);
		}
	}
	
	@Override
	public boolean authenticate(String username, String password) {
		logger.trace("authenticate; user: {}, password: {}", username, password);
		// TODO: do we need this check any more?
		if (username.equals(schemaName) && password.equals(schemaPass)) {
			return true;
		}
		// check username/password against access DB
		XDMUser user = users.get(username);
		if (user != null) {
			String pwd = Encryptor.encrypt(password);
			if (pwd.equals(user.getPassword())) {
				Boolean granted = checkSchemaAccess(user);
				if (granted != null) {
					return granted;
				}
			}
		}
		// throw NotFound exception?
		return false;
	}
	
	private Boolean checkSchemaAccess(XDMPermissionAware test) {
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
				Boolean check = checkSchemaAccess(xdmr);
				if (check != null) {
					return check;
				}
			}
		}
		return null;
	}

}
