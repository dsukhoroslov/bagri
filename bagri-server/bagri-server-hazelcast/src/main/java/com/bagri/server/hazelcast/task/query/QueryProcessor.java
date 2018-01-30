package com.bagri.server.hazelcast.task.query;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.model.QueryResult;
import com.bagri.core.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.QueryManagementImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryProcessor extends com.bagri.client.hazelcast.task.query.QueryProcessor {

	private static final transient Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
	
	private transient QueryManagementImpl queryMgr;
	
	public QueryProcessor() {
		super();
	}
	
	public QueryProcessor(String clientId, long txId, String query, Map<String, Object> params, Properties context, boolean readOnly) {
		super(clientId, txId, query, params, context, readOnly);
	}
	
    @Autowired
	public void setRepository(SchemaRepository repo) {
		//super.setRepository(repo);
		this.queryMgr = (QueryManagementImpl) repo.getQueryManagement();
	}
	
	@Override
	public void processBackup(Entry<Long, QueryResult> entry) {
		// ??
	}

	@Override
	public ResultCursor process(Entry<Long, QueryResult> entry) {
		try {
			return queryMgr.executeQuery(query, params, context);
		} catch (BagriException ex) {
			// already logged?
			logger.error("process.error;", ex);
		}
		return null;
	}

}
