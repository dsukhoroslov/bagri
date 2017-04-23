package com.bagri.core.system;

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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents Data Format configuration
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"name", 
		"description",
		"extensions",
		"type",
		"parserClass",
		"builderClass",
		"modelerClass",
		"enabled",
		"props"
})
public class DataFormat extends Entity {

	public static final String df_json = "JSON"; 
	public static final String df_xml = "XML"; 
	
	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	false)
	private String description;
		
	@XmlList
	private Set<String> extensions = new HashSet<>();

	@XmlElement(required = true)
	private String type;
	
	@XmlElement(required = true)
	private String parserClass;

	@XmlElement(required = 	false)
	private String builderClass;
	
	@XmlElement(required = 	false)
	private String modelerClass;
	
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(name = "properties")
	@XmlJavaTypeAdapter(EntriesAdapter.class)
	private Properties props = new Properties();

	/**
	 * default constructor
	 */
	public DataFormat() {
		// for JAXB
		super();
	}

	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the data format name
	 * @param description the data format description
	 * @param type the data format type name
	 * @param extensions the data format file extensions
	 * @param parserClass the data format parser class
	 * @param builderClass the data format builder class
	 * @param enabled the data format enabled flag
	 * @param props the data format properties
	 */
	public DataFormat(int version, Date createdAt, String createdBy, String name, String description, String type, 
			Collection<String> extensions, String parserClass, String builderClass, String modelerClass, boolean enabled, 
			Properties props) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		this.type = type;
		if (extensions != null) {
			this.extensions.addAll(extensions);
		}
		this.parserClass = parserClass;
		this.builderClass = builderClass;
		this.modelerClass = modelerClass;
		this.enabled = enabled;
		if (props != null) {
			this.props.putAll(props);
		}
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
	 * @return the extensions
	 */
	public Set<String> getExtensions() {
		return extensions;
	}

	/**
	 * @return the description
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the parser class
	 */
	public String getParserClass() {
		return parserClass;
	}

	/**
	 * @return the builder class
	 */
	public String getBuilderClass() {
		return builderClass;
	}

	/**
	 * @return the builder class
	 */
	public String getModelerClass() {
		return modelerClass;
	}

	/**
	 * @return the enabled flag
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * adds data format extension 
	 * 
	 * @param extension the file extension like xml, json, etc..
	 * @return true if extension has been added, false otherwise
	 */
	public boolean addExtension(String extension) {
		return extensions.add(extension);
	}
	
	/**
	 * removes data format extension
	 * 
	 * @param extension the file extension like xml, json, etc..
	 * @return true if extension has been removed, false otherwise
	 */
	public boolean removeExtension(String extension) {
		return extensions.remove(extension);
	}

	/**
	 * @return the data format properties
	 */
	public Properties getProperties() {
		return props;
	}

	/**
	 * adds data format property
	 * 
	 * @param key the property key
	 * @param value the property value
	 * @return true if the property was inserted, false if updated
	 */
	public boolean addProperty(String key, String value) {
		return props.setProperty(key, value) == null;
	}
	
	/**
	 * removes data format property
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
		result.put("extensions", extensions.toString());
		result.put("parser", parserClass);
		result.put("builder", builderClass);
		result.put("modeler", modelerClass);
		result.put("enabled", enabled);
		result.put("properties", props.size());
		result.put("type", type);
		return result;
	}
	
}
