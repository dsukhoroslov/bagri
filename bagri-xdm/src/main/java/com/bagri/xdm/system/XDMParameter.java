package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents extension function parameter
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name"
})
public class XDMParameter extends XDMType {

	@XmlAttribute(required = true)
	private String name;

	/**
	 * default constructor
	 */
	public XDMParameter() {
		// for JAXB serialization
	}
	
	/**
	 * 
	 * @param name the parameter name
	 * @param type the parameter XML type
	 * @param cardinality the parameter cardinality
	 */
	public XDMParameter(String name, String type, XDMCardinality cardinality) {
		super(type, cardinality);
		this.name = name;
	}

	/**
	 * 
	 * @return the parameter name
	 */
	public String getName() {
		return name;
	}
	
}
