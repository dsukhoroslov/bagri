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

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"description",
		"storeClass",
		"enabled",
		"props"
})
public class XDMDataStore extends XDMEntity {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	false)
	private String description;

	@XmlElement(required = 	true)
	private String storeClass;
	
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(name = "properties")
	@XmlJavaTypeAdapter(XDMEntriesAdapter.class)
	private Properties props = new Properties();
	
	public XDMDataStore() {
		// for JAXB
		super();
	}

	public XDMDataStore(int version, Date createdAt, String createdBy, String name, String description, 
			String storeClass, boolean enabled, Properties props) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		this.storeClass = storeClass;
		this.enabled = enabled;
		this.props.putAll(props);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the storeClass
	 */
	public String getStoreClass() {
		return storeClass;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return the props
	 */
	public Properties getProperties() {
		return props;
	}
	
	public boolean addProperty(String key, String value) {
		return props.setProperty(key, value) == null;
	}
	
	public boolean removeProperty(String key) {
		return props.remove(key) != null;
	}

	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("description", description);
		result.put("store", storeClass);
		result.put("enabled", enabled);
		result.put("properties", props.size());
		return result;
	}
	
}
