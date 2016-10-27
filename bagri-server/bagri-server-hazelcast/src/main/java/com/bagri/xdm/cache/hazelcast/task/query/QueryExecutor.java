package com.bagri.xdm.cache.hazelcast.task.query;

import static com.bagri.xdm.api.TransactionManagement.TX_NO;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.ResultCursor;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.cache.api.TransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryExecutor extends com.bagri.xdm.client.hazelcast.task.query.QueryExecutor {

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
    		
	    	public ResultCursor call() throws XDMException {
				return queryMgr.executeQuery(query, params, context);
	    	}
    	});
    }

}
