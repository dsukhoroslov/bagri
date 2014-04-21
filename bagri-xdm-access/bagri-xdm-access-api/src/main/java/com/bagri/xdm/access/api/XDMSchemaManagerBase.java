package com.bagri.xdm.access.api;

import java.util.Map;
import java.util.Properties;

import javax.management.openmbean.CompositeData;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.system.XDMSchema;

public abstract class XDMSchemaManagerBase {
	
	protected String schemaName;
	protected XDMDocumentManagerServer docManager;
	protected XDMSchemaDictionary schemaDictionary;
	
	public XDMSchemaManagerBase() {
		//this.schemaName = schemaName;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public XDMDocumentManagerServer getDocumentManager() {
		return docManager;
	}
	
	public void setDocumentManager(XDMDocumentManagerServer docManager) {
		this.docManager = docManager;
	}
	
	public XDMSchemaDictionary getSchemaDictionary() {
		return schemaDictionary;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	protected abstract XDMSchema getSchema();
	protected abstract void flushSchema(XDMSchema schema);

	public Properties getProperties() {
		return getSchema().getProperties();
	}

	public void setProperties(Properties props) {
		XDMSchema schema = getSchema();
		schema.setProperties(props);
		flushSchema(schema);
	}

	public CompositeData getAllProperties() {
		Properties props = getProperties();
		return JMXUtils.propsToComposite(schemaName, "properties", props);
	}
	
	public String getProperty(String name) {
		return getSchema().getProperty(name);
	}
	
	public void setProperty(String name, String value) {
		XDMSchema schema = getSchema();
		schema.setProperty(name, value);
		flushSchema(schema);
	}
	
	public void removeProperty(String name) {
		// @TODO: set to default value!
		setProperty(name, null); 
	}
	
}
