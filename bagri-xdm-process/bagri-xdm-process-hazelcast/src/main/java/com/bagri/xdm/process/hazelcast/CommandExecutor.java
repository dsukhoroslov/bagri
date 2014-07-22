package com.bagri.xdm.process.hazelcast;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.process.hazelcast.util.HazelcastUtils;

public class CommandExecutor extends com.bagri.xdm.access.hazelcast.process.CommandExecutor {

	private static final transient Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    
	private transient XDMDocumentManagerServer xdmManager;
    
    @Autowired
	public void setXdmManager(XDMDocumentManagerServer xdmManager) {
		this.xdmManager = xdmManager;
		logger.debug("setXdmManager; got manager: {}", xdmManager); 
	}
    
	@Override
	public Object call() throws Exception {
		
		if (xdmManager == null) {
			ApplicationContext ctx = HazelcastUtils.findContext();
			xdmManager = ctx.getBean(XDMDocumentManagerServer.class); 
		}
		
        return xdmManager.executeXCommand(command, bindings, context);
    }
	
}
