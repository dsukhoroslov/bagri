package com.bagri.xdm.api.impl;

import com.bagri.xdm.api.XDMAccessManagement;
import com.bagri.xdm.api.XDMBindingManagement;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMHealthManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMTransactionManagement;

/**
 * Base XDMRepository implementation. Just a common holder for internal management interfaces
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class RepositoryBase {
	
	private XDMAccessManagement accessMgr;
	private XDMBindingManagement bindMgr;
	private XDMDocumentManagement docMgr;
	private XDMHealthManagement healthMgr;
	private XDMModelManagement modelMgr;
	private XDMQueryManagement queryMgr;
	private XDMTransactionManagement txMgr;
	
	/**
	 * 
	 * @return XDM Access Management implementation
	 */
	public XDMAccessManagement getAccessManagement() {
		return accessMgr;
	}
	
	/**
	 * 
	 * @param accessMgr the XDM Access Management implementation
	 */
	public void setAccessManagement(XDMAccessManagement accessMgr) {
		this.accessMgr = accessMgr;
	}

	/**
	 * 
	 * @return XDM Binding Management implementation
	 */
	public XDMBindingManagement getBindingManagement() {
		return bindMgr;
	}
	
	/**
	 * 
	 * @param bindMgr the XDM Binding Management implementation
	 */
	public void setBindingManagement(XDMBindingManagement bindMgr) {
		this.bindMgr = bindMgr;
	}

	/**
	 * 
	 * @return XDM Document Management implementation
	 */
	public XDMDocumentManagement getDocumentManagement() {
		return docMgr;
	}
	
	/**
	 * 
	 * @param docMgr  the XDM Document Management implementation
	 */
	public void setDocumentManagement(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}

	/**
	 * 
	 * @return XDM Health Management implementation
	 */
	public XDMHealthManagement getHealthManagement() {
		return healthMgr;
	}
	
	/**
	 * 
	 * @param healthMgr  the XDM Health Management implementation
	 */
	public void setHealthManagement(XDMHealthManagement healthMgr) {
		this.healthMgr = healthMgr;
	}

	/**
	 * 
	 * @return XDM Model Management implementation
	 */
	public XDMModelManagement getModelManagement() {
		return modelMgr;
	}

	/**
	 * 
	 * @param modelMgr  the XDM Model Management implementation
	 */
	public void setModelManagement(XDMModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}
	
	/**
	 * 
	 * @return XDM Query Management implementation
	 */
	public XDMQueryManagement getQueryManagement() {
		return queryMgr;
	}

	/**
	 * 
	 * @param queryMgr  the XDM Query Management implementation
	 */
	public void setQueryManagement(XDMQueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}

	/**
	 * 
	 * @return XDM Transaction Management implementation
	 */
	public XDMTransactionManagement getTxManagement() {
		return txMgr;
	}

	/**
	 * 
	 * @param txMgr  the XDM Transaction Management implementation
	 */
	public void setTxManagement(XDMTransactionManagement txMgr) {
		this.txMgr = txMgr;
	}


}
