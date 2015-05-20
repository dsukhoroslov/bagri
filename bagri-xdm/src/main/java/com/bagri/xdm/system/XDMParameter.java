package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name"
})
public class XDMParameter extends XDMType {

	@XmlAttribute(required = true)
	private String name;

	public XDMParameter() {
		// for JAXB serialization
	}
	
	public XDMParameter(String name, String type, XDMCardinality cardinality) {
		super(type, cardinality);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
