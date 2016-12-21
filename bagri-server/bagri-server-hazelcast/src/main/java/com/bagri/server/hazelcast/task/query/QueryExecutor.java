package com.bagri.server.hazelcast.task.query;

import static com.bagri.core.api.TransactionManagement.TX_NO;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryExecutor extends com.bagri.client.hazelcast.task.query.QueryExecutor {

	private static final transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);
	
	private transient QueryManagement queryMgr;
    
    @Autowired
	public void setQueryManager(QueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}
    
    @Autowired
	public void setRepository(SchemaRepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public ResultCursor call() throws Exception {
    	
    	logger.trace("call; context: {}", context);

    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	boolean readOnly = queryMgr.isQueryReadOnly(query, context);
    	if (readOnly) {
    		checkPermission(Permission.Value.read);
    	} else {
    		checkPermission(Permission.Value.modify);
    	}

    	if (txId == TX_NO && readOnly) {
			return queryMgr.executeQuery(query, params, context);
    	}

    	return ((TransactionManagement) repo.getTxManagement()).callInTransaction(txId, false, new Callable<ResultCursor>() {
    		
	    	public ResultCursor call() throws BagriException {
				return queryMgr.executeQuery(query, params, context);
	    	}
    	});
    }

}
