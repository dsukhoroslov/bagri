package com.bagri.xdm.cache.hazelcast.management;

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

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.process.hazelcast.user.UserUpdater;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.IMap;

@ManagedResource(description="User Manager MBean")
public class UserManager extends PermissionAwareManager<XDMUser> {
	
	private IMap<String, XDMRole> roleCache;

	public UserManager() {
		super();
	}

	public UserManager(String userName) {
		super(userName);
	}
	
	public void setRoleCache(IMap<String, XDMRole> roleCache) {
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
		Map<String, XDMPermission> xPerms = getAllPermissions();
		Map<String, Object> pMap = new HashMap<String, Object>(xPerms.size());
		for (Map.Entry<String, XDMPermission> e: xPerms.entrySet()) {
			pMap.put(e.getKey(), e.getValue().getPermissionsAsString());
		}
		return JMXUtils.mapToComposite(entityName, "permissions", pMap);
	}
	
	public Map<String, XDMPermission> getAllPermissions() {
		Map<String, XDMPermission> xPerms = new HashMap<String, XDMPermission>();
		for (String role: getDirectRoles()) {
			getRecursivePermissions(xPerms, role);
		}
		return xPerms;
	}
	
	//@ManagedAttribute(description="Returns effective User permissions, recursivelly, resolving wildcards")
	public Map<String, XDMPermission> getFlatPermissions() {
		Map<String, XDMPermission> xPerms = getAllPermissions();
		List<XDMPermission> lPerms = new ArrayList<XDMPermission>(xPerms.values());
		for (XDMPermission lPerm: lPerms) {
			if (lPerm.isWildcard()) {
				xPerms.remove(lPerm.getResource());
				List<String> all = JMXUtils.queryNames(lPerm.getResource());
				for (String resource: all) {
					XDMPermission xPerm = new XDMPermission(resource, lPerm.getPermissions());
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
	protected IMap<String, XDMRole> getRoleCache() {
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
		XDMUser user = getEntity();
		if (user != null) {
	    	Object result = entityCache.executeOnKey(entityName, new UserUpdater(user.getVersion(), 
	    			JMXUtils.getCurrentUser(), oldPassword, newPassword));
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
