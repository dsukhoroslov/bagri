package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import com.bagri.xdm.common.XDMEntity;

/**
 * Represents external Java library containing extension functions and/or triggers.
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"name", 
		"fileName",
		"description",
		"enabled",
		"functions"
})
public class XDMLibrary extends XDMEntity {

	@XmlAttribute(required = true)
	@XmlID
	private String name;
		
	@XmlElement(required = 	true)
	private String fileName;

	@XmlElement(required = 	false)
	private String description;
		
	@XmlElement(required = false, defaultValue = "true")
	private boolean enabled = true;

	@XmlElement(name="function")
	@XmlElementWrapper(name="functions")
	private List<XDMFunction> functions = new ArrayList<XDMFunction>();

	/**
	 * default constructor
	 */
	public XDMLibrary() {
		// for JAXB
		super();
	}
	
	/**
	 * 
	 * @param version the version
	 * @param createdAt the date/time of version creation
	 * @param createdBy the user who has created the version
	 * @param name the library name
	 * @param fileName the library jar name
	 * @param description the library description
	 * @param enabled the library enable flag
	 */
	public XDMLibrary(int version, Date createdAt, String createdBy, String name, 
			String fileName, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		this.enabled = enabled;
	}

	/**
	 * 
	 * @return the library name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the library file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 
	 * @return the library description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * 
	 * @return the library enable flag
	 */
	public boolean isEnabled() {
		return enabled;
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
	 * 
	 * @return the collection of functions registered in the library
	 */
	public List<XDMFunction> getFunctions() {
		return functions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> convert() {
		Map<String, Object> result = super.convert();
		result.put("name", name);
		result.put("fileName", fileName == null ? "" : fileName);
		result.put("description", description);
		result.put("functions", functions.size());
		result.put("enabled", enabled);
		return result;
	}

	
}
