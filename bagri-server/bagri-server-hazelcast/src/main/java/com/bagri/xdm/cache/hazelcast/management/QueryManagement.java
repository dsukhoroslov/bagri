/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.common.util.PropUtils.getOutputProperties;
import static com.bagri.xdm.common.XDMConstants.pn_client_fetchSize;
import static com.bagri.xdm.common.XDMConstants.pn_client_submitTo;
import static com.bagri.xdm.common.XDMConstants.pn_queryTimeout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
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

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.stats.StatsAggregator;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMCacheConstants;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaQueryCleaner;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xqj.BagriXQConnection;
import com.bagri.xquery.api.XQProcessor;
//import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
//import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
//import com.hazelcast.core.HazelcastInstance;
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
	private XDMQueryManagement queryMgr;
	
    
    public QueryManagement(String schemaName) {
    	super(schemaName);
    }

    @Override
	public void setSchemaManager(SchemaManager schemaManager) {
    	super.setSchemaManager(schemaManager);
		queryMgr = schemaManager.getRepository().getQueryManagement();
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
	
	@ManagedOperation(description="Cancel currently running query started from the same JMX connection")
	public void cancelQuery() {
		try {
			// are we in exec state now?
			XQProcessor xqp = ((BagriXQConnection) xqConn).getProcessor();
			xqp.cancelExecution();
		} catch (XQException ex) {
			logger.error("cancelQuery.error", ex); 
			throw new RuntimeException(ex.getMessage());
		} 
	}
	    
	@ManagedOperation(description="Parse XQuery. Return array of parameter names, if any")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax")})
	public String[] parseQuery(String query) {
		XQPreparedExpression xqpExp = null;
		try {
			xqpExp = xqConn.prepareExpression(query);
			QName[] vars = xqpExp.getAllExternalVariables();
			String[] result = null;
			if (vars != null) {
				result = new String[vars.length];
				for (int i=0; i < vars.length; i++) {
					result[i] = vars[i].toString();
				}
			}
			xqpExp.close();
			return result;
		} catch (XQException ex) {
			logger.error("parseQuery.error", ex); 
			throw new RuntimeException(ex.getMessage());
		} 
	}

	@ManagedOperation(description="Run XQuery. Returns string output specified by XQuery")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax"),
		@ManagedOperationParameter(name = "useXDM", description = "use XDM (true) or XQJ query interface"),
		@ManagedOperationParameter(name = "props", description = "Query processing properties")})
	public String runQuery(String query, boolean useXDM, Properties props) {
		
		String result = null;
		try {
			if (useXDM) {
				Iterator<?> itr = queryMgr.executeQuery(query, null, props);
				result = extractResult(itr, props);
			} else {
			    XQExpression xqExp = xqConn.createExpression();
			    XQResultSequence xqSec = xqExp.executeQuery(query);
			    result = xqSec.getSequenceAsString(props);
			    xqSec.close();
			    xqExp.close();
			    return result;
			}	
			return result;
		} catch (XQException | XDMException ex) {
			logger.error("runQuery.error", ex); 
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	private String extractResult(Iterator<?> itr, Properties props) throws XQException {
		StringBuffer buff = new StringBuffer();
		//ResultCursor rc = (ResultCursor) itr;
		//rc.deserialize(((RepositoryImpl) schemaManager.getRepository()).getHzInstance());
		XQProcessor xqp = ((BagriXQConnection) xqConn).getProcessor();
		Properties outProps = getOutputProperties(props);
		int fSize = Integer.parseInt(props.getProperty(pn_client_fetchSize, String.valueOf(fetchSize)));
		if (fSize > 0) {
			int cnt = 0;
			while (itr.hasNext() && cnt < fSize) {
				buff.append(xqp.convertToString(itr.next(), outProps));
				cnt++;
			}
		} else {
			while (itr.hasNext()) {
				buff.append(xqp.convertToString(itr.next(), outProps));
			}
		}
		return buff.toString();
	}
	
	@ManagedOperation(description="Run XQuery. Returns string output specified by XQuery")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax"),
		@ManagedOperationParameter(name = "useXDM", description = "use XDM (true) or XQJ query interface"),
		@ManagedOperationParameter(name = "bindings", description = "A map of query parameters"),
		@ManagedOperationParameter(name = "props", description = "Query processing properties")})
	public String runPreparedQuery(String query, boolean useXDM, CompositeData bindings, Properties props) {
		logger.trace("runPreparedQuery.enter; got bindings: {}, properties: {}", bindings, props);
		
		if (bindings == null) {
			return runQuery(query, useXDM, props);
		}
		
		String result;
		try {
			if (useXDM) {
				Set<String> keys = bindings.getCompositeType().keySet();
				Map<QName, Object> params = new HashMap<>(keys.size()); 
			    for (String key: keys) {
			    	params.put(new QName(key), bindings.get(key)); 
			    }
				Iterator itr = queryMgr.executeQuery(query, params, props);
				result = extractResult(itr, props);
			} else {
				XQPreparedExpression xqpExp = xqConn.prepareExpression(query);
			    for (String key: bindings.getCompositeType().keySet()) {
			    	xqpExp.bindObject(new QName(key), bindings.get(key), null); 
			    }
			    XQResultSequence xqSec = xqpExp.executeQuery();
			    result = xqSec.getSequenceAsString(props);
			    xqSec.close();
			    xqpExp.close();
			}
		} catch (XQException | XDMException ex) {
			logger.error("runPreparedQuery.error", ex); 
			throw new RuntimeException(ex.getMessage());
		}
		logger.trace("runPreparedQuery.exit; returning: {}", result);
	    return result;
	}

	private void setQueryProperties(Properties props) {
		props.setProperty(pn_client_fetchSize, String.valueOf(fetchSize));
		props.setProperty(pn_queryTimeout, String.valueOf(queryTimeout));
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
