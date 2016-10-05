package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents external XQuery module containing extension functions and/or triggers
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"fileName",
		"description",
		"prefix",
		"namespace",
		"enabled"
})
public class Module extends Entity {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = true)
	private String fileName;

	@XmlElement(required = false)
	private String description;
		
	@XmlElement(required = true)
	private String prefix;

	@XmlElement(required = true)
	private String namespace;

	@XmlTransient
	private String body;

	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	/**
	 * default constructor
	 */
	public Module() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the module name
	 * @param fileName the module file name
	 * @param description the module description
	 * @param prefix the module ns prefix
	 * @param namespace the module namespace
	 * @param body the module definition body
	 * @param enabled the module enable flag
	 */
	public Module(int version, Date createdAt, String createdBy, String name, String fileName, 
			String description, String prefix, String namespace, String body, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		this.prefix = prefix;
		this.namespace = namespace;
		this.body = body;
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return the module name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the module file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 
	 * @return the module description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the module ns prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * 
	 * @return the module namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * 
	 * @return the module definition body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * 
	 * @return the module enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * 
	 * @param enabled new enabled flag value
	 * @return true if flag has been changed, false otherwise
	 */
	public boolean setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			//this.updateVersion("???");
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param body the new module definition body 
	 */
	public void setBody(String body) {
		this.body = body;
		//this.updateVersion("???");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
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
		Module other = (Module) obj;
		return name.equals(other.name); 
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("fileName", fileName);
		result.put("description", description);
		result.put("prefix", prefix);
		result.put("namespace", namespace);
		result.put("enabled", enabled);
		result.put("body", body);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Module [name=" + name + ", version=" + getVersion()
				+ ", fileName=" + fileName + ", description=" + description  
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", prefix=" + prefix + ", namespace=" + namespace + ", enabled=" + enabled + "]";
	}
	

}
