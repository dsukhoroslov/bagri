package com.bagri.server.hazelcast.task.doc;

import static com.bagri.core.server.api.CacheConstants.PN_XDM_SCHEMA_POOL; 

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Document;
import com.bagri.core.system.Permission;
//import com.hazelcast.core.Offloadable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProvider extends com.bagri.client.hazelcast.task.doc.DocumentProvider {

	private transient DocumentManagement docMgr;
    
	//@Override
	//public String getExecutorName() {
		// configure it. can even decide it dynamically.. ..
	//	return PN_XDM_SCHEMA_POOL;
		//return Offloadable.NO_OFFLOADING;
	//}

	@Autowired
    @Override
	public void setRepository(SchemaRepository repo) {
		super.setRepository(repo);
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	public DocumentAccessor process(Entry<DocumentKey, Document> entry) {
    	try {
    		checkPermission(Permission.Value.read);
			return docMgr.getDocument(uri, context);
    	} catch (BagriException ex) {
    		return null; //ex; think about this case!!
    	}
	}


}
