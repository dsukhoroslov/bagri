package com.bagri.xdm.system;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents Document Store configuration
 *  
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"description",
		"storeClass",
		"readOnly",
		"enabled",
		"props"
})
public class DataStore extends Entity {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = false)
	private String description;

	@XmlElement(required = true)
	private String storeClass;
	
	@XmlElement(required = true)
	private boolean readOnly = true;

	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(name = "properties")
	@XmlJavaTypeAdapter(EntriesAdapter.class)
	private Properties props = new Properties();
	
	/**
	 * default constructor
	 */
	public DataStore() {
		// for JAXB
		super();
	}

	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the data store name
	 * @param description the data store description
	 * @param storeClass the data store implementation class
	 * @param enabled the data store enable flag
	 * @param props the data store properties
	 */
	public DataStore(int version, Date createdAt, String createdBy, String name, String description, 
			String storeClass, boolean readOnly, boolean enabled, Properties props) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		this.storeClass = storeClass;
		this.readOnly = readOnly;
		this.enabled = enabled;
		this.props.putAll(props);
	}

	/**
	 * @return the data store name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the data store description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the data store implementation class
	 */
	public String getStoreClass() {
		return storeClass;
	}

	/**
	 * @return the data store read-only flag
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return the data store enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return the data store properties
	 */
	public Properties getProperties() {
		return props;
	}
	
	/**
	 * adds data store property
	 * 
	 * @param key the property key
	 * @param value the property value
	 * @return true if the property was inserted, false if updated
	 */
	public boolean addProperty(String key, String value) {
		return props.setProperty(key, value) == null;
	}
	
	/**
	 * removes data store property
	 * 
	 * @param key the property key
	 * @return true if the property was removed, false otherwise
	 */
	public boolean removeProperty(String key) {
		return props.remove(key) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("description", description);
		result.put("store", storeClass);
		result.put("read-only", readOnly);
		result.put("enabled", enabled);
		result.put("properties", props.size());
		return result;
	}
	
}
