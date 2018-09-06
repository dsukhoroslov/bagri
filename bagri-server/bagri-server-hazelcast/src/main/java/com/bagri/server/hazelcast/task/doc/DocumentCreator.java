package com.bagri.server.hazelcast.task.doc;

import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.server.hazelcast.util.SpringContextHolder.getContext;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.server.api.TransactionManagement;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCreator extends com.bagri.client.hazelcast.task.doc.DocumentCreator {

	private transient DocumentManagement docMgr;
	private transient TransactionManagement txMgr;
	
	@Autowired
	@Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.docMgr = repo.getDocumentManagement();
		this.txMgr = (TransactionManagement) repo.getTxManagement();
	}

	@Override
	public DocumentAccessor call() throws Exception {
    	
		checkPermission(Permission.Value.modify);
    	
    	TransactionIsolation tiLevel = ((SchemaRepositoryImpl) repo).getTransactionLevel(context); 
    	if (tiLevel == null) {
    		// bypass tx stack completely!
    		return docMgr.storeDocument(uri, content, context);
    	}
    	
    	return txMgr.callInTransaction(txId, false, tiLevel, new Callable<DocumentAccessor>() {
    		
	    	public DocumentAccessor call() throws Exception {
	    		return docMgr.storeDocument(uri, content, context);
	    	}
    	});
    }

	@Override
	protected void checkRepo() {
		String schemaName = context.getProperty(pn_schema_name);
		ApplicationContext ctx = getContext(schemaName);
		repo = ctx.getBean(SchemaRepositoryImpl.class);
	}

}
