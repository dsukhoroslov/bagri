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

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/access", propOrder = {
		"name", 
		"description"
})
public class XDMRole extends XDMPermissionAware {

	@XmlAttribute
	@XmlID
	private String name;
	
	@XmlElement(required = true)
	private String description;
	
	public XDMRole() {
		super();
	}
	
	public XDMRole(int version, Date createdAt, String createdBy, Map<String, XDMPermission> permissions, Set<String> includedRoles,
			String name, String description) {
		super(version, createdAt, createdBy, permissions, includedRoles);
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
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
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("description", description);
		return result;
	}
	
	@Override
	public String toString() {
		return "XDMRole [name=" + name + ", version=" + this.getVersion()
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + ", permissions=" + getPermissions()
				+ ", includedRoles=" + getIncludedRoles() + "]";
	}
	
	
}
