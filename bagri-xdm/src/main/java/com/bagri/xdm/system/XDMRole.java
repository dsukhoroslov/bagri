package com.bagri.xdm.system;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
//import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bagri.xdm.api.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/access", propOrder = {
		"name", 
		"description", 
		"permissions",
		"includedRoles"
})
public class XDMRole extends XDMEntity {

	@XmlAttribute
	@XmlID
	private String name;
	
	@XmlElement(required = true)
	private String description;
	
	@XmlElement(name = "permissions")
	@XmlJavaTypeAdapter(XDMPermissionsAdapter.class)
	private Map<String, XDMPermission> permissions = new HashMap<String, XDMPermission>(); 
	
	@XmlList
	//@XmlIDREF
	private Set<String> includedRoles = new HashSet<String>();
	
	public XDMRole() {
		super();
	}
	
	public XDMRole(int version, Date createdAt, String createdBy, String name, String description,  
			Map<String, XDMPermission> permissions, Set<String> includedRoles) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		setPermissions(permissions);
		setIncludedRoles(includedRoles);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
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
	
	//public Set<XDMPermission.Permission> getResourcePermissions(String resource) {
	//	Set<XDMPermission.Permission> perms = new HashSet<XDMPermission.Permission>();
	//	for (XDMPermission p: permissions) {
	//		if (resource.equals(p.getResource())) {
	//			perms.addAll(p.getPermissions());
	//		}
	//	}
	//	return perms;
	//}
	
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		XDMRole other = (XDMRole) obj;
		return name.equals(other.name);
	}

	@Override
	public String toString() {
		return "XDMRole [name=" + name + ", description=" + description
				+ ", permissions=" + permissions + ", includedRoles="
				+ includedRoles + "]";
	}
	
	
}
