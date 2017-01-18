package com.bagri.core.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents REST resource which will be exposed by schema. Resource implementation resides in a corresponding Module
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"name", 
		"path",
		"description",
		"module",
		"enabled"
})
public class Resource extends Entity {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = true)
	private String path;

	@XmlElement(required = false)
	private String description;
		
	@XmlElement(required = true)
	private String module;

	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	/**
	 * default constructor
	 */
	public Resource() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the resource name
	 * @param path the resource base path
	 * @param description the resource description
	 * @param module the module containing resource implementation
	 * @param enabled the resource enable flag
	 */
	public Resource(int version, Date createdAt, String createdBy, String name, 
			String path, String description, String module, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.path = path;
		this.description = description;
		this.module = module;
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return the resource name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the resource base path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 
	 * @return the resource description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the module implementing resource
	 */
	public String getModule() {
		return module;
	}

	/**
	 * 
	 * @return the resource enabled flag
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
		Resource other = (Resource) obj;
		return name.equals(other.name); 
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("path", path);
		result.put("description", description);
		result.put("module", module);
		result.put("enabled", enabled);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Resource [name=" + name + ", version=" + getVersion()
				+ ", path=" + path + ", description=" + description  
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", module=" + module + ", enabled=" + enabled + "]";
	}
	

}
