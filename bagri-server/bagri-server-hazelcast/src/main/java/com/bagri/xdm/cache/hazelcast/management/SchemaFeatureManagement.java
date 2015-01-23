package com.bagri.xdm.cache.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.xquery.XQConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMSchemaDictionary;

public abstract class SchemaFeatureManagement implements SelfNaming {
	
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static final String type_schema = "Schema";

    protected String schemaName;
	protected XDMSchemaDictionary schemaDictionary;
	
    public SchemaFeatureManagement(String schemaName) {
    	this.schemaName = schemaName;
    }

	@ManagedAttribute(description="Returns corresponding Schema name")
	public String getSchema() {
		return schemaName;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	protected abstract String getFeatureKind();

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=" + type_schema + ",name=" + schemaName + ",kind=" + getFeatureKind());
	}

}
