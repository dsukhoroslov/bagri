package com.bagri.xdm.cache.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMQuery;

/**
 * XDM Query Management server-side extension; Adds methods to be used from {@link com.bagri.xquery.api.XQProcessor} in generic way.
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XDMQueryManagement extends com.bagri.xdm.api.XDMQueryManagement {
	
	/**
	 * collect document identifiers witch match the query provided
	 * 
	 * @param query container with internal compiled query representation
	 * @return the collection of internal document identifiers matched by query
	 * @throws XDMException in case of any error
	 */
	Collection<Long> getDocumentIds(ExpressionContainer query) throws XDMException;

	/**
	 * build text content by applying {@literal template} with {@literal params} on documents found by {@literal query}  
	 * 
	 * @param query container with internal compiled query representation
	 * @param template the String template to apply on the found documents
	 * @param params the 0{@link Map} of parameters associated with template 
	 * @return collection of strings produced by the system. The number of returned strings matches the number of documents found by query 
	 * @throws XDMException in case of any error
	 */
	Collection<String> getContent(ExpressionContainer query, String template, Map params) throws XDMException;
	
	/**
	 * check if the {@literal query} read-only or not. To do this the system looks query in internal cache. 
	 * If the query is not cached yet the system assumes it is read-write for the first time.
	 * 
	 * @param query the plain text query representation
	 * @return true if query is read-only, false otherwise
	 */
	boolean isReadOnlyQuery(String query);
	
	/**
	 * looks for internal cached query representation identified by the query text provided  
	 * 
	 * @param query the plain text query representation
	 * @return internal query representation or null if not found
	 */
	XDMQuery getQuery(String query); 
	
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
	 * looks for cached query results associated with the {@literal query} {@literal params} and query processing {@literal props} provided.
	 * 
	 * @param query the plain text query representation
	 * @param params the map of parameter name/value pairs. Parameters are bound in query by names
	 * @param props the query processing instructions. Supported values are: ...
	 * @return the {@link Iterator} over found query results or null if not found
	 */
	Iterator<?> getQueryResults(String query, Map<QName, Object> params, Properties props);
	
	/**
	 * adds query {@literal results} into internal cache for the {@literal query} {@literal params} and query processing {@literal props} specified.
	 * 
	 * @param query the plain text query representation
	 * @param params the map of parameter name/value pairs. Parameters are bound in query by names
	 * @param props the query processing instructions. Supported values are: ...
	 * @param results the generic {@link Iterator} over query results
	 * @return the {@link Iterator} over query results 
	 */
	Iterator<?> addQueryResults(String query, Map<QName, Object> params, Properties props, Iterator<?> results);
	
	/**
	 * clears internal query and query results caches
	 */
	void clearCache();

}
