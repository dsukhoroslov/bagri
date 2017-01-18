package com.bagri.server.hazelcast.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.system.Permission;
import com.bagri.core.system.Role;
import com.bagri.core.system.User;
import com.bagri.server.hazelcast.task.user.UserUpdater;
import com.bagri.support.util.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@ManagedResource(description="User Manager MBean")
public class UserManager extends PermissionAwareManager<User> {
	
	private IMap<String, Role> roleCache;

	public UserManager() {
		super();
	}

	public UserManager(HazelcastInstance hzInstance, String userName) {
		super(hzInstance, userName);
	}
	
	public void setRoleCache(IMap<String, Role> roleCache) {
		this.roleCache = roleCache;
	}

	@ManagedAttribute(description="Returns User name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns User version")
	public int getVersion() {
		return super.getVersion();
	}

	@ManagedAttribute(description="Returns User state")
	public boolean isActive() {
		return getEntity().isActive();
	}

	@ManagedAttribute(description="Returns effective User permissions, recursivelly")
	public CompositeData getRecursivePermissions() {
		Map<String, Permission> xPerms = getAllPermissions();
		Map<String, Object> pMap = new HashMap<String, Object>(xPerms.size());
		for (Map.Entry<String, Permission> e: xPerms.entrySet()) {
			pMap.put(e.getKey(), e.getValue().getPermissionsAsString());
		}
		return JMXUtils.mapToComposite(entityName, "permissions", pMap);
	}
	
	public Map<String, Permission> getAllPermissions() {
		Map<String, Permission> xPerms = new HashMap<String, Permission>();
		for (String role: getDirectRoles()) {
			getRecursivePermissions(xPerms, role);
		}
		return xPerms;
	}
	
	//@ManagedAttribute(description="Returns effective User permissions, recursivelly, resolving wildcards")
	public Map<String, Permission> getFlatPermissions() {
		Map<String, Permission> xPerms = getAllPermissions();
		List<Permission> lPerms = new ArrayList<Permission>(xPerms.values());
		for (Permission lPerm: lPerms) {
			if (lPerm.isWildcard()) {
				xPerms.remove(lPerm.getResource());
				List<String> all = JMXUtils.queryNames(lPerm.getResource());
				for (String resource: all) {
					Permission xPerm = new Permission(resource, lPerm.getPermissions());
					xPerms.put(resource, xPerm);
				}
			}
		}
    	logger.trace("getFlatPermissions.exit; returning: {}", xPerms);
		return xPerms;
	}

	@ManagedAttribute(description="Returns all Roles assigned to this User, recursivelly")
	public String[] getRecursiveRoles() {
		Set<String> xRoles = new HashSet<String>();
		for (String role: getDirectRoles()) {
			getRecursiveRoles(xRoles, role);
		}
		return xRoles.toArray(new String[xRoles.size()]);
	}

	@Override
	protected IMap<String, Role> getRoleCache() {
		return roleCache;
	}
	
	@ManagedOperation(description="Activates/Deactivates User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login"),
		@ManagedOperationParameter(name = "activate", description = "Activate or Deactivate the User identifieb by login")})
	public boolean activateUser(String login, boolean activate) {
		// TODO Auto-generated method stub
		return false;
	}

	@ManagedOperation(description="Changes User password")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "oldPassword", description = "old User's password"),
		@ManagedOperationParameter(name = "newPassword", description = "new User's password")})
	public boolean changePassword(String oldPassword, String newPassword) {
		User user = getEntity();
		if (user != null) {
	    	Object result = entityCache.executeOnKey(entityName, new UserUpdater(user.getVersion(), 
	    			getCurrentUser(), oldPassword, newPassword));
	    	logger.trace("changePassword; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected String getEntityType() {
		return "User";
	}

}
