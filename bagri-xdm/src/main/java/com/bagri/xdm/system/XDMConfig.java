package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system",	propOrder = {
		"nodes", 
		"schemas",
		"modules"
})
@XmlRootElement(name = "config", namespace = "http://www.bagri.com/xdm/system") 
public class XDMConfig {
	
	@XmlElement(name="node")
	@XmlElementWrapper(name="nodes")
	private List<XDMNode> nodes = new ArrayList<XDMNode>();

	@XmlElement(name="schema")
	@XmlElementWrapper(name="schemas")
	private List<XDMSchema> schemas = new ArrayList<XDMSchema>();
	
	@XmlElement(name="module")
	@XmlElementWrapper(name="modules")
	private List<XDMModule> modules = new ArrayList<XDMModule>();

	public List<XDMNode> getNodes() {
		return nodes;
	}
	
	public List<XDMSchema> getSchemas() {
		return schemas;
	}
	
	public List<XDMModule> getModules() {
		return modules;
	}
}
