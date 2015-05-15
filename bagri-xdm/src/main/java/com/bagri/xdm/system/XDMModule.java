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
		"fileName",
		"description",
		"text",
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
	private String text;

	@XmlElement(required = 	false, defaultValue = "true")
	private boolean enabled = true;

	public XDMModule() {
		// for JAXB
		super();
	}
	
	public XDMModule(int version, Date createdAt, String createdBy, String name, 
			String fileName, String description, String text, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		this.text = text;
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
	
	public String getText() {
		return text;
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

	public void setText(String text) {
		this.text = text;
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
	
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("name", name);
		result.put("version", getVersion());
		result.put("created at", getCreatedAt().toString());
		result.put("created by", getCreatedBy());
		result.put("fileName", fileName);
		result.put("description", description);
		result.put("enabled", enabled);
		result.put("text", text);
		return result;
	}

	@Override
	public String toString() {
		return "XDMModule [name=" + name + ", version=" + getVersion()
				+ ", fileName=" + fileName + ", description=" + description  
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", enabled=" + enabled + "]";
	}
	

}
