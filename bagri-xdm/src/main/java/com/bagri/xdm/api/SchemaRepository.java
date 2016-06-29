package com.bagri.xdm.api;

/**
 * XDM repository interface, provided for the client side. The main entry point to work with XDM Schema from client side.
 * 
 * @author Denis Sukhoroslov
 */
public interface SchemaRepository {
	
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
	AccessManagement getAccessManagement();
	
	/**
	 * @return binding management interface
	 */
	BindingManagement getBindingManagement();
	
	/**
	 * @return document management interface
	 */
	DocumentManagement getDocumentManagement();

	/**
	 * @return health management interface
	 */
	HealthManagement getHealthManagement();
	
	/**
	 * @return meta-data management interface
	 */
	ModelManagement getModelManagement();

	/**
	 * @return query management interface
	 */
	QueryManagement getQueryManagement();
	
	/**
	 * @return transaction management interface
	 */
	TransactionManagement getTxManagement();
	
}
