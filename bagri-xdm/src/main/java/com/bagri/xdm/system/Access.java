package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents Bagri access configuration file
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/access", propOrder = {
		"roles", 
		"users"
})
@XmlRootElement(name = "access", namespace = "http://www.bagridb.com/xdm/access") 
public class Access {

	@XmlElement(name="role")
	@XmlElementWrapper(name="roles")
	private List<Role> roles = new ArrayList<Role>();

	@XmlElement(name="user")
	@XmlElementWrapper(name="users")
	private List<User> users = new ArrayList<User>();
	
	/**
	 * 
	 * @return access roles
	 */
	public List<Role> getRoles() {
		return roles;
	}
	
	/**
	 * 
	 * @return access users
	 */
	public List<User> getUsers() {
		return users;
	}
	
}
