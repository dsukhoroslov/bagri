package com.bagri.xdm.cache.hazelcast.task.doc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCollectionUpdater extends com.bagri.xdm.client.hazelcast.task.doc.DocumentCollectionUpdater {

	private transient XDMDocumentManagement docMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}
	    
    @Override
	public Integer call() throws Exception {
    	if (add) {
    		return docMgr.addDocumentToCollections(docKey, collectIds);
    	} else {
    		return docMgr.removeDocumentFromCollections(docKey, collectIds);
    	}    	
	}


}
