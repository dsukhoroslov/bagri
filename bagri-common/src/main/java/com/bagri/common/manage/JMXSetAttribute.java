package com.bagri.common.manage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"mbean", 
		"attribute",
		"value",
		"onFailure",
		"onSuccess"
})
public class JMXSetAttribute {

	@XmlElement(required = 	true)
	private String mbean;

	@XmlElement(required = true)
	private String attribute;

	@XmlElement(required = true)
	private String value;
	
	@XmlElement(required = false)
	private String onFailure;

	@XmlElement(required = false)
	private String onSuccess;
	
	public JMXSetAttribute() {
		//
	}
	
	public JMXSetAttribute(String mbean, String attribute, String value, String onFailure, String onSuccess) {
		this.mbean = mbean;
		this.attribute = attribute;
		this.value = value;
		this.onFailure = onFailure;
		this.onSuccess = onSuccess;
	}
	
	public String getMBean() {
		return mbean;
	}
	
	public String getAttribute() {
		return attribute;
	}
	
	public String getValue() {
		return value;
	}

	public String getOnFailure() {
		return onFailure;
	}
	
	public String getOnSuccess() {
		return onSuccess;
	}
	
}

