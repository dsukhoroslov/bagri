package com.bagri.core.system;

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
 * Represents user registered to access XDM Schema.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/access", propOrder = {
		"login", 
		"password", 
		"active"
})
public class User extends PermissionAware {
	
	@XmlAttribute
	@XmlID
	private String login;
	
	@XmlElement(required = true)
	private String password;
	
	@XmlAttribute(required = true)
	private boolean active;
	
	/**
	 * default constructor
	 */
	public User() {
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param permissions the map of direct permissions granted to user
	 * @param includedRoles the collection of roles granted to user
	 * @param login the user login
	 * @param password the user password
	 * @param active the user active flag
	 */
	public User(int version, Date createdAt, String createdBy, Map<String, Permission> permissions, Set<String> includedRoles,
			String login, String password, boolean active) {
		super(version, createdAt, createdBy, permissions, includedRoles);
		this.login = login;
		this.password = password;
		this.active = active;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * @return the active flag
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * @param active the active flag new value
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	/**
	 * @return the user login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + login.hashCode();
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
		User other = (User) obj;
		if (!login.equals(other.login)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", login);
		result.put("active", active);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "User [login=" + login + ", version=" + getVersion()  
				+ ", active=" + active + ", created at=" + getCreatedAt()
				+ ", by=" + getCreatedBy() + "]";
	}
	

}
