package com.bagri.xdm.domain;

import com.bagri.xdm.common.query.QueryBuilder;

/**
 * Represents cached compiled query 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMQuery implements Cloneable {
	
	private String query;
	private boolean readOnly;
	private QueryBuilder xdmQuery;
	
	/**
	 * default constructor
	 */
	public XDMQuery() {
		//
	}
	
	/**
	 * 
	 * @param query the query itself
	 * @param readOnly is query read-only or not
	 * @param xdmQuery the compiled query
	 */
	public XDMQuery(String query, boolean readOnly, QueryBuilder xdmQuery) {
		this.query = query;
		this.readOnly = readOnly;
		this.xdmQuery = xdmQuery;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public XDMQuery clone() {
		return new XDMQuery(query, readOnly, xdmQuery.clone());
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
		XDMQuery other = (XDMQuery) obj;
		return query.equals(other.query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMQuery [query=" + query + ", readOnly=" + readOnly + 
				", xdmQuery=" + xdmQuery + "]";
	}
	
	
}
