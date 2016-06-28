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
import javax.xml.namespace.QName;

import com.bagri.xdm.common.XDMEntity;

/**
 * Represents index registered in XDM schema.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"docType", 
		"path",
		"dataType",
		"caseSensitive",
		"range",
		"unique",
		"description",
		"enabled"
})
public class Index extends XDMEntity {
	
	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = true)
	private String docType;
	
	@XmlTransient
	private String typePath;

	@XmlElement(required = true)
	private String path;

	@XmlElement(required = true)
	private QName dataType;

	@XmlElement(required = false, defaultValue = "true")
	private boolean caseSensitive = true;

	@XmlElement(required = false, defaultValue = "false")
	private boolean range = false;

	@XmlElement(required = false, defaultValue = "false")
	private boolean unique = false;
	
	@XmlElement(required = false)
	private String description;
		
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	/**
	 * default constructor
	 */
	public Index() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the index name
	 * @param docType the type of document to be used by index
	 * @param typePath
	 * @param path the XPath to the value to be indexed
	 * @param dataType the XML type of the value to be indexed
	 * @param caseSensitive is index case-sensitive or not
	 * @param range is it a range index or not
	 * @param unique is it unique index or not
	 * @param description the index description
	 * @param enabled the index enabled flag
	 */
	public Index(int version, Date createdAt, String createdBy, String name, 
			String docType, String typePath, String path, QName dataType, boolean caseSensitive, boolean range, boolean unique, 
			String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.docType = docType;
		this.typePath = typePath;
		this.path = path;
		this.dataType = dataType;
		this.caseSensitive = caseSensitive;
		this.range = range;
		this.unique = unique;
		this.description = description;
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return the index name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the index description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the type of document to be used by index
	 */
	public String getDocumentType() {
		return docType;
	}

	/**
	 * 
	 * @return ??
	 */
	public String getTypePath() {
		return typePath;
	}

	/**
	 * 
	 * @return the XPath to the value to be indexed
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 
	 * @return the XML type of the value to be indexed. See XML Schema (XSD) types for more details
	 */
	public QName getDataType() {
		return dataType;
	}
	
	/**
	 * 
	 * @return is index case-sensitive or not
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * 
	 * @return the index enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * 
	 * @return is index compatible to be used in range queries or not
	 */
	public boolean isRange() {
		return range;
	}

	/**
	 * 
	 * @return is index controls data uniqueness for the indexed values
	 */
	public boolean isUnique() {
		return unique;
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
		Index other = (Index) obj;
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
		result.put("data type", dataType.toString());
		result.put("case sensitive", caseSensitive);
		result.put("range", range);
		result.put("unique", unique);
		result.put("description", description);
		result.put("enabled", enabled);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMIndex [name=" + name + ", version=" + getVersion()
				+ ", docType=" + docType + ", typePath=" + typePath
				+ ", path=" + path + ", dataType=" + dataType
				+ ", caseSensitive=" + caseSensitive + ", range=" + range
				+ ", unique=" + unique + ", created at=" + getCreatedAt()
				+ ", by=" + getCreatedBy() + ", description=" + description
				+ ", enabled=" + enabled + "]";
	}
	
}
