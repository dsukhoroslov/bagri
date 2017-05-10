package com.bagri.core.system;

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
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"name"
})
public class Parameter extends DataType {

	@XmlAttribute(required = true)
	private String name;

	/**
	 * default constructor
	 */
	public Parameter() {
		// for JAXB serialization
	}
	
	/**
	 * 
	 * @param name the parameter name
	 * @param type the parameter XML type
	 * @param cardinality the parameter cardinality
	 */
	public Parameter(String name, String type, Cardinality cardinality) {
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

	@Override
	public String toString() {
		return "Parameter [name=" + name + ", type=" + getType() + ", cardinality=" + getCardinality() + "]";
	}
	
	
	
}
