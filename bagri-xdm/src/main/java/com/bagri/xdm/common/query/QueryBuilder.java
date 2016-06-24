package com.bagri.xdm.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents (X-)Query containing query expressions and parameters.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class QueryBuilder implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
	
	private Map<Integer, ExpressionContainer> containers = new HashMap<>();
	
	/**
	 * default constructor
	 */
	public QueryBuilder() {
		//
	}
	
	/**
	 * 
	 * @param containers the collection of all expressions constructing query 
	 */
	public QueryBuilder(Collection<ExpressionContainer> containers) {
		setContainers(containers);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public QueryBuilder clone() {
		return new QueryBuilder(containers.values());
	}
	
	/**
	 * Adds new expression container to the internal containers map
	 * 
	 * @param container the expression container 
	 */
	public void addContainer(ExpressionContainer container) {
		int clnId = container.getBuilder().getRoot().getCollectionId();
		addContainer(clnId, container);
	}
	
	/**
	 * Associates expression container with the collection identifier provided
	 * 
	 * @param clnId the collection identifier
	 * @param container the expression container
	 */
	public void addContainer(int clnId, ExpressionContainer container) {
		ExpressionContainer oldValue = containers.put(clnId, container);
		// ??
	}

	/**
	 * 
	 * @param clnId the collection identifier
	 * @return expression container for the specified collection identifier
	 */
	public ExpressionContainer getContainer(int clnId) {
		return containers.get(clnId);
	}
	
	/**
	 * 
	 * @return the internally stored expression containers
	 */
	public Collection<ExpressionContainer> getContainers() {
		return containers.values();
	}
	
	/**
	 * 
	 * @param containers the expression containers to store
	 */
	public void setContainers(Collection<ExpressionContainer> containers) {
		this.containers.clear();
		if (containers != null) {
			for (ExpressionContainer ec: containers) {
				addContainer(ec.clone());
			}
		}
	}
	
	/**
	 * 
	 * @return all parameter names used in query expressions
	 */
	public Collection<String> getParamNames() {
		List<String> result = new ArrayList<>();
		for (ExpressionContainer exCont: containers.values()) {
			for (Map.Entry<String, Object> param: exCont.getParams().entrySet()) {
				result.add(param.getKey());
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return all parameter names which has no bound parameter value
	 */
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
	
	/**
	 * 
	 * @return true if query has any unbound parameter 
	 */
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
	
	/**
	 * Bind parameter if it has no value yet
	 * 
	 * @param pName th parameter name
	 * @param value the parameter value
	 */
	public void setEmptyParam(String pName, Object value) {
		for (ExpressionContainer exCont: containers.values()) {
			if (exCont.getParams().containsKey(pName) && exCont.getParam(pName) == null) {
				exCont.getParams().put(pName, value);
			}
		}		
	}
	
	/**
	 * reset parameters in all underlying query expressions 
	 * 
	 * @param params the parameters to use
	 */
	public void resetParams(Map<String, Object> params) {
		logger.trace("resetParams; this: {}; got params: {}", this, params);
		for (ExpressionContainer exCont: containers.values()) {
			exCont.resetParams(params);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + " [" + containers + "]";
	}
	
}
