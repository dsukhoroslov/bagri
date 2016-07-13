package com.bagri.xdm.cache.hazelcast.task.query;

import static com.bagri.xdm.api.TransactionManagement.TX_NO;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.cache.api.TransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.xdm.client.hazelcast.impl.QueuedCursorImpl;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryExecutor extends com.bagri.xdm.client.hazelcast.task.query.QueryExecutor {

	//private static final transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);
	
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
	public QueuedCursorImpl call() throws Exception {
    	
    	//logger.info("call; clientId: {}", clientId);

    	boolean readOnly = queryMgr.isReadOnlyQuery(query);
    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	if (readOnly) {
    		checkPermission(Permission.Value.read);
    	} else {
    		checkPermission(Permission.Value.modify);
    	}

    	if (txId == TX_NO && readOnly) {
			return (QueuedCursorImpl) queryMgr.executeQuery(query, params, context);
    	}

    	return ((TransactionManagement) repo.getTxManagement()).callInTransaction(txId, false, new Callable<QueuedCursorImpl>() {
    		
	    	public QueuedCursorImpl call() throws XDMException {
				return (QueuedCursorImpl) queryMgr.executeQuery(query, params, context);
	    	}
    	});
    }

}
