package com.bagri.core.system;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents schema where XDM documents are stored. The base container for all other document processing entities.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"name", 
		"active", 
		"description", 
		"props",
		"collections",
		"fragments",
		"indexes",
		"resources",
		"triggers"
})
@XmlRootElement
public class Schema extends Entity {

	@XmlAttribute(required = true)
	private String name;
	
	@XmlElement(required = true)
	private String description;
	
	@XmlAttribute(required = true)
	private boolean active;
	
	@XmlElement(name = "properties")
	@XmlJavaTypeAdapter(EntriesAdapter.class)
	private Properties props = new Properties();
	
	@XmlElement(name="collection")
	@XmlElementWrapper(name="collections")
	private Set<Collection> collections = new HashSet<>();
	
	@XmlElement(name="fragment")
	@XmlElementWrapper(name="fragments")
	private Set<Fragment> fragments = new HashSet<>();
	
	@XmlElement(name="index")
	@XmlElementWrapper(name="indexes")
	private Set<Index> indexes = new HashSet<>();

	@XmlElement(name="resource")
	@XmlElementWrapper(name="resources")
	private Set<Resource> resources = new HashSet<>();
	
	@XmlElement(name="trigger")
	@XmlElementWrapper(name="triggers")
	private Set<TriggerDefinition> triggers = new HashSet<>();
	
	/**
	 * default constructor
	 */
	public Schema() {
		// we need it for JAXB
		super();
	}

	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the schema name
	 * @param description the schema description
	 * @param active the schema active flag
	 * @param props the schema properties
	 */
	public Schema(int version, Date createdAt, String createdBy, String name, String description, boolean active, Properties props) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.description = description;
		this.active = active;
		setProperties(props);
	}

	/**
	 * 
	 * @return the schema name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the schema description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @param description the new schema description value
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 
	 * @return the schema active flag
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * 
	 * @param active the new active flag value
	 */
	public void setActive(boolean active) {
		this.active = active;
		//version++;
	}
	
	/**
	 * 
	 * @param key the schema property name
	 * @return the schema property value, if any
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	/**
	 * 
	 * @return the schema properties
	 */
	public Properties getProperties() {
		return props; //new Properties(props); // Collections.unmodifiableMap(props);
	}
	
	/**
	 * 
	 * @param key the schema property name
	 * @param value the new schema property value
	 */
	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}
	
	/**
	 * 
	 * @param props the schema properties
	 */
	public void setProperties(Properties props) {
		this.props.clear();
		if (props != null) {
			this.props.putAll(props);
		}
	}
	
	/**
	 * 
	 * @return the XDM collections registered in the schema
	 */
	public Set<Collection> getCollections() {
		return collections;
	}
	
	/**
	 * 
	 * @param collection the new collection to register in schema
	 * @return true if collection has been added, false otherwise
	 */
	public boolean addCollection(Collection collection) {
		return collections.add(collection);
	}
	
	/**
	 * 
	 * @param name the name of schema collection to enable
	 * @param enable the new enable value
	 * @return true if the collection enable flag has been changed, false otherwise
	 */
	public boolean enableCollection(String name, boolean enable) {
		for (Collection collection: collections) {
			if (name.equals(collection.getName())) {
				return collection.setEnabled(enable);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param name the name of the collection to search for
	 * @return the XDM collection instance if it is found, null otherwise
	 */
	public Collection getCollection(String name) {
		for (Collection collection: collections) {
			if (name.equals(collection.getName())) {
				return collection;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param collectId the XDM collection id
	 * @return true if the schema contains collection with id provided, false otherwise
	 */
	public boolean hasCollection(int collectId) {
		for (Collection collection: collections) {
			if (collectId == collection.getId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param name the name of the collection to remove from schema
	 * @return true if collection has been removed, false otherwise
	 */
	public Collection removeCollection(String name) {
		for (Collection collection: collections) {
			if (name.equals(collection.getName())) {
				if (collections.remove(collection)) {
					return collection;
				}
				break;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return XDM fragments registered in schema
	 */
	public Set<Fragment> getFragments() {
		return fragments;
	}

	/**
	 * 
	 * @param fragment the new fragment to register in schema
	 * @return true if fragment has been added, false otherwise
	 */
	public boolean addFragment(Fragment fragment) {
		return fragments.add(fragment);
	}
	
	/**
	 * 
	 * @param name the name of schema fragment to enable
	 * @param enable the new enable value
	 * @return true if the fragment enable flag has been changed, false otherwise
	 */
	public boolean enableFragment(String name, boolean enable) {
		for (Fragment fragment: fragments) {
			if (name.equals(fragment.getName())) {
				return fragment.setEnabled(enable);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param name the name of the fragment to search for
	 * @return the XDM fragment instance if it is found, null otherwise
	 */
	public Fragment getFragment(String name) {
		for (Fragment fragment: fragments) {
			if (name.equals(fragment.getName())) {
				return fragment;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name the name of the fragment to remove from schema
	 * @return true if fragment has been removed, false otherwise
	 */
	public Fragment removeFragment(String name) {
		for (Fragment fragment: fragments) {
			if (name.equals(fragment.getName())) {
				if (fragments.remove(fragment)) {
					return fragment;
				}
				break;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return XDM indices registered in schema
	 */
	public Set<Index> getIndexes() {
		return indexes;
	}

	/**
	 * 
	 * @param index the new index to register in schema
	 * @return true if index has been added, false otherwise
	 */
	public boolean addIndex(Index index) {
		return indexes.add(index);
	}

	/**
	 * 
	 * @param name the name of schema index to enable
	 * @param enable the new enable value
	 * @return true if the index enable flag has been changed, false otherwise
	 */
	public boolean enableIndex(String name, boolean enable) {
		for (Index index: indexes) {
			if (name.equals(index.getName())) {
				return index.setEnabled(enable);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param name the name of the index to search for
	 * @return the XDM index instance if it is found, null otherwise
	 */
	public Index getIndex(String name) {
		for (Index index: indexes) {
			if (name.equals(index.getName())) {
				return index;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name the name of the index to remove from schema
	 * @return true if index has been removed, false otherwise
	 */
	public Index removeIndex(String name) {
		for (Index index: indexes) {
			if (name.equals(index.getName())) {
				if (indexes.remove(index)) {
					return index;
				}
				break;
			}
		}
		return null;
	}

	/**
	 * 
	 * @return REST resources registered in schema
	 */
	public Set<Resource> getResources() {
		return resources;
	}
	
	/**
	 * 
	 * @param resource the new resource to register in schema
	 * @return true if resource has been added, false otherwise
	 */
	public boolean addResource(Resource resource) {
		return resources.add(resource);
	}
	
	/**
	 * 
	 * @param name the name of REST resource to enable
	 * @param enable the new enable value
	 * @return true if the resource enable flag has been changed, false otherwise
	 */
	public boolean enableResource(String name, boolean enable) {
		for (Resource resource: resources) {
			if (name.equals(resource.getName())) {
				return resource.setEnabled(enable);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param name the name of the resource to search for
	 * @return the REST resource instance if it is found, null otherwise
	 */
	public Resource getResource(String name) {
		for (Resource resource: resources) {
			if (name.equals(resource.getName())) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name the name of the resource to remove from schema
	 * @return true if resource has been removed, false otherwise
	 */
	public Resource removeResource(String name) {
		for (Resource resource: resources) {
			if (name.equals(resource.getName())) {
				if (triggers.remove(resource)) {
					return resource;
				}
				break;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return XDM triggers registered in schema
	 */
	public Set<TriggerDefinition> getTriggers() {
		return triggers;
	}
	
	/**
	 * 
	 * @param trigger the new trigger to register in schema
	 * @return true if trigger has been added, false otherwise
	 */
	public boolean addTrigger(TriggerDefinition trigger) {
		return triggers.add(trigger);
	}
	
	/**
	 * 
	 * @param name the name of schema trigger to enable
	 * @param enable the new enable value
	 * @return true if the trigger enable flag has been changed, false otherwise
	 */
	public boolean enableTrigger(String name, boolean enable) {
		for (TriggerDefinition trigger: triggers) {
			if (name.equals(trigger.getName())) {
				return trigger.setEnabled(enable);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param name the name of the trigger to search for
	 * @return the XDM trigger instance if it is found, null otherwise
	 */
	public TriggerDefinition getTrigger(String name) {
		for (TriggerDefinition trigger: triggers) {
			if (name.equals(trigger.getName())) {
				return trigger;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param name the name of the trigger to remove from schema
	 * @return true if trigger has been removed, false otherwise
	 */
	public TriggerDefinition removeTrigger(String name) {
		for (TriggerDefinition trigger: triggers) {
			if (name.equals(trigger.getName())) {
				if (triggers.remove(trigger)) {
					return trigger;
				}
				break;
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 + name.hashCode();
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
		Schema other = (Schema) obj;
		return name.equals(other.name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("active", active);
		result.put("description", description);
		result.put("props", props.size());
		result.put("collections", collections.size());
		result.put("fragments", fragments.size());
		result.put("indexes", indexes.size());
		result.put("resources", resources.size());
		result.put("triggers", triggers.size());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Schema [name=" + name + ", version=" + getVersion() + 
			", description=" + description + ", active=" + active + 
			", created at=" + getCreatedAt() + ", by=" + getCreatedBy() + 
			", props=" + props + ", indexes=" + indexes + 
			", triggers=" + triggers + ", fragments=" + fragments + 
			", collections=" + collections + ", resources=" + resources + "]";
	}
	
}

