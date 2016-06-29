package com.bagri.xdm.cache.hazelcast.task.doc;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.cache.api.TransactionManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.domain.Document;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProcessor extends com.bagri.xdm.client.hazelcast.task.doc.DocumentProcessor {

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
	
	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}
	
	@Override
	public Object process(Entry<Long, Document> entry) {

		// TODO: rewrite it to get use of entry
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	try {
	    	return txMgr.callInTransaction(txId, false, new Callable<Document>() {
	    		
		    	public Document call() throws Exception {
		    		return docMgr.storeDocumentFromString(uri, content, props);
		    	}
	    	});
    	} catch (XDMException ex) {
    		// log it ?
    	}
		return null;
	}
    
}
