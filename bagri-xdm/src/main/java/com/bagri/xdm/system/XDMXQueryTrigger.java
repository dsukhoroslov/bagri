package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"module", 
		"function"
})
public class XDMXQueryTrigger extends XDMTriggerDef {

	@XmlElement(required = true)
	private String module;
		
	@XmlElement(required = true)
	private String function;

	public XDMXQueryTrigger(int version, Date createdAt, String createdBy, String module, 
			String function, String docType, boolean synchronous, boolean enabled) {
		super(version, createdAt, createdBy, docType, synchronous, enabled);
		this.module = module;
		this.function = function;
	}
	
	public String getName() {
		return module + ":" + function;
	}

	public String getModule() {
		return module;
	}

	public String getFunction() {
		return function;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + module.hashCode();
		result = prime * result	+ function.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		XDMXQueryTrigger other = (XDMXQueryTrigger) obj;
		if (!module.equals(other.module)) {
			return false;
		}
		if (!function.equals(other.function)) {
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = super.toMap();
		result.put("module", module);
		result.put("function", function);
		return result;
	}

	@Override
	public String toString() {
		return "XDMXQueryTrigger [module=" + module + ", function=" + function
				+ ", docType=" + getDocType() + ", enabled=" + isEnabled()
				+ ", actions=" + getActions().toString() + "]";
	}
	

}
