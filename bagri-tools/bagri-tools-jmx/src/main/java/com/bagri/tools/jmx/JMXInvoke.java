package com.bagri.tools.jmx;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"mbean", 
		"method",
		"args",
		"onFailure",
		"onSuccess"
})
public class JMXInvoke {

	@XmlElement(required = 	true)
	private String mbean;

	@XmlElement(required = true)
	private String method;
		
	@XmlElement(name="argument")
	@XmlElementWrapper(name="arguments")
	private List<JMXArgument> args = new ArrayList<JMXArgument>();
	
	@XmlElement(required = false)
	private String onFailure;

	@XmlElement(required = false)
	private String onSuccess;
	
	public JMXInvoke() {
		//
	}
	
	public JMXInvoke(String mbean, String method, String onFailure, String onSuccess) {
		this.mbean = mbean;
		this.method = method;
		this.onFailure = onFailure;
		this.onSuccess = onSuccess;
	}
	
	public String getMBean() {
		return mbean;
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getOnFailure() {
		return onFailure;
	}
	
	public String getOnSuccess() {
		return onSuccess;
	}
	
	public List<JMXArgument> getArguments() {
		return args;
	}
	
	public void addArgument(String type, String value) {
		args.add(new JMXArgument(args.size(), type, value));
	}
}