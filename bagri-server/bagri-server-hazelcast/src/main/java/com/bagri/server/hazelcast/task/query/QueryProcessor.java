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
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.QueryManagementImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryProcessor extends com.bagri.client.hazelcast.task.query.QueryProcessor {

	//private static final transient Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
	
	private transient QueryManagementImpl queryMgr;
	
	public QueryProcessor() {
		super();
	}
	
	public QueryProcessor(boolean readOnly, String query, Map<String, Object> params, Properties props) {
		super(readOnly, query, params, props);
	}
	
    @Autowired
	public void setRepository(SchemaRepository repo) {
		//this.repo = repo;
		this.queryMgr = (QueryManagementImpl) repo.getQueryManagement();
	}
	
	@Override
	public void processBackup(Entry<Long, QueryResult> entry) {
		// ??
	}

	@Override
	public ResultCursor process(Entry<Long, QueryResult> entry) {
		try {
			return queryMgr.executeQuery(query, params, props);
		} catch (BagriException ex) {
			// already logged?
			//logger.error();
		}
		return null;
	}

}
