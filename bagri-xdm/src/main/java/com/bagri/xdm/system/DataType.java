package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents XML type and cardinality. User to specify function parameters.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"type",
		"cardinality"
})
@XmlSeeAlso({
    Parameter.class
})
public class DataType {

	@XmlAttribute(required = true)
	private String type;

	@XmlAttribute(required = true)
	private Cardinality cardinality;

	/**
	 * default constructor
	 */
	public DataType() {
		// for JAXB serialization
	}
	
	/**
	 * 
	 * @param type the XML type name
	 * @param cardinality the type cardinality
	 */
	public DataType(String type, Cardinality cardinality) {
		this.type = type;
		this.cardinality = cardinality;
	}

	/**
	 * 
	 * @return the XML type name
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * 
	 * @return the type cardinality
	 */
	public Cardinality getCardinality() {
		return cardinality;
	}
	
}
