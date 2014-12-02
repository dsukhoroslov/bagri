package com.bagri.xdm.domain;

import com.bagri.common.query.ExpressionBuilder;

public class XDMQuery {
	
	private String query;
	private Object xqExpression;
	private ExpressionBuilder xdmExpression;
	
	public XDMQuery() {
		//
	}
	
	public XDMQuery(String query, Object xqExpression, ExpressionBuilder xdmExpression) {
		super();
		this.query = query;
		this.xqExpression = xqExpression;
		this.xdmExpression = xdmExpression;
	}

	public String getQuery() {
		return query;
	}

	public Object getXqExpression() {
		return xqExpression;
	}

	public ExpressionBuilder getXdmExpression() {
		return xdmExpression;
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
				+ ", xdmExpression=" + xdmExpression + "]";
	}
	
	
}
