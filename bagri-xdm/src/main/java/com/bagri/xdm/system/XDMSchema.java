package com.bagri.xdm.system;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XDMSchema {

	private String name;
	private int version;
	private String description;
	private boolean active;
	private Date createdAt;
	private String createdBy;
	//private Map<String, Object> props = new HashMap<String, Object>();
	private Properties props = new Properties();

	public XDMSchema(String name, int version, String description, boolean active, 
			Date createdAt, String createdBy, Properties props) {
		super();
		this.name = name;
		this.version = version;
		this.description = description;
		this.active = active;
		this.createdAt = createdAt;
		this.createdBy = createdBy;
		setProperties(props);
	}

	public String getName() {
		return name;
	}
	
	public int getVersion() {
		return version;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
		//version++;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public String getCreatedBy() {
		return createdBy;
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
	
	public void updateVersion() {
		version++;
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
		return "XDMSchema [name=" + name + ", version=" + version + 
			", description=" + description + ", active=" + active + 
			", createdAt=" + createdAt + ", createdBy=" + createdBy + 
			", props=" + props + "]";
	}
	
	
}
