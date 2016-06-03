package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

	public XDMLibrary() {
		// for JAXB
		super();
	}
	
	public XDMLibrary(int version, Date createdAt, String createdBy, String name, 
			String fileName, String description, boolean enabled) {
		super(version, createdAt, createdBy);
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public boolean setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			//this.updateVersion("???");
			return true;
		}
		return false;
	}

	public List<XDMFunction> getFunctions() {
		return functions;
	}

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
