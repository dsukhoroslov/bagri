package com.bagri.xdm.common.query;

import java.util.HashMap;
import java.util.Map;

public class ExpressionContainer implements Cloneable {
	
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
	
	@Override
	public ExpressionContainer clone() {
		return new ExpressionContainer(eBuilder, new HashMap<>(params));
	}

	public ExpressionBuilder getExpression() {
		return this.eBuilder;
	}
	
	public int addExpression(int clnId) {
		return eBuilder.addExpression(new AlwaysExpression(clnId));
	}
	
	public int addExpression(int clnId, Comparison compType, PathBuilder path) {
		return eBuilder.addExpression(clnId, compType, path, null);
	}
	
	public int addExpression(int clnId, Comparison compType, PathBuilder path, String param, Object value) {
		if (param == null) {
			param = "var" + params.size();
		}
		Object oldValue = params.put(param, value);
		return eBuilder.addExpression(clnId, compType, path, param);
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
	
	public void resetParams(Map<String, Object> params) {
		// do not clear existing params ?
		this.params.putAll(params);
	}

	@Override
	public String toString() {
		return "ExpressionContainer: [eBuilder=" + eBuilder + ", params=" + params + "]";
	}
	
	
}
