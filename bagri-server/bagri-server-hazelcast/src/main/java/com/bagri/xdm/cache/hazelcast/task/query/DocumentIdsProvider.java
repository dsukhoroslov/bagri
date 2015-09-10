package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMQueryManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentIdsProvider extends com.bagri.xdm.client.hazelcast.task.query.DocumentIdsProvider {

	private transient XDMQueryManagement queryMgr;
    
    @Autowired
    @Qualifier("queryProxy")
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		//logger.debug("setQueryManager; got QueryManager: {}", queryMgr); 
	}
    
    @Override
	public Collection<Long> call() throws Exception {
       	return queryMgr.getDocumentIDs(exp);
	}

}
