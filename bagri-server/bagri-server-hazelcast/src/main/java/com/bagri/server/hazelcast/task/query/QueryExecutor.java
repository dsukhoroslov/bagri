package com.bagri.server.hazelcast.task.query;

import static com.bagri.core.api.TransactionManagement.TX_NO;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryExecutor<T> extends com.bagri.client.hazelcast.task.query.QueryExecutor<T> {

	//private static final transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);
	
	private transient QueryManagement queryMgr;
    
    @Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.queryMgr = (QueryManagement) repo.getQueryManagement();
	}

    @Override
	public ResultCursor<T> call() throws Exception {
    	
    	//logger.info("call.enter; context: {}; params: {}", context, params);

    	//((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	boolean readOnly = queryMgr.isQueryReadOnly(query, context);
    	if (readOnly) {
    		checkPermission(Permission.Value.read);
    	} else {
    		checkPermission(Permission.Value.modify);
    	}

    	TransactionIsolation tiLevel = ((SchemaRepositoryImpl) repo).getTransactionLevel(context); 
    	if ((tiLevel == null) || (txId == TX_NO && readOnly)) {
			return queryMgr.executeQuery(query, params, context);
    	}

    	ResultCursor<T> rc = ((TransactionManagement) repo.getTxManagement()).callInTransaction(txId, false, tiLevel, new Callable<ResultCursor<T>>() {
    		
	    	public ResultCursor<T> call() throws BagriException {
				return queryMgr.executeQuery(query, params, context);
	    	}
    	});
    	
    	//logger.info("call.exit; returning: {}; executor: {}", rc, this);
    	return rc;
    }

}
