package com.bagri.xdm.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"library", 
		"className",
		"docType",
		"scope",
		"enabled"
})
public class XDMTriggerDef extends XDMEntity {
	
	@XmlType(name = "Scope", namespace = "http://www.bagri.com/xdm/system")
	@XmlEnum
	public enum Scope {

	    @XmlEnumValue("before")
		before,

	    @XmlEnumValue("after")
		after
	}
	
	@XmlElement(required = true)
	private String library;
		
	@XmlElement(required = true)
	private String className;

	@XmlElement(required = false)
	private String docType;

	@XmlElement(required = true)
	private Scope scope;
	
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	public XDMTriggerDef() {
		// for JAXB
		super();
	}
	
	public XDMTriggerDef(int version, Date createdAt, String createdBy, String library, 
			String className, String docType, Scope scope, boolean enabled) {
		super(version, createdAt, createdBy);
		this.library = library;
		this.className = className;
		this.docType = docType;
		this.scope = scope;
		this.enabled = enabled;
	}

	public String getLibrary() {
		return library;
	}

	public String getClassName() {
		return className;
	}

	public String getDocType() {
		return docType;
	}

	public Scope getScope() {
		return scope;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			//this.updateVersion("???");
			return true;
		}
		return false;
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
		
		XDMTriggerDef other = (XDMTriggerDef) obj;
		if (!library.equals(other.library)) {
			return false;
		}
		if (!className.equals(other.className)) {
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("version", getVersion());
		result.put("created at", getCreatedAt().toString());
		result.put("created by", getCreatedBy());
		result.put("library", library);
		result.put("className", className);
		result.put("docType", docType); // can be null!
		result.put("scope", scope.name());
		result.put("enabled", enabled);
		return result;
	}

	@Override
	public String toString() {
		return "XDMTriggerDef [library=" + library + ", className=" + className
				+ ", docType=" + docType + ", scope=" + scope + ", enabled=" + enabled + "]";
	}

}
