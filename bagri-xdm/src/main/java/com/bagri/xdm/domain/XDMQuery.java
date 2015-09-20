package com.bagri.xdm.domain;

import com.bagri.common.query.QueryBuilder;

public class XDMQuery implements Cloneable {
	
	private String query;
	private boolean readOnly;
	private QueryBuilder xdmQuery;
	
	public XDMQuery() {
		//
	}
	
	public XDMQuery(String query, boolean readOnly, QueryBuilder xdmQuery) {
		this.query = query;
		this.readOnly = readOnly;
		this.xdmQuery = xdmQuery;
	}
	
	@Override
	public XDMQuery clone() {
		return new XDMQuery(query, readOnly, xdmQuery.clone());
	}

	public String getQuery() {
		return query;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}

	public QueryBuilder getXdmQuery() {
		return xdmQuery;
	}

	@Override
	public int hashCode() {
		return query.hashCode();
	}

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

	@Override
	public String toString() {
		return "XDMQuery [query=" + query + ", readOnly=" + readOnly + 
				", xdmQuery=" + xdmQuery + "]";
	}
	
	
}
