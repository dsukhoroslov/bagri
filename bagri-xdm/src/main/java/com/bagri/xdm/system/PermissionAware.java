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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents basic entity which can be granted some direct permissions and indirect roles.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/access", propOrder = {
		"permissions",
		"includedRoles"
})
@XmlSeeAlso({
    Role.class,
    User.class
})
public abstract class PermissionAware extends Entity {

	@XmlElement(name = "permissions")
	@XmlJavaTypeAdapter(PermissionsAdapter.class)
	private Map<String, Permission> permissions = new HashMap<String, Permission>(); 
	
	@XmlElement(name = "includedRoles")
	@XmlList
	private Set<String> includedRoles = new HashSet<String>();
	
	/**
	 * default constructor
	 */
	public PermissionAware() {
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param permissions the map of direct permissions granted to user
	 * @param includedRoles the collection of roles granted to user
	 */
	public PermissionAware(int version, Date createdAt, String createdBy, Map<String, Permission> permissions, Set<String> includedRoles) {
		super(version, createdAt, createdBy);
		setPermissions(permissions);
		setIncludedRoles(includedRoles);
	}

	/**
	 * 
	 * @return the map of direct permissions granted to the entity
	 */
	public Map<String, Permission> getPermissions() {
		return permissions;
	}
	
	/**
	 * 
	 * @return the full map permissions granted to entity directly or via roles
	 */
	public Map<String, Object> getFlatPermissions() {
		Map<String, Object> perms = new HashMap<String, Object>(permissions.size());
		for (Map.Entry<String, Permission> e: permissions.entrySet()) {
			perms.put(e.getKey(), e.getValue().getPermissionsAsString());
		}
		return perms;
	}
	
	/**
	 * 
	 * @return the collection of roles granted to the entity
	 */
	public Set<String> getIncludedRoles() {
		return includedRoles;
	}
	
	/**
	 * 
	 * @param permissions the map of direct permissions granted to the entity
	 */
	public void setPermissions(Map<String, Permission> permissions) {
		this.permissions.clear();
		if (permissions != null) {
			this.permissions.putAll(permissions);
		}
	}
	
	/**
	 * 
	 * @param includedRoles the collection of roles granted to the entity
	 */
	public void setIncludedRoles(Set<String> includedRoles) {
		this.includedRoles.clear();
		if (includedRoles != null) {
			this.includedRoles.addAll(includedRoles);
		}
	}
	
	/**
	 * 
	 * @param role the role to add into the entity roles collection 
	 * @return true if the role has been added, false otherwise
	 */
	public boolean addIncludedRole(String role) {
		return includedRoles.add(role);
	}
	
	/**
	 * 
	 * @param role the role to remove from the entity roles collection
	 * @return true if the role has been removed, false otherwise
	 */
	public boolean removeIncludedRole(String role) {
		return includedRoles.remove(role);
	}

	/**
	 * 
	 * @param resource the resource to grant permission on
	 * @param permission the permission to add into the entity permissions map
	 * @return true if the permission has been added, false otherwise
	 */
	public boolean addPermission(String resource, Permission.Value permission) {
		Permission perm = permissions.get(resource);
		if (perm == null) {
			perm = new Permission(resource);
			permissions.put(resource, perm);
		}
		return perm.addPermission(permission);
	}
	
	/**
	 * 
	 * @param resource the resource to revoke permission from
	 * @param permission the permission to remove from the entity permissions map
	 * @return true if the permission has been removed, false otherwise
	 */
	public boolean removePermission(String resource, Permission.Value permission) {
		Permission perm = permissions.get(resource);
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("permissions", permissions.size());
		result.put("includedRoles", includedRoles.size());
		return result;
	}
	
}
