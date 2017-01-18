package com.bagri.core.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents Bagri cluster configuration file 
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"nodes", 
		"schemas",
		"modules",
		"libraries",
		"formats",
		"stores"
})
@XmlRootElement(name = "config", namespace = "http://www.bagridb.com/schema/system") 
public class Config {
	
	@XmlElement(name="node")
	@XmlElementWrapper(name="nodes")
	private List<Node> nodes = new ArrayList<>();

	@XmlElement(name="schema")
	@XmlElementWrapper(name="schemas")
	private List<Schema> schemas = new ArrayList<>();
	
	@XmlElement(name="module")
	@XmlElementWrapper(name="modules")
	private List<Module> modules = new ArrayList<>();

	@XmlElement(name="library")
	@XmlElementWrapper(name="libraries")
	private List<Library> libraries = new ArrayList<>();

	@XmlElement(name="dataFormat")
	@XmlElementWrapper(name="dataFormats")
	private List<DataFormat> formats = new ArrayList<>();

	@XmlElement(name="dataStore")
	@XmlElementWrapper(name="dataStores")
	private List<DataStore> stores = new ArrayList<>();

	/**
	 * 
	 * @return node templates
	 */
	public List<Node> getNodes() {
		return nodes;
	}
	
	/**
	 * 
	 * @return registered schemas
	 */
	public List<Schema> getSchemas() {
		return schemas;
	}
	
	/**
	 * 
	 * @return registered modules
	 */
	public List<Module> getModules() {
		return modules;
	}

	/**
	 * 
	 * @return registered libraries
	 */
	public List<Library> getLibraries() {
		return libraries;
	}

	/**
	 * 
	 * @return registered data formats
	 */
	public List<DataFormat> getDataFormats() {
		return formats;
	}

	/**
	 * 
	 * @return registered document stores
	 */
	public List<DataStore> getDataStores() {
		return stores;
	}

}
