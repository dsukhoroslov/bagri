/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.domain.XDMDocumentType;

/**
 * @author Denis Sukhoroslov
 *
 */
@ManagedResource(description="(X)Query Management MBean")
public class QueryManagement implements SelfNaming {
	
    private static final transient Logger logger = LoggerFactory.getLogger(QueryManagement.class);
	//private static final String schema_management = "SchemaManagement";
    private static final String type_schema = "Schema";

	private XDMSchemaDictionary schemaDictionary;
    private XQConnection xqConn;
    
    private String schemaName;
    
    public QueryManagement(String schemaName) {
    	this.schemaName = schemaName;
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

	@ManagedAttribute(description="Returns Document Types registered in the Schema")
	public String[] getRegisteredTypes() {
		Collection<XDMDocumentType> types = ((XDMSchemaDictionaryBase) schemaDictionary).getDocumentTypes();
		String[] result = new String[types.size()];
		Iterator<XDMDocumentType> itr = types.iterator();
		for (int i=0; i < types.size(); i++) {
			result[i] = itr.next().getRootPath();
		}
		Arrays.sort(result);
		return result;
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

	@ManagedOperation(description="Run XQuery. Returns string output specified by XQuery")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax"),
		@ManagedOperationParameter(name = "bindings", description = "A map of query parameters")})
	public String runPreparedQuery(String query, Map<String, Object> bindings) {
		XQPreparedExpression xqExp;
		try {
		    xqExp = xqConn.prepareExpression(query);
		    // TODO: bind params properly..
		    for (Map.Entry<String, Object> e: bindings.entrySet()) {
		    	xqExp.bindString(new QName(e.getKey()), e.getValue().toString(), null); 
		    }
		    XQResultSequence xqSec = xqExp.executeQuery();
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
