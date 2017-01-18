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
 * Represents some (periodic) part of XDM document instance
 *  
 * @author Denis Sukhoroslov
 * @since 09.2015 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"name", 
		"docType", 
		"path",
		"description",
		"enabled"
})
public class Fragment extends Entity {
	
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

	/**
	 * default constructor
	 */
	public Fragment() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the fragment name
	 * @param docType the fragment document type
	 * @param path the fragment path
	 * @param description the fragment description
	 * @param enabled the fragment enable flag
	 */
	public Fragment(int version, Date createdAt, String createdBy, String name, 
			String docType, String path, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.docType = docType;
		this.path = path;
		this.description = description;
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return the fragment name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the fragment description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the fragment document type in Clark form
	 */
	public String getDocumentType() {
		return docType;
	}

	/**
	 * 
	 * @return the fragment XPath
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 
	 * @return the fragment enable flag
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * 
	 * @param enabled set fragment enable flag
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
		Fragment other = (Fragment) obj;
		return name.equals(other.name); 
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Fragment [name=" + name + ", version=" + getVersion()
				+ ", docType=" + docType + ", path=" + path 
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + ", enabled=" + enabled + "]";
	}
	
}