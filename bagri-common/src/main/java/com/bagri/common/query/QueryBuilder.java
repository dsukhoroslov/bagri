package com.bagri.common.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QueryBuilder {
	
	private Map<Integer, ExpressionContainer> containers = new HashMap<>();
	
	public QueryBuilder() {
		//
	}
	
	public QueryBuilder(Collection<ExpressionContainer> containers) {
		setContainers(containers);
	}
	
	public void addContainer(ExpressionContainer container) {
		int docType = container.getExpression().getRoot().getDocType();
		ExpressionContainer oldValue = containers.put(docType, container);
		//
	}
	
	public ExpressionContainer getContainer(int docType) {
		return containers.get(docType);
	}
	
	public Collection<ExpressionContainer> getContainers() {
		return containers.values();
	}
	
	public void setContainers(Collection<ExpressionContainer> containers) {
		this.containers.clear();
		if (containers != null) {
			for (ExpressionContainer ec: containers) {
				addContainer(ec);
			}
		}
	}
	
}
