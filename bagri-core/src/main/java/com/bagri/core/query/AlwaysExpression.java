package com.bagri.core.query;

/**
 * Represents an expression which always produce true result.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class AlwaysExpression extends Expression {
	
	/**
	 * 
	 * @param clnId the collection identifier
	 */
	public AlwaysExpression(int clnId) {
		this(clnId, Comparison.EQ, null);
	}

	/**
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 */
	public AlwaysExpression(int clnId, Comparison compType, PathBuilder path) {
		super(clnId, compType, path);
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public String getFullPath() {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AlwaysExpression [collectId=" + clnId + "]";
	}
	
}
