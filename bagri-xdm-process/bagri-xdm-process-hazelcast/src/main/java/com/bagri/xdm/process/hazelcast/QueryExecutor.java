package com.bagri.xdm.process.hazelcast;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;

public class QueryExecutor extends com.bagri.xdm.access.hazelcast.process.QueryExecutor {

	private static final transient Logger logger = LoggerFactory.getLogger(QueryExecutor.class);
    
	private transient XDMDocumentManagement xdmManager;
    
    @Autowired
	public void setXdmManager(XDMDocumentManagement xdmManager) {
		this.xdmManager = xdmManager;
		logger.debug("setXdmManager; got manager: {}", xdmManager); 
	}
    
	@Override
	public Object call() throws Exception {
		
		if (xdmManager == null) {
			//ApplicationContext ctx = HazelcastUtils.findContext();
			ApplicationContext ctx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
			logger.debug("call; got context: {}, for schema: {}", ctx, schemaName); 
			xdmManager = ctx.getBean(XDMDocumentManagement.class); 
		}
		
        return xdmManager.executeXQuery(query, bindings, context);
    }

}
