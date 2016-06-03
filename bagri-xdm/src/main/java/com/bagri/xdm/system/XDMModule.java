package com.bagri.xdm.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"fileName",
		"description",
		"namespace",
		"enabled"
})
public class XDMModule extends XDMEntity {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	true)
	private String fileName;

	@XmlElement(required = 	false)
	private String description;
		
	@XmlElement(required = 	true)
	private String namespace;

	@XmlTransient
	private String body;

	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	public XDMModule() {
		// for JAXB
		super();
	}
	
	public XDMModule(int version, Date createdAt, String createdBy, String name, 
			String fileName, String description, String namespace, String body, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		this.namespace = namespace;
		this.body = body;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescription() {
		return description;
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getBody() {
		return body;
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

	public void setBody(String body) {
		this.body = body;
		//this.updateVersion("???");
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
		XDMModule other = (XDMModule) obj;
		return name.equals(other.name); 
	}
	
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("fileName", fileName);
		result.put("description", description);
		result.put("namespace", namespace);
		result.put("enabled", enabled);
		result.put("body", body);
		return result;
	}

	@Override
	public String toString() {
		return "XDMModule [name=" + name + ", version=" + getVersion()
				+ ", fileName=" + fileName + ", description=" + description  
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", namespace=" + namespace + ", enabled=" + enabled + "]";
	}
	

}
