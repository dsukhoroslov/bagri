package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bagri.xdm.common.XDMEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagri.com/xdm/system", propOrder = {
		"name", 
		"active", 
		"description", 
		"props",
		"indexes",
		"triggers"
})
public class XDMSchema extends XDMEntity {

	@XmlAttribute(required = true)
	private String name;
	
	@XmlElement(required = true)
	private String description;
	
	@XmlAttribute(required = true)
	private boolean active;
	//private Map<String, Object> props = new HashMap<String, Object>();
	
	@XmlElement(name = "properties")
	@XmlJavaTypeAdapter(XDMEntriesAdapter.class)
	private Properties props = new Properties();
	
	@XmlElement(name="index")
	@XmlElementWrapper(name="indexes")
	private Set<XDMIndex> indexes = new HashSet<XDMIndex>();
	
	@XmlElement(name="triggers")
	@XmlElementWrapper(name="triggers")
	private Set<XDMTriggerDef> triggers = new HashSet<XDMTriggerDef>();
	
	public XDMSchema() {
		// we need it for JAXB
		super();
	}

	public XDMSchema(int version, Date createdAt, String createdBy, String name, 
			String description, boolean active,	Properties props) {
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
	
	public Set<XDMIndex> getIndexes() {
		return indexes;
	}
	
	public boolean addIndex(XDMIndex index) {
		return indexes.add(index);
	}
	
	public boolean enableIndex(String name, boolean enable) {
		for (XDMIndex index: indexes) {
			if (name.equals(index.getName())) {
				return index.setEnabled(enable);
			}
		}
		return false;
	}

	public XDMIndex getIndex(String name) {
		for (XDMIndex index: indexes) {
			if (name.equals(index.getName())) {
				return index;
			}
		}
		return null;
	}

	public XDMIndex removeIndex(String name) {
		for (XDMIndex index: indexes) {
			if (name.equals(index.getName())) {
				if (indexes.remove(index)) {
					return index;
				}
				break;
			}
		}
		return null;
	}
	
	public Set<XDMTriggerDef> getTriggers() {
		return triggers;
	}
	
	public boolean addTrigger(XDMTriggerDef trigger) {
		return triggers.add(trigger);
	}
	
	public boolean enableTrigger(String className, boolean enable) {
		for (XDMTriggerDef trigger: triggers) {
			if (name.equals(trigger.getClassName())) {
				return trigger.setEnabled(enable);
			}
		}
		return false;
	}

	public XDMTriggerDef getTrigger(String className) {
		for (XDMTriggerDef trigger: triggers) {
			if (name.equals(trigger.getClassName())) {
				return trigger;
			}
		}
		return null;
	}

	public XDMTriggerDef removeTrigger(String className) {
		for (XDMTriggerDef trigger: triggers) {
			if (name.equals(trigger.getClassName())) {
				if (triggers.remove(trigger)) {
					return trigger;
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return 31 + name.hashCode();
	}

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
		XDMSchema other = (XDMSchema) obj;
		return name.equals(other.name);
	}

	@Override
	public String toString() {
		return "XDMSchema [name=" + name + ", version=" + getVersion() + 
			", description=" + description + ", active=" + active + 
			", created at=" + getCreatedAt() + ", by=" + getCreatedBy() + 
			", props=" + props + ", indexes=" + indexes + 
			", triggers=" + triggers + "]";
	}
	
}

