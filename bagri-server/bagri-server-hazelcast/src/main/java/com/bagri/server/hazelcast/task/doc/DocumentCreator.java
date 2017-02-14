package com.bagri.server.hazelcast.task.doc;

//import static com.bagri.server.hazelcast.util.SpringContextHolder.getContext; 

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;

import com.bagri.core.api.DocumentManagement;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.client.hazelcast.task.doc.DocumentCreator {

	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
    
	@Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}

	@Override
	public Document call() throws Exception {
    	
    	//final ApplicationContext ctx = getContext("default");
    	//repo = ctx.getBean(SchemaRepository.bean_id, SchemaRepository.class);
    	//final DocumentManagement docMgr = repo.getDocumentManagement();
    	//final TransactionManagement txMgr = (TransactionManagement) repo.getTxManagement();

    	((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.modify);
    	
    	return txMgr.callInTransaction(txId, false, new Callable<Document>() {
    		
	    	public Document call() throws Exception {
	    		return docMgr.storeDocumentFromString(uri, content, props);
	    	}
    	});
    }
	
}
