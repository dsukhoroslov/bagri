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
public class DocumentContentProvider extends com.bagri.xdm.client.hazelcast.task.doc.DocumentContentProvider {

	private transient XDMDocumentManagement docMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}
	    
    @Override
	public String call() throws Exception {
		return docMgr.getDocumentAsString(docId);
	}
}
