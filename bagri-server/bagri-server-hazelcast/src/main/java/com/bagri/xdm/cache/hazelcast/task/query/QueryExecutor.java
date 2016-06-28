package com.bagri.xdm.cache.hazelcast.task.query;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.client.hazelcast.impl.ResultCursor;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class QueryExecutor extends com.bagri.xdm.client.hazelcast.task.query.QueryExecutor {

	//private static final transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);
	
	private transient XDMQueryManagement queryMgr;
    
    @Autowired
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}
    
    @Autowired
	public void setRepository(RepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public ResultCursor call() throws Exception {
    	
    	//logger.info("call; clientId: {}", clientId);

    	boolean readOnly = queryMgr.isReadOnlyQuery(query);
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	if (readOnly) {
    		checkPermission(Permission.Value.read);
    	} else {
    		checkPermission(Permission.Value.modify);
    	}

    	if (txId == TX_NO && readOnly) {
			return (ResultCursor) queryMgr.executeQuery(query, params, context);
    	}

    	return ((XDMTransactionManagement) repo.getTxManagement()).callInTransaction(txId, false, new Callable<ResultCursor>() {
    		
	    	public ResultCursor call() throws XDMException {
				return (ResultCursor) queryMgr.executeQuery(query, params, context);
	    	}
    	});
    }

}
