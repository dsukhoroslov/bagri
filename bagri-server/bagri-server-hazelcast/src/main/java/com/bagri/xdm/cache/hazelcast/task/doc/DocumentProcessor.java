package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMTransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProcessor extends com.bagri.xdm.client.hazelcast.task.doc.DocumentProcessor {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
	
	private transient XDMRepository repo;
	private transient XDMDocumentManagement docMgr;
	private transient XDMTransactionManagement txMgr;
    
    @Autowired
    @Qualifier("docProxy")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}
    
    @Autowired
	public void setTxManager(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
	}

    @Autowired
	public void setRepository(XDMRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public Object process(Entry<Long, XDMDocument> entry) {

		// TODO: rewrite it to get use of entry
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	try {
	    	return txMgr.callInTransaction(txId, false, new Callable<XDMDocument>() {
	    		
		    	public XDMDocument call() throws Exception {
		    		return docMgr.storeDocumentFromString(docId, uri, xml);
		    	}
	    	});
    	} catch (XDMException ex) {
    		// log it ?
    	}
		return null;
	}
    
}
