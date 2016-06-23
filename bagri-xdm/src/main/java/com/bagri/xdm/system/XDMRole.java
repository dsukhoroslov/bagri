package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents role registered to access XDM Schema.
 * 
 * @author Denis Sukhoroslov
 *
 */
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
	
	/**
	 * default constructor
	 */
	public XDMRole() {
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param permissions the map of direct permissions granted to user
	 * @param includedRoles the collection of roles granted to user
	 * @param name the role name
	 * @param description the role description
	 */
	public XDMRole(int version, Date createdAt, String createdBy, Map<String, XDMPermission> permissions, Set<String> includedRoles,
			String name, String description) {
		super(version, createdAt, createdBy, permissions, includedRoles);
		this.name = name;
		this.description = description;
	}
	
	/**
	 * 
	 * @return the role name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the role description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("description", description);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMRole [name=" + name + ", version=" + this.getVersion()
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + ", permissions=" + getPermissions()
				+ ", includedRoles=" + getIncludedRoles() + "]";
	}
	
	
}
