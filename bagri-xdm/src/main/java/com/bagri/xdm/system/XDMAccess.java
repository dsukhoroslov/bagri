package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/access",	propOrder = {
		"roles", 
		"users"
})
@XmlRootElement(name = "access", namespace = "http://www.bagridb.com/xdm/access") 
public class XDMAccess {

	@XmlElement(name="role")
	@XmlElementWrapper(name="roles")
	private List<XDMRole> roles = new ArrayList<XDMRole>();

	@XmlElement(name="user")
	@XmlElementWrapper(name="users")
	private List<XDMUser> users = new ArrayList<XDMUser>();
	
	public List<XDMRole> getRoles() {
		return roles;
	}
	
	public List<XDMUser> getUsers() {
		return users;
	}
	
}
