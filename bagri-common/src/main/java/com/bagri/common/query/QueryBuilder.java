package com.bagri.common.query;

import java.util.HashMap;
import java.util.Map;

public class QueryBuilder {
	
	private Map<Integer, ExpressionContainer> containers = new HashMap<>();
	
	public QueryBuilder() {
		//
	}
	
	public void addContainer(ExpressionContainer container) {
		int docType = container.getExpression().getRoot().getDocType();
		ExpressionContainer oldValue = containers.put(docType, container);
		//
	}
	
	public ExpressionContainer getContainer(int docId) {
		return containers.get(docId);
	}
	
	public Map<Integer, ExpressionContainer> getContainers() {
		return containers;
	}

}
