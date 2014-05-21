package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system",
	propOrder = {
		"nodes", 
		"schemas"
})
@XmlRootElement(name = "config", namespace = "http://www.bagri.com/xdm/system") 
public class XDMConfig {
	
	@XmlElement
	private List<XDMNode> nodes = new ArrayList<XDMNode>();

	@XmlElement
	private List<XDMSchema> schemas = new ArrayList<XDMSchema>();
	
	public List<XDMNode> getNodes() {
		return nodes;
	}
	
	public List<XDMSchema> getSchemas() {
		return schemas;
	}
	
}
