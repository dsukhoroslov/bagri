package com.bagri.xdm.client.common.impl;

import com.bagri.xdm.api.XDMBindingManagement;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMHealthManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMTransactionManagement;

public class XDMRepositoryBase {
	
	private XDMBindingManagement bindMgr;
	private XDMDocumentManagement docMgr;
	private XDMHealthManagement healthMgr;
	private XDMModelManagement modelMgr;
	private XDMQueryManagement queryMgr;
	private XDMTransactionManagement txMgr;
	
	public XDMBindingManagement getBindingManagement() {
		return bindMgr;
	}
	
	public void setBindingManagement(XDMBindingManagement bindMgr) {
		this.bindMgr = bindMgr;
	}

	public XDMDocumentManagement getDocumentManagement() {
		return docMgr;
	}
	
	public void setDocumentManagement(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}

	public XDMHealthManagement getHealthManagement() {
		return healthMgr;
	}
	
	public void setHealthManagement(XDMHealthManagement healthMgr) {
		this.healthMgr = healthMgr;
	}

	public XDMModelManagement getModelManagement() {
		return modelMgr;
	}

	public void setModelManagement(XDMModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}
	
	public XDMQueryManagement getQueryManagement() {
		return queryMgr;
	}

	public void setQueryManagement(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}

	public XDMTransactionManagement getTxManagement() {
		return txMgr;
	}

	public void setTxManagement(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
	}


}
