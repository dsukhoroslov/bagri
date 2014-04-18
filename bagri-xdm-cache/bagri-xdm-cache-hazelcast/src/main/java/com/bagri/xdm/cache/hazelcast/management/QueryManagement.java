/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.api.XDMSchemaDictionary;

/**
 * @author Denis Sukhoroslov
 *
 */
@ManagedResource(description="(X)Query Management MBean")
public class QueryManagement implements SelfNaming {
	
    private static final transient Logger logger = LoggerFactory.getLogger(QueryManagement.class);
	//private static final String schema_management = "SchemaManagement";
    private static final String type_schema = "Schema";

	private XDMDocumentManagerServer docManager;
	private XDMSchemaDictionary schemaDictionary;
    
    private String schemaName;
    
    public QueryManagement(String schemaName) {
    	this.schemaName = schemaName;
    }

	public void setDocumentManager(XDMDocumentManagerServer docManager) {
		this.docManager = docManager;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	@ManagedAttribute(description="Returns corresponding Schema name")
	public String getSchema() {
		return schemaName;
	}

	@ManagedOperation(description="Run XQuery. Returns string output specified by XQuery")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax")})
	public String runQuery(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		//Hashtable keys = JMXUtils.getStandardKeys(type_schema + ".Query", "QueryManagement");
		//keys.put(type_schema, schemaName);
		//return JMXUtils.getObjectName("type=" + type_schema + ",Schema=" + schemaName + ",name=QueryManagement");
		return JMXUtils.getObjectName("type=" + type_schema + ",name=" + schemaName + ".QueryManagement");
	}

}
