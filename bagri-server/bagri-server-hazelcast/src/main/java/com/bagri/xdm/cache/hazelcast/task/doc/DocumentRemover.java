package com.bagri.xdm.cache.hazelcast.task.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentRemover extends com.bagri.xdm.client.hazelcast.task.doc.DocumentRemover {

    private static final transient Logger logger = LoggerFactory.getLogger(DocumentRemover.class);
    
	private transient XDMDocumentManagement docMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
		logger.debug("setDocManager; got DocumentManager: {}", docMgr); 
	}
    
    @Override
	public XDMDocument call() throws Exception {
		logger.trace("call.enter; docId: {}", docId);
		docMgr.removeDocument(docId);
		return null;
	}


}
