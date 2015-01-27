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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name", 
		"docType", 
		"path",
		"unique",
		"description" 
})
public class XDMIndex extends XDMEntity {
	
	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	true)
	private String docType;

	@XmlElement(required = 	true)
	private String path;

	@XmlElement(required = 	false, defaultValue = "false")
	private boolean unique;
	
	@XmlElement(required = 	false)
	private String description;
		
	public XDMIndex() {
		// for JAXB
		super();
	}
	
	public XDMIndex(int version, Date createdAt, String createdBy, String name, 
			String docType, String path, boolean unique, String description) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.docType = docType;
		this.path = path;
		this.unique = unique;
		this.description = description;
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

	public boolean isUnique() {
		return unique;
	}

	@Override
	public int hashCode() {
		return 31 + name.hashCode();
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
		XDMIndex other = (XDMIndex) obj;
		return name.equals(other.name); 
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("name", name);
		result.put("version", getVersion());
		result.put("created at", getCreatedAt().toString());
		result.put("created by", getCreatedBy());
		result.put("path", path);
		result.put("document type", docType);
		result.put("unique", unique);
		result.put("description", description);
		return result;
	}

	@Override
	public String toString() {
		return "XDMIndex [name=" + name + ", version=" + getVersion()
				+ ", docType=" + docType + ", path=" + path + ", unique=" + unique
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + "]";
	}
	
}
