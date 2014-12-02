package com.bagri.common.query;

import java.util.HashMap;
import java.util.Map;

public class ExpressionContainer {
	
	private ExpressionBuilder eBuilder;
	private Map<String, Object> params;
	
	public ExpressionContainer() {
		eBuilder = new ExpressionBuilder();
		params = new HashMap<String, Object>();
	}
	
	public ExpressionContainer(ExpressionBuilder eBuilder, Map<String, Object> params) {
		this.eBuilder = eBuilder;
		this.params = params;
	}

	public ExpressionBuilder getExpression() {
		return this.eBuilder;
	}
	
	public int addExpression(int docType, Comparison compType, PathBuilder path) {
		return eBuilder.addExpression(docType, compType, path, null);
	}
	
	public int addExpression(int docType, Comparison compType, PathBuilder path, String param, Object value) {
		if (param == null) {
			param = "var" + params.size();
		}
		Object oldValue = params.put(param, value);
		return eBuilder.addExpression(docType, compType, path, param);
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
	
	public Object getParam(String paramName) {
		return params.get(paramName);
	}
	
	public Object getParam(PathExpression pex) {
		return params.get(pex.getParamName());
	}

	@Override
	public String toString() {
		return super.toString() + "; [eBuilder=" + eBuilder + ", params=" + params + "]";
	}
	
	
}
