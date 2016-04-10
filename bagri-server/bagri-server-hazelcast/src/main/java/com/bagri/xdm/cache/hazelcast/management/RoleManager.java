package com.bagri.xdm.cache.hazelcast.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMRole;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@ManagedResource(description="Role Manager MBean")
public class RoleManager extends PermissionAwareManager<XDMRole> {

	public RoleManager() {
		super();
	}

	public RoleManager(HazelcastInstance hzInstance, String roleName) {
		super(hzInstance, roleName);
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

	@ManagedAttribute(description="Returns effective Role permissions, recursivelly")
	public CompositeData getRecursivePermissions() {
		Map<String, XDMPermission> xPerms = new HashMap<String, XDMPermission>();
		getRecursivePermissions(xPerms, entityName);
		Map<String, Object> pMap = new HashMap<String, Object>(xPerms.size());
		for (Map.Entry<String, XDMPermission> e: xPerms.entrySet()) {
			pMap.put(e.getKey(), e.getValue().getPermissionsAsString());
		}
		return JMXUtils.mapToComposite(entityName, "permissions", pMap);
	}
	
	@ManagedAttribute(description="Returns all Roles assigned to this one, recursivelly")
	public String[] getRecursiveRoles() {
		Set<String> xRoles = new HashSet<String>();
		getRecursiveRoles(xRoles, entityName);
		xRoles.remove(entityName);
		return xRoles.toArray(new String[xRoles.size()]);
	}

	@Override
	protected IMap<String, XDMRole> getRoleCache() {
		return entityCache;
	}
	
}
