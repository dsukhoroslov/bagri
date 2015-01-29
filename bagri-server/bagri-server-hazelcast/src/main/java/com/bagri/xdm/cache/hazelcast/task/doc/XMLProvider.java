package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XMLProvider extends com.bagri.xdm.client.hazelcast.task.doc.XMLProvider {

    //private static final transient Logger logger = LoggerFactory.getLogger(XMLProvider.class);
    
	private transient XDMDocumentManagement docMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
		//logger.debug("setDocManager; got DocumentManager: {}", docMgr); 
	}
	    
    @Override
	public String call() throws Exception {
		//logger.trace("call.enter; builder: {}", eBuilder.getRoot());
		//Collection<String> result = xdmProxy.getXML(eBuilder, template, params);
		//logger.trace("call.exit; returning: {}", result.size());
		return docMgr.getDocumentAsString(docId);
	}
}
