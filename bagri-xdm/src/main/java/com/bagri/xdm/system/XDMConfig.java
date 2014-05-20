package com.bagri.xdm.system;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "config", namespace = "http://www.bagri.com/xdm/system" //,
//	propOrder = {
//		"nodes", 
//		"schemas"
//}
)
public class XDMConfig {
	
	@XmlElement
	private List<XDMNode> nodes;

	@XmlElement
	private List<XDMSchema> schemas;
	
}
