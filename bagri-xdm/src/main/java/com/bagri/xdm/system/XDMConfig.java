package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system",	propOrder = {
		"nodes", 
		"schemas",
		"modules",
		"libraries",
		"formats",
		"stores"
})
@XmlRootElement(name = "config", namespace = "http://www.bagridb.com/xdm/system") 
public class XDMConfig {
	
	@XmlElement(name="node")
	@XmlElementWrapper(name="nodes")
	private List<XDMNode> nodes = new ArrayList<>();

	@XmlElement(name="schema")
	@XmlElementWrapper(name="schemas")
	private List<XDMSchema> schemas = new ArrayList<>();
	
	@XmlElement(name="module")
	@XmlElementWrapper(name="modules")
	private List<XDMModule> modules = new ArrayList<>();

	@XmlElement(name="library")
	@XmlElementWrapper(name="libraries")
	private List<XDMLibrary> libraries = new ArrayList<>();

	@XmlElement(name="dataFormat")
	@XmlElementWrapper(name="dataFormats")
	private List<XDMDataFormat> formats = new ArrayList<>();

	@XmlElement(name="dataStore")
	@XmlElementWrapper(name="dataStores")
	private List<XDMDataStore> stores = new ArrayList<>();

	public List<XDMNode> getNodes() {
		return nodes;
	}
	
	public List<XDMSchema> getSchemas() {
		return schemas;
	}
	
	public List<XDMModule> getModules() {
		return modules;
	}

	public List<XDMLibrary> getLibraries() {
		return libraries;
	}

	public List<XDMDataFormat> getDataFormats() {
		return formats;
	}

	public List<XDMDataStore> getDataStores() {
		return stores;
	}

}
