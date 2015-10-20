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
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name", 
		"docType", 
		"description",
		"enabled"
})
public class XDMCollection extends XDMEntity {

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
	
	public XDMCollection(int version, Date createdAt, String createdBy, String name, 
			String docType, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.docType = docType;
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
		XDMCollection other = (XDMCollection) obj;
		return name.equals(other.name); 
	}

	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("document type", docType);
		result.put("description", description);
		result.put("enabled", enabled);
		return result;
	}

	@Override
	public String toString() {
		return "XDMCollection [name=" + name + ", version=" + getVersion()
				+ ", docType=" + docType + ", created at=" + getCreatedAt()
				+ ", by=" + getCreatedBy() + ", description=" + description
				+ ", enabled=" + enabled + "]";
	}
	
	
}
