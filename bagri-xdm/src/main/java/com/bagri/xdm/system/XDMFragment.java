package com.bagri.xdm.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.common.XDMEntity;


/**
 * Represents some (periodic) part of XDM document instance
 *  
 * @author Denis Sukhoroslov
 * @since 09.2015 
 * @version 0.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"docType", 
		"path",
		"description",
		"enabled"
})
public class XDMFragment extends XDMEntity {
	
	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = true)
	private String docType;
	
	@XmlElement(required = true)
	private String path;

	@XmlElement(required = false)
	private String description;
		
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	public XDMFragment() {
		// for JAXB
		super();
	}
	
	public XDMFragment(int version, Date createdAt, String createdBy, String name, 
			String docType, String path, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.docType = docType;
		this.path = path;
		this.description = description;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public String getDocumentType() {
		return docType;
	}

	public String getPath() {
		return path;
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
		return name.hashCode();
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
		XDMFragment other = (XDMFragment) obj;
		return name.equals(other.name); 
	}

	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("document type", docType);
		result.put("path", path);
		result.put("description", description);
		result.put("enabled", enabled);
		return result;
	}

	@Override
	public String toString() {
		return "XDMFragment [name=" + name + ", version=" + getVersion()
				+ ", docType=" + docType + ", path=" + path 
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + ", enabled=" + enabled + "]";
	}
	
}