package com.bagri.tools.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"index", 
		"type",
		"value"
})
public class JMXArgument {

	@XmlAttribute(required = true)
	//@XmlID
	private int index;
	
	@XmlElement(required = true)
	private String type;

	@XmlElement(required = true)
	private String value;
		
	public JMXArgument() {
		//
	}
	
	public JMXArgument(int index, String type, String value) {
		this.index = index;
		this.type = type;
		this.value = value;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
}