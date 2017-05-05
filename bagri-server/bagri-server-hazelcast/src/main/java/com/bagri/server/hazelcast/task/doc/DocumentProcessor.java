package com.bagri.server.hazelcast.task.doc;

import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProcessor extends com.bagri.client.hazelcast.task.doc.DocumentProcessor {

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
	public Object process(Entry<DocumentKey, Document> entry) {

		// TODO: rewrite it to get use of entry
    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	try {
	    	return txMgr.callInTransaction(txId, false, new Callable<Document>() {
	    		
		    	public Document call() throws Exception {
		    		return null; //docMgr.storeDocumentFromString(uri, content, props);
		    	}
	    	});
    	} catch (BagriException ex) {
    		// log it ?
    	}
		return null;
	}
    
}
