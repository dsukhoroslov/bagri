package com.bagri.xdm.query;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for expressions and their parameters. All container expressions belongs to the same collection.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class ExpressionContainer implements Cloneable {
	
	private ExpressionBuilder eBuilder;
	private Map<String, Object> params;
	
	/**
	 * default constructor
	 */
	public ExpressionContainer() {
		eBuilder = new ExpressionBuilder();
		params = new HashMap<String, Object>();
	}
	
	/**
	 * 
	 * @param eBuilder the expression builder
	 * @param params the parameters map
	 */
	public ExpressionContainer(ExpressionBuilder eBuilder, Map<String, Object> params) {
		this.eBuilder = eBuilder;
		this.params = params;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpressionContainer clone() {
		return new ExpressionContainer(eBuilder, new HashMap<>(params));
	}

	/**
	 * 
	 * @return the expression builder
	 */
	public ExpressionBuilder getBuilder() {
		return this.eBuilder;
	}
	
	/**
	 * Creates a new AlwaysExpression 
	 * 
	 * @param clnId the collection identifier
	 * @return index at which the expression stored in the builder's internal list
	 */
	public int addExpression(int clnId) {
		return eBuilder.addExpression(new AlwaysExpression(clnId));
	}
	
	/**
	 * Delegates to builder to create new expression from the parameters provided
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 * @return index at which the expression stored in the builder's internal list
	 */
	public int addExpression(int clnId, Comparison compType, PathBuilder path) {
		return eBuilder.addExpression(clnId, compType, path, null);
	}
	
	/**
	 * Delegates to builder to create new expression from the parameters provided
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 * @param param the parameter name
	 * @param value the parameter value
	 * @return index at which the expression stored in the builder's internal list
	 */
	public int addExpression(int clnId, Comparison compType, PathBuilder path, String param, Object value) {
		if (param == null) {
			param = "var" + params.size();
		}
		Object oldValue = params.put(param, value);
		return eBuilder.addExpression(clnId, compType, path, param);
	}
	
	/**
	 * 
	 * @return the parameters map
	 */
	public Map<String, Object> getParams() {
		return params;
	}
	
	/**
	 * 
	 * @param paramName the parameter name
	 * @return the parameter value if it is found in internal parameters map, null otherwise
	 */
	public Object getParam(String paramName) {
		return params.get(paramName);
	}
	
	/**
	 * Search for parameter value stored for the provided path expression   
	 * 
	 * @param pex the path expression
	 * @return the parameter value if it is found in internal parameters map, null otherwise
	 */
	public Object getParam(PathExpression pex) {
		return params.get(pex.getParamName());
	}
	
	/**
	 * Combines existing parameters with provided ones
	 * 
	 * @param params the new parameters provided
	 */
	public void resetParams(Map<String, Object> params) {
		// do not clear existing params ?
		this.params.putAll(params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ExpressionContainer: [eBuilder=" + eBuilder + ", params=" + params + "]";
	}
	
	
}
