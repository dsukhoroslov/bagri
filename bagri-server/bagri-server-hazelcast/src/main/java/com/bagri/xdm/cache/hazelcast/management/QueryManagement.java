/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xdm.common.XDMConstants.pn_client_fetchSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_submitTo;
import static com.bagri.xdm.common.XDMConstants.pn_queryTimeout;

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
import com.bagri.xqj.BagriXQConnection;
import com.hazelcast.core.Member;

/**
 * @author Denis Sukhoroslov
 *
 */
@ManagedResource(description="(X)Query Management MBean")
public class QueryManagement extends SchemaFeatureManagement {

	private int fetchSize = 0;
	private int queryTimeout = 0;
    private XQConnection xqConn;
	private StatsAggregator qcAggregator;
    
    public QueryManagement(String schemaName) {
    	super(schemaName);
    }

    public void setXQConnection(XQConnection xqConn) {
		this.xqConn = xqConn;
	}
	
	@Override
	protected String getFeatureKind() {
		return "QueryManagement";
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
		    setQueryProperties();
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
		    setQueryProperties();
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

	private void setQueryProperties() {
		((BagriXQConnection) xqConn).getProcessor().getProperties().setProperty(pn_client_fetchSize, String.valueOf(fetchSize));
		((BagriXQConnection) xqConn).getProcessor().getProperties().setProperty(pn_queryTimeout, String.valueOf(queryTimeout));
	}	
	
	@ManagedAttribute(description="Returns aggregated QueryManagement invocation statistics, per method")
	public TabularData getInvocationStatistics() {
		return super.getSeriesStatistics(new StatisticSeriesCollector(schemaName, "queryStats"), aggregator);
	}
	
	@ManagedOperation(description="Reset QueryManagement invocation statistics")
	public void resetStatistics() {
		super.resetStatistics(new StatisticsReseter(schemaName, "queryStats")); 
	}

	@ManagedAttribute(description="Return aggregated query usage statistics, per cached query")
	public TabularData getQueryCacheStatistics() {
		if (qcAggregator == null) {
			qcAggregator = new StatsAggregator() {

				@Override
				@SuppressWarnings({ "unchecked", "rawtypes" })
				public Object[] aggregateStats(Object[] source, Object[] target) {
					target[0] = (Integer) source[0] + (Integer) target[0]; // accessed
					target[1] = (Integer) source[1] + (Integer) target[1]; // cached results
					target[2] = ((Comparable) source[2]).compareTo((Comparable) target[2]) < 0 ? source[2] : target[2]; // first  
					target[3] = (Integer) source[3] + (Integer) target[3]; // hits
					target[4] = ((Comparable) source[4]).compareTo((Comparable) target[4]) > 0 ? source[4] : target[4]; // last  
					target[5] = (Integer) source[5] + (Integer) target[5]; // miss
					target[6] = source[6]; // query
					target[7] = (Integer) source[7] + (Integer) target[7]; // result hits
					target[8] = (Integer) source[8] + (Integer) target[8]; // result miss
					return target;
				}
				
			};
		}
		
		return super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "queryCacheStats"), qcAggregator);
	}

	@ManagedAttribute(description="Returns query fetch size limit in records. 0 means no limit")
	public int getFetchSize() {
		return fetchSize;
	}
	
	@ManagedAttribute(description="Set query fetch size limit in records. 0 means no limit")
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}
	
	@ManagedAttribute(description="Returns query timeoit in seconds. 0 means no timeout")
	public int getQueryTimeout() {
		return queryTimeout;
	}
	
	@ManagedAttribute(description="Set query timeout in seconds. 0 means no timeout")
	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}
	
}
