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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"description",
		"extensions",
		"parserClass",
		"builderClass",
		"enabled",
		"props"
})
public class XDMDataFormat extends XDMEntity {

	public static final String df_json = "JSON"; 
	public static final String df_xml = "XML"; 
	
	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	false)
	private String description;
		
	//@XmlValue
	@XmlList
	private Set<String> extensions = new HashSet<>();

	@XmlElement(required = true)
	private String parserClass;

	@XmlElement(required = 	false)
	private String builderClass;
	
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(name = "properties")
	@XmlJavaTypeAdapter(XDMEntriesAdapter.class)
	private Properties props = new Properties();
	
	public XDMDataFormat() {
		// for JAXB
		super();
	}

	public XDMDataFormat(int version, Date createdAt, String createdBy, String name, String description, 
			Collection<String> extensions, String parserClass, String builderClass, boolean enabled, Properties props) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		if (extensions != null) {
			this.extensions.addAll(extensions);
		}
		this.parserClass = parserClass;
		this.builderClass = builderClass;
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
	 * @return the parserClass
	 */
	public String getParserClass() {
		return parserClass;
	}

	/**
	 * @return the builderClass
	 */
	public String getBuilderClass() {
		return builderClass;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public boolean addExtension(String extension) {
		return extensions.add(extension);
	}
	
	public boolean removeExtension(String extension) {
		return extensions.remove(extension);
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
		result.put("extensions", extensions.toString());
		result.put("parser", parserClass);
		result.put("builder", builderClass);
		result.put("enabled", enabled);
		result.put("properties", props.size());
		return result;
	}
	
}
