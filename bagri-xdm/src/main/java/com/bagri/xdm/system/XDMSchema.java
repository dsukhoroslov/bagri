package com.bagri.xdm.system;

import java.util.Date;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.api.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system",
	propOrder = {
		"name", 
		"active", 
		"description", 
		"props"
})
public class XDMSchema extends XDMEntity {

	@XmlAttribute(required = true)
	private String name;
	
	@XmlElement(required = true)
	private String description;
	
	@XmlAttribute(required = true)
	private boolean active;
	//private Map<String, Object> props = new HashMap<String, Object>();
	
	@XmlElement(name = "properties", required = false)
	private Properties props = new Properties();

	public XDMSchema(String name, int version, String description, boolean active, 
			Date createdAt, String createdBy, Properties props) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		this.active = active;
		setProperties(props);
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
		//version++;
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public Properties getProperties() {
		return props; //new Properties(props); // Collections.unmodifiableMap(props);
	}
	
	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}
	
	public void setProperties(Properties props) {
		this.props.clear();
		if (props != null) {
			this.props.putAll(props);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XDMSchema other = (XDMSchema) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "XDMSchema [name=" + name + ", version=" + getVersion() + 
			", description=" + description + ", active=" + active + 
			", created at=" + getCreatedAt() + ", by=" + getCreatedBy() + 
			", props=" + props + "]";
	}
	
	
}

