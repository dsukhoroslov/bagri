package com.bagri.core.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a collection of schema permissions 
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/access", propOrder = {
		"perms"
})
public class Permissions {

	  @XmlElement(name = "permission")
	  private List<Permission> perms = new ArrayList<>();

	  List<Permission> permissions() {
		  return perms;
	  }
	 
	  void addPermission(Permission perm) {
		  perms.add(perm);
	  }

}