package com.bagri.common.manage;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(namespace = "http://www.bagri.com/xdm/system")
@XmlRootElement(name = "script", namespace = "http://www.bagri.com/xdm/system") 
public class JMXScript {
	
	@XmlElements(value = { 
			@XmlElement(name="invoke", type=JMXInvoke.class),
            //@XmlElement(name="get-attribute", type=JMXGetAttribute.class),
            //@XmlElement(name="set-attribute", type=JMXSetAttribute.class),
            @XmlElement(name="delay", type=Integer.class)
    })	
	private List<Object> tasks = new ArrayList<>();

	public List<Object> getTasks() {
		return tasks;
	}

}