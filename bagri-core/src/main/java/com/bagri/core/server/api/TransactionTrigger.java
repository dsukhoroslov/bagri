package com.bagri.core.server.api;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Transaction;

public interface TransactionTrigger {

	/**
	 * fires before transaction started in repository
	 * 
	 * @param tx the XDM transaction
	 * @param repo the XDM Schema Repository
	 * @throws BagriException in case of processing error
	 */
	void beforeBegin(Transaction tx, SchemaRepository repo) throws BagriException;
	
	/**
	 * fires after transaction started in repository
	 * 
	 * @param tx the XDM transaction
	 * @param repo the XDM Schema Repository
	 * @throws BagriException in case of processing error
	 */
	void afterBegin(Transaction tx, SchemaRepository repo) throws BagriException;
	
	/**
	 * fires transaction committed in repository
	 * 
	 * @param tx the XDM transaction
	 * @param repo the XDM Schema Repository
	 * @throws BagriException in case of processing error
	 */
	void beforeCommit(Transaction tx, SchemaRepository repo) throws BagriException;
	
	/**
	 * fires after transaction committed in repository
	 * 
	 * @param tx the XDM transaction
	 * @param repo the XDM Schema Repository
	 * @throws BagriException in case of processing error
	 */
	void afterCommit(Transaction tx, SchemaRepository repo) throws BagriException;

	/**
	 * fires before transaction rolled back in repository
	 * 
	 * @param tx the XDM transaction
	 * @param repo the XDM Schema Repository
	 * @throws BagriException in case of processing error
	 */
	void beforeRollback(Transaction tx, SchemaRepository repo) throws BagriException;

	/**
	 * fires after transaction rolled back in repository
	 * 
	 * @param tx the XDM transaction
	 * @param repo the XDM Schema Repository
	 * @throws BagriException in case of processing error
	 */
	void afterRollback(Transaction tx, SchemaRepository repo) throws BagriException;
	
}
