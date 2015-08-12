package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.client.hazelcast.task.query.DocumentIdsProvider;
import com.bagri.xdm.client.hazelcast.task.query.DocumentUrisProvider;
import com.bagri.xdm.client.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.domain.XDMQuery;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class QueryManagementImpl implements XDMQueryManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(QueryManagementImpl.class);
	
    private RepositoryImpl repo;
	private IExecutorService execService;
	
	public QueryManagementImpl() {
		// what should we do here? 
	}
	
	void initialize(RepositoryImpl repo) {
		this.repo = repo;
		execService = repo.getHazelcastClient().getExecutorService(PN_XDM_SCHEMA_POOL);
	}
	
	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) throws XDMException {

		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentURIs.enter; query: {}", query);
		
		DocumentUrisProvider task = new DocumentUrisProvider(query, repo.getTransactionId());
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
	public Collection<Long> getDocumentIDs(ExpressionContainer query) throws XDMException {

		long stamp = System.currentTimeMillis();
		logger.trace("getDocumentIDs.enter; query: {}", query);
		
		DocumentIdsProvider task = new DocumentIdsProvider(query, repo.getTransactionId());
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
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) throws XDMException {
		long stamp = System.currentTimeMillis();
		logger.trace("getXML.enter; got query: {}; template: {}; params: {}", query, template, params);
		
		XMLBuilder xb = new XMLBuilder(query, repo.getTransactionId(), template, params);
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
	public Iterator executeXCommand(String command, Map bindings, Properties props) throws XDMException {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXCommand.enter; command: {}; bindings: {}; context: {}", command, bindings, props);
		Iterator result = repo.execXQuery(false, command, bindings, props);
		logger.trace("executeXCommand.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
		return result;
	}

	@Override
	public Iterator executeXQuery(String query, Map bindings, Properties props) throws XDMException {

		long stamp = System.currentTimeMillis();
		logger.trace("executeXQuery.enter; query: {}; bindings: {}; context: {}", query, bindings, props);
		Iterator result = repo.execXQuery(true, query, bindings, props);
		logger.trace("executeXQuery.exit; time taken: {}; returning: {}", System.currentTimeMillis() - stamp, result);
		return result; 
	}
	
	@Override
	public int getQueryKey(String query) {
		// TODO: implement it via cifer ...
		return query.hashCode();
	}

}
