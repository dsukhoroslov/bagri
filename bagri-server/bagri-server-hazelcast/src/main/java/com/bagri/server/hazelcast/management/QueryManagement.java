/**
 * 
 */
package com.bagri.server.hazelcast.management;

import static com.bagri.core.Constants.*;
import static com.bagri.support.util.PropUtils.getOutputProperties;
import static com.bagri.support.util.XQUtils.props2Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.server.hazelcast.task.schema.SchemaQueryCleaner;
import com.bagri.server.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.server.hazelcast.task.stats.StatisticsReseter;
import com.bagri.support.stats.StatsAggregator;
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
	private com.bagri.core.api.QueryManagement queryMgr;
	
    
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
	public boolean clearCache() {
		
		SchemaQueryCleaner task = new SchemaQueryCleaner(schemaName);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		boolean result = true;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (!entry.getValue().get()) {
					result = false;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("clearCache.error; ", ex);
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
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax"),
		@ManagedOperationParameter(name = "props", description = "Query processing properties")})
	public String[] parseQuery(String query, Properties props) {
		XQPreparedExpression xqpExp = null;
		try {
			XQStaticContext ctx = xqConn.getStaticContext();
			props2Context(schemaManager.getEntity().getProperties(), ctx);
			props2Context(props, ctx);
			xqpExp = xqConn.prepareExpression(query, ctx);
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
	public String[] runQuery(String query, boolean useXDM, Properties props) {
		logger.debug("runQuery.enter; got query: {}, properties: {}", query, props);
		
		String[] result = null;
		try {
			if (useXDM) {
				ResultCursor<XQItemAccessor> cursor = queryMgr.executeQuery(query, null, props);
				result = extractResult(cursor, props);
				cursor.close();
			} else {
				XQStaticContext ctx = xqConn.getStaticContext();
				props2Context(schemaManager.getEntity().getProperties(), ctx);
				props2Context(props, ctx);
			    XQExpression xqExp = xqConn.createExpression(ctx);
			    XQResultSequence xqSec = xqExp.executeQuery(query);
			    result = extractResult(xqSec, props);
			    xqExp.close();
			}	
		} catch (Exception ex) {
			logger.error("runQuery.error", ex); 
			throw new RuntimeException(ex.getMessage());
		}
		logger.debug("runQuery.exit; returning {} items", result.length);
		return result;
	}
	
	private String[] extractResult(ResultCursor<XQItemAccessor> cursor, Properties props) throws BagriException {
		try {
			List<String> buff = new ArrayList<>();
			XQProcessor xqp = ((BagriXQConnection) xqConn).getProcessor();
			Properties outProps = getOutputProperties(props);
			for (XQItemAccessor item: cursor) {
				buff.add(xqp.convertToString(item, outProps));
			}
			return buff.toArray(new String[buff.size()]);
		} catch (Exception ex) {
			logger.error("extractResult.error", ex); 
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}
	
	private String[] extractResult(XQResultSequence sequence, Properties props) throws BagriException {
		try {
			List<String> buff = new ArrayList<>();
			if (sequence.first()) {
				do {
					buff.add(sequence.getItemAsString(props));
				} while (sequence.next());
			}
			return buff.toArray(new String[buff.size()]);
		} catch (Exception ex) {
			logger.error("extractResult.error", ex); 
			throw new BagriException(ex, BagriException.ecQuery);
		}
	}

	@ManagedOperation(description="Run XQuery. Returns string output specified by XQuery")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "query", description = "A query request provided in XQuery syntax"),
		@ManagedOperationParameter(name = "useXDM", description = "use XDM (true) or XQJ query interface"),
		@ManagedOperationParameter(name = "bindings", description = "A map of query parameters"),
		@ManagedOperationParameter(name = "props", description = "Query processing properties")})
	public String[] runPreparedQuery(String query, boolean useXDM, CompositeData bindings, Properties props) {
		logger.debug("runPreparedQuery.enter; got bindings: {}, properties: {}", bindings, props);
		
		String[] result;
		try {
			if (useXDM) {
				Set<String> keys = bindings.getCompositeType().keySet();
				Map<String, Object> params = new HashMap<>(keys.size()); 
			    for (String key: keys) {
			    	params.put(key, bindings.get(key)); 
			    }
				ResultCursor<XQItemAccessor> cursor = queryMgr.executeQuery(query, params, props);
				result = extractResult(cursor, props);
				cursor.close();
			} else {
				XQStaticContext ctx = xqConn.getStaticContext();
				props2Context(schemaManager.getEntity().getProperties(), ctx);
				props2Context(props, ctx);
				XQPreparedExpression xqpExp = xqConn.prepareExpression(query, ctx);
			    for (String key: bindings.getCompositeType().keySet()) {
			    	xqpExp.bindObject(new QName(key), bindings.get(key), null); 
			    }
			    XQResultSequence xqSec = xqpExp.executeQuery();
			    result = extractResult(xqSec, props);
			    xqpExp.close();
			}
		} catch (Exception ex) {
			logger.error("runPreparedQuery.error", ex); 
			throw new RuntimeException(ex.getMessage());
		}
		logger.debug("runPreparedQuery.exit; returning {} items", result.length);
	    return result;
	}

	private void setQueryProperties(Properties props) {
		props.setProperty(pn_client_fetchSize, String.valueOf(fetchSize));
		props.setProperty(pn_xqj_queryTimeout, String.valueOf(queryTimeout));
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
