package com.bagri.xdm.process.hazelcast;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XMLProvider extends com.bagri.xdm.access.hazelcast.process.XMLProvider {

    //private static final transient Logger logger = LoggerFactory.getLogger(XMLProvider.class);
    
	private XDMDocumentManagement xdmProxy;
	    
    @Autowired
	public void setXdmProxy(XDMDocumentManagement xdmProxy) {
		this.xdmProxy = xdmProxy;
		//logger.trace("setXdmProxy; got proxy: {}", xdmProxy); 
	}
	    
    @Override
	public String call() throws Exception {
		//logger.trace("call.enter; builder: {}", eBuilder.getRoot());
		//Collection<String> result = xdmProxy.getXML(eBuilder, template, params);
		//logger.trace("call.exit; returning: {}", result.size());
		return xdmProxy.getDocumentAsString(docId);
	}
}
