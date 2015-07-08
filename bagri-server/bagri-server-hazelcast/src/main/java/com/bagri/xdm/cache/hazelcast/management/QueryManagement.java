/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.TabularData;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.StatsAggregator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaQueryCleaner;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.client.common.impl.XDMModelManagementBase;
import com.bagri.xdm.client.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.domain.XDMDocumentType;
import com.hazelcast.core.Member;

/**
 * @author Denis Sukhoroslov
 *
 */
@ManagedResource(description="(X)Query Management MBean")
public class QueryManagement extends SchemaFeatureManagement {
	
    private XQConnection xqConn;
    
    public QueryManagement(String schemaName) {
    	super(schemaName);
    }

    public void setXQConnection(XQConnection xqConn) {
		this.xqConn = xqConn;
	}
	
	@ManagedOperation(description="clear Query cache")
	public boolean clear() {
		
		SchemaQueryCleaner task = new SchemaQueryCleaner(schemaName);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		boolean result = true;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (!entry.getValue().get()) {
					result = false;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("clear.error; ", ex);
			}
		}
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

	@ManagedAttribute(description="Returns aggregated QueryManagement invocation statistics, per method")
	public TabularData getInvocationStatistics() {
		return super.getSeriesStatistics(new StatisticSeriesCollector(schemaName, "queryStats"), aggregator);
	}
	
	@ManagedOperation(description="Reset QueryManagement invocation statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "queryStats")); 
	}

	@Override
	protected String getFeatureKind() {
		return "QueryManagement";
	}

}
