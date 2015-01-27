package com.bagri.xdm.client.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentIdsProvider;
import com.bagri.xdm.client.hazelcast.task.doc.DocumentUrisProvider;
import com.bagri.xdm.client.hazelcast.task.doc.XMLBuilder;
import com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor;
import com.bagri.xdm.domain.XDMQuery;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class QueryManagementImpl implements XDMQueryManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
	private String clientId;
	private String schemaName;
    
	private HazelcastInstance hzClient;
	private IExecutorService execService;
	private ResultsIterator cursor;
	
	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) {

		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentURIs.enter; query: {}", query);
		
		DocumentUrisProvider task = new DocumentUrisProvider(query);
		Future<Collection<String>> future = execService.submit(task);
		Collection<String> result = null;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentURIs.error; error getting result", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getDocumentURIs.exit; time taken: {}; returning: {}", stamp, result);
		return result;
	}
	
	@Override
	public Collection<Long> getDocumentIDs(ExpressionContainer query) {

		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentIDs.enter; query: {}", query);
		
		DocumentIdsProvider task = new DocumentIdsProvider(query);
		Future<Collection<Long>> future = execService.submit(task);
		Collection<Long> result = null;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getDocumentIDs.error; error getting result", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getDocumentIDs.exit; time taken: {}; returning: {}", stamp, result);
		return result;
	}
	
	@Override
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) {
		long stamp = System.currentTimeMillis();
		logger.trace("getXML.enter; got query: {}; template: {}; params: {}", query, template, params);
		
		XMLBuilder xb = new XMLBuilder(query, template, params);
		Map<Member, Future<Collection<String>>> result = execService.submitToAllMembers(xb);

		Collection<String> xmls = new ArrayList<String>();
		for (Future<Collection<String>> future: result.values()) {
			try {
				Collection<String> c = future.get();
				if (c.isEmpty()) {
					continue;
				}
				xmls.addAll(c);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getXML; error getting result", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("getXML.exit; got query results: {}; time taken {}", xmls.size(), stamp);
		return xmls;
	}
	
	@Override
	public Iterator executeXCommand(String command, Map bindings, Properties props) {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXCommand.enter; command: {}; bindings: {}; context: {}", command, bindings, props);
		//try {
			Iterator result = execXQuery(false, command, bindings, props);
			logger.trace("executeXCommand.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			return result;
		//} catch (Exception ex) {
		//	logger.warn("executeXCommand.error; time taken: {}; exception: {}", System.currentTimeMillis() - stamp, ex);
		//}
		//return null; 
	}

	@Override
	public Iterator executeXQuery(String query, Map bindings, Properties props) {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXQuery.enter; query: {}; bindings: {}; context: {}", query, bindings, props);
		//try {
			Iterator result = execXQuery(true, query, bindings, props);
			logger.trace("executeXQuery.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
			return result; 
		//} catch (Exception ex) {
		//	logger.warn("executeXQuery.error; time taken: {}; exception: {}", System.currentTimeMillis() - stamp, ex);
		//	ex.printStackTrace();
		//}
		//return null; 
	}
	
	private Iterator execXQuery(boolean isQuery, String query, Map bindings, Properties props) { //throws Exception {
		
		//if (logger.isTraceEnabled()) {
		//	for (Object o: bindings.entrySet()) {
		//		Map.Entry e = (Map.Entry) o;
		//		logger.trace("execXQuery.binding; {}:{}; {}:{}", e.getKey().getClass().getName(),
		//				e.getKey(), e.getValue().getClass().getName(), e.getValue());
		//	}
		//}
		
		props.put("clientId", clientId);
		//props.put("batchSize", "5");
		
		String runOn = System.getProperty("xdm.client.submitTo", "any");
		
		XQCommandExecutor task = new XQCommandExecutor(isQuery, schemaName, query, bindings, props);
		Future<Object> future;
		if (isQuery) {
			if ("owner".equals(runOn)) {
				int key = getQueryKey(query);
				future = execService.submitToKeyOwner(task, key);
			} else if ("member".equals(runOn)) {
				int key = getQueryKey(query);
				Member member = hzClient.getPartitionService().getPartition(key).getOwner();
				future = execService.submitToMember(task, member);
			} else {
				future = execService.submit(task);
			}
		} else {
			future = execService.submit(task);
		}

		Iterator result = null;
		long timeout = Long.parseLong(props.getProperty("timeout", "0"));
		int fetchSize = Integer.parseInt(props.getProperty("batchSize", "0"));
		try {
			//if (cursor != null) {
			//	cursor.close(false);
			//}
			
			if (timeout > 0) {
				cursor = (ResultsIterator) future.get(timeout, TimeUnit.SECONDS);
			} else {
				cursor = (ResultsIterator) future.get();
			}
			
			logger.trace("execXQuery; got cursor: {}", cursor);
			if (cursor != null) {
				cursor.deserialize(hzClient);

				if (cursor.isFailure()) {
					//Exception ex = (Exception) cursor.next();
					//throw ex;
					while (cursor.hasNext()) {
						Object err = cursor.next();
						if (err instanceof String) {
							throw new RuntimeException((String) err);
						}
					}
				}
			}
			
			if (fetchSize == 0) {
				result = extractFromCursor(cursor);
			} else {
				result = cursor;
			}
		} catch (TimeoutException ex) {
			future.cancel(true);
			logger.warn("execXQuery.error; query timed out", ex);
		} catch (InterruptedException | ExecutionException ex) {
			// cancel future ??
			logger.error("execXQuery.error; error getting result", ex);
		}
		return result; 
	}
	
	private int getQueryKey(String query) {
		// get it from some common service, 
		// QueryManagement most probably
		return query.hashCode();
	}
	
	private Iterator extractFromCursor(ResultsIterator cursor) {
		List result = new ArrayList();
		while (cursor.hasNext()) {
			result.add(cursor.next());
		}
		return result.iterator();
	}

	
	
	
	@Override
	public XDMQuery getQuery(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addQuery(String query, Object xqExpression,
			ExpressionBuilder xdmExpression) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addExpression(String query, Object xqExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addExpression(String query, ExpressionBuilder xdmExpression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator getQueryResults(String query, Map<String, Object> params,
			Properties props) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator addQueryResults(String query, Map<String, Object> params,
			Properties props, Iterator results) {
		// TODO Auto-generated method stub
		return null;
	}

}
