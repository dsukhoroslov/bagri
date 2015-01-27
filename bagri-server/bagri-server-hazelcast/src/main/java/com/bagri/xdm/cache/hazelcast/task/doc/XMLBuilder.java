package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XMLBuilder extends com.bagri.xdm.client.hazelcast.task.doc.XMLBuilder {

    private static final transient Logger logger = LoggerFactory.getLogger(XMLBuilder.class);
	    
	private XDMQueryManagement xdmProxy;
	    
    @Autowired
	public void setXdmProxy(XDMQueryManagement xdmProxy) {
		this.xdmProxy = xdmProxy;
		logger.trace("setXdmProxy; got proxy: {}", xdmProxy); 
	}
	    
    @Override
	public Collection<String> call() throws Exception {
		logger.trace("call.enter; container: {}", exp); //eBuilder.getRoot());
		Collection<String> result = xdmProxy.getXML(exp, template, params);
		logger.trace("call.exit; returning: {}", result.size());
		return result;
	}

}
