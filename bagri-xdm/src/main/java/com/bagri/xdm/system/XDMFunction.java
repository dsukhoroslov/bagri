package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name", 
		"description",
		"parameters"
})
public class XDMFunction {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	false)
	private String description;
		
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	private List<XDMParameter> parameters = new ArrayList<XDMParameter>();

	public XDMFunction() {
		// for JAXB
		super();
	}
	
	public XDMFunction(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescriptioin() {
		return description;
	}
	
	public List<XDMParameter> getParameters() {
		return parameters;
	}
	
}
