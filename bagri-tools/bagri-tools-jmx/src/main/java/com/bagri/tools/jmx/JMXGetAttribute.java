package com.bagri.tools.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"mbean", 
		"attribute",
		"onFailure",
		"onSuccess"
})
public class JMXGetAttribute {

	@XmlElement(required = 	true)
	private String mbean;

	@XmlElement(required = true)
	private String attribute;

	@XmlElement(required = false)
	private String onFailure;

	@XmlElement(required = false)
	private String onSuccess;
	
	public JMXGetAttribute() {
		//
	}
	
	public JMXGetAttribute(String mbean, String attribute, String onFailure, String onSuccess) {
		this.mbean = mbean;
		this.attribute = attribute;
		this.onFailure = onFailure;
		this.onSuccess = onSuccess;
	}
	
	public String getMBean() {
		return mbean;
	}
	
	public String getAttribute() {
		return attribute;
	}
	
	public String getOnFailure() {
		return onFailure;
	}
	
	public String getOnSuccess() {
		return onSuccess;
	}
	
}
