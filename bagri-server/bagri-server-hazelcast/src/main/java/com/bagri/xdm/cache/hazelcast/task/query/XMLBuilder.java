package com.bagri.xdm.cache.hazelcast.task.query;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.system.Permission;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class XMLBuilder extends com.bagri.xdm.client.hazelcast.task.query.XMLBuilder {

	private transient XDMQueryManagement queryMgr;
    
    @Autowired
	public void setQueryManager(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}
	    
    @Autowired
	public void setRepository(RepositoryImpl repo) {
		this.repo = repo;
	}

    @Override
	public Collection<String> call() throws Exception {
    	((RepositoryImpl) repo).getXQProcessor(clientId);
    	checkPermission(Permission.Value.read);
   		return queryMgr.getContent(exp, template, params);
	}


}
