/**
 * 
 */
package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Denis Sukhoroslov
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/access", propOrder = {
		"resource",
		"perms"
})
public class XDMPermission {

	@XmlType(name = "Permission", namespace = "http://www.bagri.com/xdm/access")
	@XmlEnum
	public enum Permission {

	    @XmlEnumValue("read")
		read,

	    @XmlEnumValue("modify")
		modify,
		
	    @XmlEnumValue("execute")
		execute
	}
	
	@XmlAttribute(required = true)
	private String resource;
	  
	@XmlValue
	@XmlList
	private Set<Permission> perms = new HashSet<Permission>();
	
	public XDMPermission() {
		// for JAXB serialization
	}
	
	public XDMPermission(String resource, Permission... permissions) {
		this.resource = resource;
		for (Permission p: permissions) {
			addPermission(p);
		}
	}

	public XDMPermission(String resource, Set<Permission> permissions) {
		this.resource = resource;
		setPermissions(permissions);
	}

	public String getResource() {
		return resource;
	}
	
	public Set<Permission> getPermissions() {
		return perms;
	}
	
	public String getPermissionsAsString() {
		StringBuffer buff = new StringBuffer();
		for (Permission p: perms) {
			buff.append(p.name());
			buff.append(" ");
		}
		buff.deleteCharAt(buff.length() - 1);
		return buff.toString();
	}
	
	public String[] getPermissionsAsArray() {
		String[] pNames = new String[perms.size()];
		int i = 0;
		for (Permission p: perms) {
			pNames[i++] = p.name();
		}
		return pNames;
	}
	
	public boolean isEmpty() {
		return perms.isEmpty();
	}
	
	public boolean isWildcard() {
		return resource.indexOf("*") >= 0;
	}
	
	public void setPermissions(Set<Permission> permissions) {
		this.perms.clear();
		if (permissions != null) {
			perms.addAll(permissions);
		}
	}
	
	public boolean addPermission(Permission permission) {
		return perms.add(permission);
	}

	public boolean addPermissions(Collection<Permission> permissions) {
		return perms.addAll(permissions);
	}
	
	public boolean removePermission(Permission permission) {
		return perms.remove(permission);
	}
	
	public boolean hasPermission(Permission permission) {
		return perms.contains(permission);
	}
	
	@Override
	public String toString() {
		return "XDMPermission [" + perms + "]";
	}

}


