package com.bagri.core.api;

import com.bagri.core.server.api.ContentParser;

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
	 * @return document management interface
	 */
	DocumentManagement getDocumentManagement();

	/**
	 * @return health management interface
	 */
	HealthManagement getHealthManagement();
	
	/**
	 * @return query management interface
	 */
	QueryManagement getQueryManagement();
	
	/**
	 * @return transaction management interface
	 */
	TransactionManagement getTxManagement();
	
	/**
	 * 
	 * @param dataFormat the name of dataFormat to search for
	 * @return ContentSerializer instance associated with the dataFormat name 
	 */
	ContentSerializer<?> getSerializer(String dataFormat);
	
}
