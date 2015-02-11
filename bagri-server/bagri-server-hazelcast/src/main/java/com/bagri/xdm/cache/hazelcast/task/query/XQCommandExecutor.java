package com.bagri.xdm.cache.hazelcast.task.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMQueryManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XQCommandExecutor extends com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor {

	private static final transient Logger logger = LoggerFactory.getLogger(XQCommandExecutor.class);
    
	private transient XDMQueryManagement queryMgr;
    
    @Autowired
    @Qualifier("queryProxy") //queryProxy //queryManager
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
		logger.debug("setQueryManager; got QueryManager: {}", queryMgr); 
	}
    
	@Override
	public Object call() throws Exception {
		
		if (isQuery) {
			return queryMgr.executeXQuery(command, bindings, context);
		} else {
	        return queryMgr.executeXCommand(command, bindings, context);
		}
    }

}
