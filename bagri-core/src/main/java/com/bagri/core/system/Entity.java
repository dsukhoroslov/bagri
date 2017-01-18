package com.bagri.core.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.bagri.core.Convertable;
import com.bagri.core.Versionable;

/**
 * Represents the basic configurable XDM schema entity. All configuration changes are stored and can be reviewed later on.  
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
		"version", 
		"createdAt", 
		"createdBy"
})
@XmlSeeAlso({
    Node.class,
    Schema.class,
    Module.class,
    Library.class,
    Collection.class,
    Fragment.class,
    Index.class,
    TriggerDefinition.class,
    Role.class,
    User.class,
    DataFormat.class,
    DataStore.class
})
public abstract class Entity implements Convertable<Map<String, Object>>, Versionable {
	
	@XmlElement(required = true)
	private int version;

	@XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
	private Date createdAt;

	@XmlElement(required = true)
	private String createdBy;
	
	/**
	 * default constructor
	 */
	public Entity() {
		// ...
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 */
	public Entity(int version, Date createdAt, String createdBy) {
		this.version = version;
		// todo: think about other Date implementation, joda date, for instance..
		this.createdAt = new Date(createdAt.getTime());
		this.createdBy = createdBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getVersion() {
		return version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreatedAt() {
		return new Date(createdAt.getTime());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = new HashMap<>();
		result.put("version", version);
		result.put("created at", createdAt.toString());
		result.put("created by", createdBy);
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateVersion(String by) {
		version++;
		createdAt = new Date();
		createdBy = by;
	}

}
