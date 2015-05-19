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
		"className",
		"method",
		"resultClass",
		"description",
		"parameters"
})
public class XDMFunction {

	@XmlElement(required = true)
	private String className;

	@XmlElement(required = true)
	private String method;

	@XmlElement(required = true)
	private String resultClass;
	
	@XmlElement(required = false)
	private String description;
		
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	private List<XDMParameter> parameters = new ArrayList<XDMParameter>();

	public XDMFunction() {
		// for JAXB
		super();
	}
	
	public XDMFunction(String className, String method, String resultClass, String description) {
		this.className = className;
		this.method = method;
		this.resultClass = resultClass;
		this.description = description;
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethod() {
		return method;
	}
	
	public String getResultClass() {
		return resultClass;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<XDMParameter> getParameters() {
		return parameters;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(className).append(".");
		buff.append(method).append("(");
		int cnt = 0;
		for (XDMParameter xp: parameters) {
			if (cnt > 0) {
				buff.append(", ");
			}
			buff.append(xp.getType()).append(" ").append(xp.getName());
		}
		buff.append("): ").append(resultClass).append(";");
		return buff.toString();
	}
	
}
