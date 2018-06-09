package com.bagri.core.api.impl;

import com.bagri.core.api.AccessManagement;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.HealthManagement;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.TransactionManagement;

/**
 * Base XDMRepository implementation. Just a common holder for internal management interfaces
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class SchemaRepositoryBase {
	
	private AccessManagement accessMgr;
	private DocumentManagement docMgr;
	private HealthManagement healthMgr;
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
	 * @return XDM Query Management implementation
	 */
	public QueryManagement getQueryManagement() {
		return queryMgr;
	}

	/**
	 * 
	 * @param queryMgr the XDM Query Management implementation
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

	//public ContentSerializer<?> getSerializer(String dataFormat) {
	//	return null;
	//}

	/**
	 * 
	 * @param clientId the client ID to set
	 */
	public void setClientId(String clientId) {
		// do nothing;
	}

}
