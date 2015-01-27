package com.bagri.xdm.client.hazelcast.impl;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;

public class RepositoryImpl implements XDMRepository {
	
	private XDMDocumentManagement docMgr;
	private XDMModelManagement modelMgr;
	private XDMQueryManagement queryMgr;
	
	

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public XDMDocumentManagement getDocumentManagement() {
		return docMgr;
	}
	
	public void setDocumentManagement(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}

	@Override
	public XDMQueryManagement getQueryManagement() {
		return queryMgr;
	}

	public void setDocumentManagement(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}

	@Override
	public XDMModelManagement getModelManagement() {
		return modelMgr;
	}

	public void setModelManagement(XDMModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

}
