package com.bagri.xdm.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryBuilder implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
	
	private Map<Integer, ExpressionContainer> containers = new HashMap<>();
	
	public QueryBuilder() {
		//
	}
	
	public QueryBuilder(Collection<ExpressionContainer> containers) {
		setContainers(containers);
	}
	
	@Override
	public QueryBuilder clone() {
		return new QueryBuilder(containers.values());
	}
	
	public void addContainer(ExpressionContainer container) {
		int clnId = container.getExpression().getRoot().getCollectionId();
		addContainer(clnId, container);
	}
	
	public void addContainer(int clnId, ExpressionContainer container) {
		ExpressionContainer oldValue = containers.put(clnId, container);
		//
	}

	public ExpressionContainer getContainer(int clnId) {
		return containers.get(clnId);
	}
	
	public Collection<ExpressionContainer> getContainers() {
		return containers.values();
	}
	
	public void setContainers(Collection<ExpressionContainer> containers) {
		this.containers.clear();
		if (containers != null) {
			for (ExpressionContainer ec: containers) {
				addContainer(ec.clone());
			}
		}
	}
	
	public Collection<String> getParamNames() {
		List<String> result = new ArrayList<>();
		for (ExpressionContainer exCont: containers.values()) {
			for (Map.Entry<String, Object> param: exCont.getParams().entrySet()) {
				result.add(param.getKey());
			}
		}
		return result;
	}
	
	public Collection<String> getEmptyParams() {
		List<String> result = new ArrayList<>();
		for (ExpressionContainer exCont: containers.values()) {
			for (Map.Entry<String, Object> param: exCont.getParams().entrySet()) {
				if (param.getValue() == null) {
					result.add(param.getKey());
				}
			}
		}
		return result;
	}
	
	public boolean hasEmptyParams() {
		for (ExpressionContainer exCont: containers.values()) {
			for (Object value: exCont.getParams().values()) {
				if (value == null) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void setEmptyParam(String pName, Object value) {
		for (ExpressionContainer exCont: containers.values()) {
			if (exCont.getParams().containsKey(pName) && exCont.getParam(pName) == null) {
				exCont.getParams().put(pName, value);
			}
		}		
	}
	
	public void resetParams(Map<String, Object> params) {
		logger.trace("resetParams; this: {}; got params: {}", this, params);
		for (ExpressionContainer exCont: containers.values()) {
			exCont.resetParams(params);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " [" + containers + "]";
	}
	
}
