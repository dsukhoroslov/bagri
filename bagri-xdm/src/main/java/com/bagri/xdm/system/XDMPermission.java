/**
 * 
 */
package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Denis Sukhoroslov
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/access", propOrder = {
		"perm"
})
public class XDMPermission {

	@XmlType(name = "Permission", namespace = "http://www.bagri.com/xdm/access")
	@XmlEnum
	public enum Permission {

	    @XmlEnumValue("readonly")
		readonly,

	    @XmlEnumValue("readwrite")
		readwrite,
		
	    @XmlEnumValue("execute")
		execute
	}
	
	@XmlAttribute(name = "name", required = true)
	private Permission perm;
	
	@XmlValue
	private String resource;
	
	public XDMPermission() {
		// for JAXB serialization
	}
	
	public XDMPermission(Permission perm, String resource) {
		this.perm = perm;
		this.resource = resource;
	}
	
	public Permission getPermission() {
		return perm;
	}
	
	public String getResource() {
		return resource;
	}

}


