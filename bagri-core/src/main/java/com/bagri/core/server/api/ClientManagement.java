package com.bagri.core.server.api;

import java.util.Properties;

/**
 * XDM Client Management Interface; Manages connected clients on the server side.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface ClientManagement {

	/**
	 * 
	 * @return all connected client UUIDs
	 */
	String[] getClients();
	
	/**
	 * 
	 * @return userName associated with the current thread's client
	 */
	String getCurrentUser();
	
	/**
	 * return {@link Properties} specified for the client UUID
	 * 
	 * @param clientId the client UUID identifier
	 * @return Properties associated with the client
	 */
	Properties getClientProperties(String clientId);
	
}
