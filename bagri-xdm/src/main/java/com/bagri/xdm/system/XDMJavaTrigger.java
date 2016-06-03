package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="javatrigger", namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"library", 
		"className"
})
public class XDMJavaTrigger extends XDMTriggerDef {

	@XmlElement(required = true)
	private String library;
		
	@XmlElement(required = true)
	private String className;
	
	public XDMJavaTrigger() {
		// for JAXB de-serialization
		super();
	}

	public XDMJavaTrigger(int version, Date createdAt, String createdBy, String library, 
			String className, String docType, boolean synchronous, boolean enabled, int index) {
		super(version, createdAt, createdBy, docType, synchronous, enabled, index);
		this.library = library;
		this.className = className;
	}
	
	public String getName() {
		return className;
	}

	public String getLibrary() {
		return library;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + library.hashCode();
		result = prime * result	+ className.hashCode();
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
		
		XDMJavaTrigger other = (XDMJavaTrigger) obj;
		if (!library.equals(other.library)) {
			return false;
		}
		if (!className.equals(other.className)) {
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("library", library);
		result.put("className", className);
		return result;
	}

	@Override
	public String toString() {
		return "XDMJavaTrigger [library=" + library + ", className=" + className
				+ ", docType=" + getDocType() + ", enabled=" + isEnabled()
				+ ", actions=" + getActions().toString() + "]";
	}
	
}
