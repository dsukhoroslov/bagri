package com.bagri.xdm.domain;

import com.bagri.xdm.query.QueryBuilder;

/**
 * Represents cached compiled query 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class Query implements Cloneable {
	
	private String query;
	private boolean readOnly;
	private QueryBuilder xdmQuery;
	
	/**
	 * default constructor
	 */
	public Query() {
		//
	}
	
	/**
	 * 
	 * @param query the query itself
	 * @param readOnly is query read-only or not
	 * @param xdmQuery the compiled query
	 */
	public Query(String query, boolean readOnly, QueryBuilder xdmQuery) {
		this.query = query;
		this.readOnly = readOnly;
		this.xdmQuery = xdmQuery;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query clone() {
		return new Query(query, readOnly, xdmQuery.clone());
	}

	/**
	 * 
	 * @return the plain text query
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * 
	 * @return is query read-only or not
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * 
	 * @return the compiled query
	 */
	public QueryBuilder getXdmQuery() {
		return xdmQuery;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return query.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Query other = (Query) obj;
		return query.equals(other.query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Query [query=" + query + ", readOnly=" + readOnly + 
				", xdmQuery=" + xdmQuery + "]";
	}
	
	
}
