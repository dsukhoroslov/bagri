package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Represents cache query results
 * 
 * @author Denis Sukhoroslov
 *
 */
public class QueryResult {
	
	private Map<QName, Object> params;
	private Collection<Long> docIds;
	private Collection<Object> results;
	
	/**
	 * default constructor
	 */
	public QueryResult() {
		//
	}
	
	/**
	 * 
	 * @param params the query parameters
	 * @param docIds the query resulting document ids
	 * @param results the query results
	 */
	public QueryResult(Map<QName, Object> params, Collection<Long> docIds, Collection<Object> results) {
		//super();
		this.params = params;
		this.docIds = docIds;
		this.results = results;
	}

	/**
	 * 
	 * @return the query parameters
	 */
	public Map<QName, Object> getParams() {
		return params;
	}

	/**
	 * 
	 * @return the query resulting document ids
	 */
	public Collection<Long> getDocIds() {
		return docIds;
	}
	
	/**
	 * 
	 * @return the query results
	 */
	public Collection<Object> getResults() {
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "QueryResult [params=" + params + ", docIds=" + docIds + ", results=" + results + "]";
	}
	

}
