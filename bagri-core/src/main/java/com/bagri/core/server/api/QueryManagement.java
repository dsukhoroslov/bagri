package com.bagri.core.server.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Query;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.QueryBuilder;

/**
 * Query Management server-side extension; Adds methods to be used from {@link com.bagri.core.xquery.api.XQProcessor} in generic way.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface QueryManagement extends com.bagri.core.api.QueryManagement {
	
	/**
	 * collect document identifiers witch match the query provided
	 * 
	 * @param query container with internal compiled query representation
	 * @return the collection of internal document identifiers matched by query
	 * @throws BagriException in case of any error
	 */
	Collection<Long> getDocumentIds(ExpressionContainer query) throws BagriException;

	/**
	 * build text content by applying {@literal template} with {@literal params} on documents found by {@literal query}  
	 * 
	 * @param query container with internal compiled query representation
	 * @param template the String template to apply on the found documents
	 * @param params the 0{@link Map} of parameters associated with template 
	 * @return collection of strings produced by the system. The number of returned strings matches the number of documents found by query 
	 * @throws BagriException in case of any error
	 */
	Collection<String> getContent(ExpressionContainer query, String template, Map<String, Object> params) throws BagriException;
	
	/**
	 * check if the {@literal query} read-only or not. To do this the system looks query in internal cache. 
	 * If the query is not cached yet the system assumes it is read-write for the first time.
	 * 
	 * @param query the plain text query representation
	 * @param props the query processing instructions. Supported values are: ...
	 * @return true if query is read-only, false otherwise
	 * @throws BagriException in case of query check error
	 */
	boolean isQueryReadOnly(String query, Properties props) throws BagriException;
	
	/**
	 * looks for internal cached query representation identified by the query text provided  
	 * 
	 * @param query the plain text query representation
	 * @return internal query representation or null if not found
	 */
	Query getQuery(String query); 
	
	/**
	 * adds internal compiled query representation into query cache
	 * 
	 * @param query the plain text query representation
	 * @param readOnly specifies is query read-only or not (read-write)
	 * @param xdmQuery the internal query representation to cache
	 * @return true if the query was inserted into cache and false if it was updated in cache
	 */
	boolean addQuery(String query, boolean readOnly, QueryBuilder xdmQuery);

	/**
	 * clears internal query and query results caches
	 */
	void clearCache();

}
