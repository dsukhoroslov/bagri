package com.bagri.xdm.cache.hazelcast.task.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.api.XDMDocumentManagement;
//import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XQCommandExecutor extends com.bagri.xdm.client.hazelcast.task.query.XQCommandExecutor {

	private static final transient Logger logger = LoggerFactory.getLogger(XQCommandExecutor.class);
    
	private transient XDMDocumentManagement xdmProxy;
    
    @Autowired
	public void setXdmProxy(XDMDocumentManagement xdmProxy) {
		this.xdmProxy = xdmProxy;
		logger.debug("setXdmProxy; got manager: {}", xdmProxy); 
	}
    
	@Override
	public Object call() throws Exception {
		
		if (isQuery) {
			return xdmProxy.executeXQuery(command, bindings, context);
		} else {
	        return xdmProxy.executeXCommand(command, bindings, context);
		}
    }

}
