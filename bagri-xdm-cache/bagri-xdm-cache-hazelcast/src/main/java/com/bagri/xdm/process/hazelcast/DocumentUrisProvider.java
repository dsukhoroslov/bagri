package com.bagri.xdm.process.hazelcast;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentUrisProvider extends com.bagri.xdm.access.hazelcast.process.DocumentUrisProvider {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentUrisProvider.class);
	    
	private XDMDocumentManagement xdmProxy;
	    
	@Autowired
	public void setXdmProxy(XDMDocumentManagement xdmProxy) {
		this.xdmProxy = xdmProxy;
		logger.trace("setXdmProxy; got proxy: {}", xdmProxy); 
	}
	    
	@Override
	public Collection<String> call() throws Exception {
		logger.trace("call.enter; container: {}", exp); //eBuilder.getRoot());
		Collection<String> result = xdmProxy.getDocumentURIs(exp);
		logger.trace("call.exit; returning: {}", result);
		return result;
	}

}
