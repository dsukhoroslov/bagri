/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMDocumentManagerBase;
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

	//private XDMDocumentManagerServer docManager;
	private XDMDocumentManagerBase docManager;
	private XDMSchemaDictionary schemaDictionary;
    private XQConnection xqConn;
    
    private String schemaName;
    
    public QueryManagement(String schemaName) {
    	this.schemaName = schemaName;
    }

	public void setDocumentManager(XDMDocumentManagerBase docManager) {
		this.docManager = docManager;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	public void setXQConnection(XQConnection xqConn) {
		this.xqConn = xqConn;
	}
	
	@ManagedAttribute(description="Returns corresponding Schema name")
	public String getSchema() {
		return schemaName;
	}

	@ManagedOperation(description="Run XQuery. Returns string output specified by XQuery")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax")})
	public String runQuery(String query) {
		XQExpression xqExp;
		try {
			xqExp = xqConn.createExpression();
		    XQResultSequence xqSec = xqExp.executeQuery(query);
		    return xqSec.getSequenceAsString(null);
		} catch (XQException ex) {
			String error = "error executing XQuery: " + ex.getMessage();
			logger.error(error, ex); 
			return error;
		}
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=" + type_schema + ",name=" + schemaName + ",kind=QueryManagement");
	}

}
