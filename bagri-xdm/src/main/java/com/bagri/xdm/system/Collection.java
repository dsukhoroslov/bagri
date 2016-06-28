package com.bagri.xdm.system;

import java.util.Date;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents document's collection
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"id",
		"name", 
		"docType", 
		"description",
		"enabled"
})
public class Collection extends Entity {
	
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

	/**
	 * default constructor
	 */
	public Collection() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param id the collection id
	 * @param name the collection name
	 * @param docType the collection document type
	 * @param description the collection description
	 * @param enabled the collection enable flag
	 */
	public Collection(int version, Date createdAt, String createdBy, int id, String name, 
			String docType, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.id = id;
		this.name = name;
		this.docType = docType;
		this.description = description;
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return the collection id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return the collection name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the collection description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the collection document type in Clark form
	 */
	public String getDocumentType() {
		return docType;
	}

	/**
	 * 
	 * @return the collection enable flag
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * 
	 * @param enabled set collection enable flag
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
		return id;
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
		Collection other = (Collection) obj;
		return id == other.id; 
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Collection [id=" + id + ", name=" + name
				+ ", version=" + getVersion() + ", docType=" + docType
				+ ", created at=" + getCreatedAt() + ", by=" + getCreatedBy()
				+ ", description=" + description + ", enabled=" + enabled + "]";
	}
	
}
