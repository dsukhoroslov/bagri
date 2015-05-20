package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"type",
		"cardinality"
})
@XmlSeeAlso({
    XDMParameter.class
})
public class XDMType {

	@XmlAttribute(required = true)
	private String type;

	@XmlAttribute(required = true)
	private XDMCardinality cardinality;

	public XDMType() {
		// for JAXB serialization
	}
	
	public XDMType(String type, XDMCardinality cardinality) {
		this.type = type;
		this.cardinality = cardinality;
	}

	public String getType() {
		return type;
	}
	
	public XDMCardinality getCardinality() {
		return cardinality;
	}
	
}
