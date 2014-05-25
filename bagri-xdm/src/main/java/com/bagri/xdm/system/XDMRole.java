package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

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
	
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	private List<XDMPermission> permissions = new ArrayList<XDMPermission>(); 
	
	@XmlList
	@XmlIDREF
	private List<XDMRole> includedRoles = new ArrayList<XDMRole>();
	
	public XDMRole() {
		super();
	}
	
	public XDMRole(int version, Date createdAt, String createdBy, String name, String description,  
			List<XDMPermission> permissions, List<XDMRole> includedRoles) {
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
	
	public List<XDMPermission> getPermissions() {
		return permissions;
	}
	
	public List<XDMRole> getIncludedRoles() {
		return includedRoles;
	}

	public void setPermissions(List<XDMPermission> permissions) {
		this.permissions.clear();
		if (permissions != null) {
			this.permissions.addAll(permissions);
		}
	}
	
	public void setIncludedRoles(List<XDMRole> includedRoles) {
		this.includedRoles.clear();
		if (includedRoles != null) {
			this.includedRoles.addAll(includedRoles);
		}
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
		if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "XDMRole [name=" + name + ", description=" + description
				+ ", permissions=" + permissions + ", includedRoles="
				+ includedRoles + "]";
	}
	
	
}
