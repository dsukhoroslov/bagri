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
		"result",
		"description",
		"prefix",
		"parameters"
})
public class XDMFunction {

	@XmlElement(required = true)
	private String className;

	@XmlElement(required = true)
	private String method;

	@XmlElement(required = true)
	private XDMType result;
	
	@XmlElement(required = false)
	private String description;

	@XmlElement(required = true)
	private String prefix;
	
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	private List<XDMParameter> parameters = new ArrayList<XDMParameter>();

	public XDMFunction() {
		// for JAXB
		super();
	}
	
	public XDMFunction(String className, String method, XDMType result, String description, String prefix) {
		this.className = className;
		this.method = method;
		this.result = result;
		this.description = description;
		this.prefix = prefix;
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethod() {
		return method;
	}
	
	public XDMType getResult() {
		return result;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public List<XDMParameter> getParameters() {
		return parameters;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer(prefix).append(":");
		buff.append(className).append(".");
		buff.append(method).append("(");
		int idx = 0;
		for (XDMParameter xp: parameters) {
			if (idx > 0) {
				buff.append(", ");
			}
			buff.append(xp.getType()).append(" ").append(xp.getName());
			idx++;
		}
		buff.append("): ").append(result.getType()).append(";");
		return buff.toString();
	}
	
}
