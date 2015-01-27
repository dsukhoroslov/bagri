package com.bagri.xdm.system;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/access", propOrder = {
		"permissions",
		"includedRoles"
})
public abstract class XDMPermissionAware extends XDMEntity {

	@XmlElement(name = "permissions")
	@XmlJavaTypeAdapter(XDMPermissionsAdapter.class)
	private Map<String, XDMPermission> permissions = new HashMap<String, XDMPermission>(); 
	
	@XmlElement(name = "includedRoles")
	@XmlList
	private Set<String> includedRoles = new HashSet<String>();
	
	public XDMPermissionAware() {
		super();
	}
	
	public XDMPermissionAware(int version, Date createdAt, String createdBy, Map<String, XDMPermission> permissions, Set<String> includedRoles) {
		super(version, createdAt, createdBy);
		setPermissions(permissions);
		setIncludedRoles(includedRoles);
	}
	
	public Map<String, XDMPermission> getPermissions() {
		return permissions;
	}
	
	public Map<String, Object> getFlatPermissions() {
		Map<String, Object> perms = new HashMap<String, Object>(permissions.size());
		for (Map.Entry<String, XDMPermission> e: permissions.entrySet()) {
			perms.put(e.getKey(), e.getValue().getPermissionsAsString());
		}
		return perms;
	}
	
	public Set<String> getIncludedRoles() {
		return includedRoles;
	}
	
	public void setPermissions(Map<String, XDMPermission> permissions) {
		this.permissions.clear();
		if (permissions != null) {
			this.permissions.putAll(permissions);
		}
	}
	
	public void setIncludedRoles(Set<String> includedRoles) {
		this.includedRoles.clear();
		if (includedRoles != null) {
			this.includedRoles.addAll(includedRoles);
		}
	}
	
	public boolean addIncludedRole(String role) {
		return includedRoles.add(role);
	}
	
	public boolean removeIncludedRole(String role) {
		return includedRoles.remove(role);
	}

	public boolean addPermission(String resource, XDMPermission.Permission permission) {
		XDMPermission perm = permissions.get(resource);
		if (perm == null) {
			perm = new XDMPermission(resource);
			permissions.put(resource, perm);
		}
		return perm.addPermission(permission);
	}
	
	public boolean removePermission(String resource, XDMPermission.Permission permission) {
		XDMPermission perm = permissions.get(resource);
		if (perm != null) {
			if (permission == null) {
				permissions.remove(resource);
				return true;
			} else {
				boolean result = perm.removePermission(permission);
				if (result && perm.isEmpty()) {
					permissions.remove(resource);
				}
				return result;
			}
		}
		return false;
	}
	
}
