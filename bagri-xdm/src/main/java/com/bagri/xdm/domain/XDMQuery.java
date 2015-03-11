package com.bagri.xdm.domain;

import com.bagri.common.query.QueryBuilder;

public class XDMQuery {
	
	private String query;
	private Object xqExpression;
	private QueryBuilder xdmQuery;
	
	public XDMQuery() {
		//
	}
	
	public XDMQuery(String query, Object xqExpression, QueryBuilder xdmQuery) {
		super();
		this.query = query;
		this.xqExpression = xqExpression;
		this.xdmQuery = xdmQuery;
	}

	public String getQuery() {
		return query;
	}

	public Object getXqExpression() {
		return xqExpression;
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
		return "XDMQuery [query=" + query + ", xqExpression=" + xqExpression
				+ ", xdmQuery=" + xdmQuery + "]";
	}
	
	
}
