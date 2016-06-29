package com.bagri.xdm.api.impl;

import com.bagri.xdm.api.AccessManagement;
import com.bagri.xdm.api.BindingManagement;
import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.HealthManagement;
import com.bagri.xdm.api.ModelManagement;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.TransactionManagement;

/**
 * Base XDMRepository implementation. Just a common holder for internal management interfaces
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class RepositoryBase {
	
	private AccessManagement accessMgr;
	private BindingManagement bindMgr;
	private DocumentManagement docMgr;
	private HealthManagement healthMgr;
	private ModelManagement modelMgr;
	private QueryManagement queryMgr;
	private TransactionManagement txMgr;
	
	/**
	 * 
	 * @return XDM Access Management implementation
	 */
	public AccessManagement getAccessManagement() {
		return accessMgr;
	}
	
	/**
	 * 
	 * @param accessMgr the XDM Access Management implementation
	 */
	public void setAccessManagement(AccessManagement accessMgr) {
		this.accessMgr = accessMgr;
	}

	/**
	 * 
	 * @return XDM Binding Management implementation
	 */
	public BindingManagement getBindingManagement() {
		return bindMgr;
	}
	
	/**
	 * 
	 * @param bindMgr the XDM Binding Management implementation
	 */
	public void setBindingManagement(BindingManagement bindMgr) {
		this.bindMgr = bindMgr;
	}

	/**
	 * 
	 * @return XDM Document Management implementation
	 */
	public DocumentManagement getDocumentManagement() {
		return docMgr;
	}
	
	/**
	 * 
	 * @param docMgr  the XDM Document Management implementation
	 */
	public void setDocumentManagement(DocumentManagement docMgr) {
		this.docMgr = docMgr;
	}

	/**
	 * 
	 * @return XDM Health Management implementation
	 */
	public HealthManagement getHealthManagement() {
		return healthMgr;
	}
	
	/**
	 * 
	 * @param healthMgr  the XDM Health Management implementation
	 */
	public void setHealthManagement(HealthManagement healthMgr) {
		this.healthMgr = healthMgr;
	}

	/**
	 * 
	 * @return XDM Model Management implementation
	 */
	public ModelManagement getModelManagement() {
		return modelMgr;
	}

	/**
	 * 
	 * @param modelMgr  the XDM Model Management implementation
	 */
	public void setModelManagement(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}
	
	/**
	 * 
	 * @return XDM Query Management implementation
	 */
	public QueryManagement getQueryManagement() {
		return queryMgr;
	}

	/**
	 * 
	 * @param queryMgr  the XDM Query Management implementation
	 */
	public void setQueryManagement(QueryManagement queryMgr) {
		this.queryMgr = queryMgr;
	}

	/**
	 * 
	 * @return XDM Transaction Management implementation
	 */
	public TransactionManagement getTxManagement() {
		return txMgr;
	}

	/**
	 * 
	 * @param txMgr  the XDM Transaction Management implementation
	 */
	public void setTxManagement(TransactionManagement txMgr) {
		this.txMgr = txMgr;
	}


}
