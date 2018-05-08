package com.bagri.core.api;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * XDM query management interface; provided for the client side
 * 
 * @author Denis Sukhoroslov
 */
public interface QueryManagement {
	
	/**
	 * executes (X-)query on the current XDM SChema. Returns cursor over resulting Java objects
	 * 
	 * @param query the query specified as a plain text.  
	 * @param params the map of parameter name/value pairs. Parameters are bound in query by names 
	 * @param props the query processing instructions. Supported values are: ...
	 * @param <T> the type of ResultCursor content
	 * @return wrapping cursor over resulting data sequence 
	 * @throws BagriException in case of any query processing error
	 */
	<T> ResultCursor<T> executeQuery(String query, Map<String, Object> params, Properties props) throws BagriException;

	/**
	 * 
	 * @param query the query specified as a plain text. 
	 * @param params the map of parameter name/value pairs. Parameters are bound in query by names
	 * @param props the query processing instructions. Supported values are: ...
	 * @return collection of document uris found by query
	 * @throws BagriException in case of any query processing error
	 */
	ResultCursor<String> getDocumentUris(String query, Map<String, Object> params, Properties props) throws BagriException;
	
	/**
	 * cancels currently executing query
	 * 
	 * @throws BagriException in case of any error 
	 */
	void cancelExecution() throws BagriException;
	
	/**
	 * parses query and return back all parameter names found 
	 * 
	 * @param query the query specified as a plain text.
	 * @return collection of query parameters
	 */
	Collection<String> prepareQuery(String query); //throws XDMException;
	
	/**
	 * 
	 * @param query the query specified as a plain text.
	 * @return Integer query key 
	 */
	int getQueryKey(String query); 
	
}
