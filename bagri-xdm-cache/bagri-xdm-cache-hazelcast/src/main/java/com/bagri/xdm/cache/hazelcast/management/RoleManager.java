package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.process.hazelcast.role.PermissionUpdater;
import com.bagri.xdm.process.hazelcast.role.RoleUpdater;
import com.bagri.xdm.process.hazelcast.schema.SchemaUpdater;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMSchema;

@ManagedResource(description="Role Manager MBean")
public class RoleManager extends EntityManager<XDMRole> {

	public RoleManager() {
		super();
	}

	public RoleManager(String roleName) {
		super(roleName);
	}

	@Override
	protected String getEntityType() {
		return "Role";
	}

	@ManagedAttribute(description="Returns Role name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns Role version")
	public int getVersion() {
		return super.getVersion();
	}

	@ManagedAttribute(description="Returns registered Role permissions")
	public CompositeData getDirectPermissions() {
		Map<String, Object> pMap = getEntity().getFlatPermissions();
		return JMXUtils.propsToComposite(entityName, "permissions", pMap);
	}
	
	@ManagedAttribute(description="Returns included (nested) Roles")
	public String[] getDirectRoles() {
		Set<String> roles = getEntity().getIncludedRoles(); 
		return roles.toArray(new String[roles.size()]);
	}
	
	@ManagedAttribute(description="Returns effective Role permissions, recursivelly")
	public CompositeData getRecursivePermissions() {
		Map<String, XDMPermission> xPerms = new HashMap<String, XDMPermission>();
		getRecursivePermissions(xPerms, entityName);
		Map<String, Object> pMap = new HashMap<String, Object>(xPerms.size());
		for (Map.Entry<String, XDMPermission> e: xPerms.entrySet()) {
			pMap.put(e.getKey(), e.getValue().getPermissionsAsString());
		}
		return JMXUtils.propsToComposite(entityName, "permissions", pMap);
	}
	
	private void getRecursivePermissions(Map<String, XDMPermission> xPerms, String roleName) {
		XDMRole role = entityCache.get(roleName);
		if (role != null) {
			if (role.getIncludedRoles().size() > 0) {
				for (String name: role.getIncludedRoles()) {
					getRecursivePermissions(xPerms, name);
				}
			}
			
			Collection<XDMPermission> perms = role.getPermissions().values();
			if (perms.size() > 0) {
				for (XDMPermission perm: perms) {
					XDMPermission xPerm = xPerms.get(perm.getResource());
					if (xPerm == null) {
						xPerm = new XDMPermission(perm.getResource());
						xPerms.put(perm.getResource(), xPerm);
					}
					for (Permission p: perm.getPermissions()) {
						xPerm.addPermission(p);
					}
				}
			}
		}
	}

	@ManagedAttribute(description="Returns all Roles assigned to this one, recursivelly")
	public String[] getRecursiveRoles() {
		Set<String> all = getRecursiveRoles(entityName);
		all.remove(entityName);
		return all.toArray(new String[all.size()]);
	}
	
	private Set<String> getRecursiveRoles(String roleName) {
		XDMRole role = entityCache.get(roleName);
		if (role != null) {
			Set<String> roles = new HashSet<String>();
			if (role.getIncludedRoles().size() > 0) {
				for (String name: role.getIncludedRoles()) {
					roles.addAll(getRecursiveRoles(name));
				}
			}
			roles.add(roleName);
			return roles;
		}
		return Collections.emptySet();
	}
	
	@ManagedOperation(description="Returns access permission for the named Resource")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "resource", description = "A name of the Resource to return")})
	public String[] getResourcePermissions(String resource) {
		XDMPermission perm = getEntity().getPermissions().get(resource);
		if (perm != null) {
			return perm.getPermissionsAsArray();
		}
		return new String[0];
	}
	
	@ManagedOperation(description="Set access permission to the named Resource")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "resource", description = "A name of Resource to add Permissions"),
		@ManagedOperationParameter(name = "permissions", description = "Permisions to add separated by whitespace")})
	public void addPermissions(String resource, String permissions) {
		XDMRole role = updatePermissions(resource, permissions, RoleUpdater.Action.add);
	    logger.trace("addPermissions; execution result: {}", role);
	}
	
	@ManagedOperation(description="Removes access permission to the named Reource")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "resource", description = "A name of Resource to remove permissions from"),
		@ManagedOperationParameter(name = "permissions", description = "Permisions to remove separated by whitespace")})
	public void removePermissions(String resource, String permissions) {
		XDMRole role = updatePermissions(resource, permissions, RoleUpdater.Action.remove);
	    logger.trace("removePermissions; execution result: {}", role);
	}

	private XDMRole updatePermissions(String resource, String permissions, RoleUpdater.Action action) {
		String[] aPerms = permissions.split(" ");
		XDMRole role = getEntity();
	    Object result = entityCache.executeOnKey(entityName, new PermissionUpdater(role.getVersion(), 
	    		JMXUtils.getCurrentUser(), resource, aPerms, action));
		return (XDMRole) result;
	}
	
	@ManagedOperation(description="Adds included (nested) Roles")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "roles", description = "Name of the Roles to include separated by whitespace")})
	public void addIncludedRoles(String roles) {
		XDMRole role = updateIncludedRoles(roles, RoleUpdater.Action.add);
	    logger.trace("addIncludedRoles; execution result: {}", role);
	}
	
	@ManagedOperation(description="Removes included (nested) Roles")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "roles", description = "Name of the Roles to remove separated by whitespace")})
	public void removeIncludedRoles(String roles) {
		XDMRole role = updateIncludedRoles(roles, RoleUpdater.Action.remove);
	    logger.trace("removeIncludedRoles; execution result: {}", role);
	}

	private XDMRole updateIncludedRoles(String roles, RoleUpdater.Action action) {
		String[] aRoles = roles.split(" ");
		XDMRole role = getEntity();
	    Object result = entityCache.executeOnKey(entityName, new RoleUpdater(role.getVersion(), 
	    		JMXUtils.getCurrentUser(), aRoles, action));
		return (XDMRole) result;
	}
	
}
