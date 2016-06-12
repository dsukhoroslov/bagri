package com.bagri.xdm.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Represents configuration parameter
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name"
})
public class XDMEntry {

	@XmlAttribute(required = true)
	private String name;
	
	@XmlValue
	private String value;
	
	/**
	 * default constructor
	 */
	public XDMEntry() {
		// for JAXB serialization
	}
	
	/**
	 * 
	 * @param name the entry name
	 * @param value the entry value
	 */
	public XDMEntry(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * 
	 * @return the entry name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the entry value
	 */
	public String getValue() {
		return value;
	}
	
}
