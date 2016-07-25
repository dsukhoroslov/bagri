package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents cache query results
 * 
 * @author Denis Sukhoroslov
 *
 */
public class QueryResult {
	
	private Map<String, Object> params;
	private Map<Long, String> docKeys;
	private List<Object> results;
	
	/**
	 * default constructor
	 */
	public QueryResult() {
		//
	}
	
	/**
	 * 
	 * @param params the query parameters
	 * @param docKeys the query resulting document id/uri pairs
	 * @param results the query results
	 */
	public QueryResult(Map<String, Object> params, Map<Long, String> docKeys, List<Object> results) {
		//super();
		this.params = params;
		this.docKeys = docKeys;
		this.results = results;
	}

	/**
	 * 
	 * @return the query parameters
	 */
	public Map<String, Object> getParams() {
		return params;
	}

	/**
	 * 
	 * @return the query resulting document id/uri pairs
	 */
	public Map<Long, String> getDocKeys() {
		return docKeys;
	}
	
	/**
	 * 
	 * @return the query resulting document ids
	 */
	public Collection<Long> getDocIds() {
		return docKeys.keySet();
	}
	
	/**
	 * 
	 * @return the query resulting document uris
	 */
	public Collection<String> getDocUris() {
		return docKeys.values();
	}
	
	/**
	 * 
	 * @return the query results
	 */
	public List<Object> getResults() {
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "QueryResult [params=" + params + ", docKeys=" + docKeys + ", results=" + results + "]";
	}
	

}
