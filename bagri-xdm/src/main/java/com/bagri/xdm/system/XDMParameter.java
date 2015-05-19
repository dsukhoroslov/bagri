package com.bagri.xdm.system;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.system.XDMPermission.Permission;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name",
		"type"
})
public class XDMParameter {

	@XmlAttribute(required = true)
	private String name;

	@XmlAttribute(required = true)
	private String type;
	
	public XDMParameter() {
		// for JAXB serialization
	}
	
	public XDMParameter(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
}
