package com.bagri.xdm.api;

/**
 * XDM repository interface, provided for the client side. The main entry point to work with XDM Schema from client side.
 * 
 * @author Denis Sukhoroslov
 */
public interface XDMRepository {
	
	/**
	 * close connection with Bagri cluster
	 */
	void close();
	
	/**
	 * @return client id
	 */
	String getClientId();

	/**
	 * @return user name
	 */
	String getUserName();
	
	/**
	 * @return access management interface
	 */
	XDMAccessManagement getAccessManagement();
	
	/**
	 * @return binding management interface
	 */
	XDMBindingManagement getBindingManagement();
	
	/**
	 * @return document management interface
	 */
	XDMDocumentManagement getDocumentManagement();

	/**
	 * @return health management interface
	 */
	XDMHealthManagement getHealthManagement();
	
	/**
	 * @return meta-data management interface
	 */
	XDMModelManagement getModelManagement();

	/**
	 * @return query management interface
	 */
	XDMQueryManagement getQueryManagement();
	
	/**
	 * @return transaction management interface
	 */
	XDMTransactionManagement getTxManagement();
	
}
