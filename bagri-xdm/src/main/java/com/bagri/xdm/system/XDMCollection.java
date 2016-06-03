package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"id",
		"name", 
		"docType", 
		"description",
		"enabled"
})
public class XDMCollection extends XDMEntity {
	
	@XmlAttribute(required = true)
	private int id;
		
	@XmlAttribute(required = true)
	@XmlID
	private String name;

	@XmlElement(required = false)
	private String docType;
	
	@XmlElement(required = false)
	private String description;
		
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	public XDMCollection() {
		// for JAXB
		super();
	}
	
	public XDMCollection(int version, Date createdAt, String createdBy, int id, String name, 
			String docType, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.id = id;
		this.name = name;
		this.docType = docType;
		this.description = description;
		this.enabled = enabled;
	}

	public int getId() {
		return id;
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
		return id;
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
		XDMCollection other = (XDMCollection) obj;
		return id == other.id; 
	}

	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("id", id);
		result.put("name", name);
		result.put("document type", docType == null ? "" : docType);
		result.put("description", description);
		result.put("enabled", enabled);
		return result;
	}

	@Override
	public String toString() {
		return "XDMCollection [id=" + id + ", name=" + name
				+ ", version=" + getVersion() + ", docType=" + docType
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + ", enabled=" + enabled + "]";
	}
	
}
