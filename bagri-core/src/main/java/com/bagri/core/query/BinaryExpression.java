package com.bagri.core.query;

/**
 * Represents an expression between two other sub-expressions ANDed or ORed.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class BinaryExpression extends Expression {

	private Expression left;
	private Expression right;

	/**
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 */
	public BinaryExpression(int clnId, Comparison compType, PathBuilder path) {
		super(clnId, compType, path);
	}
	
	/**
	 * 
	 * @return left sub-expression
	 */
	public Expression getLeft() {
		return this.left;
	}
	
	/**
	 * 
	 * @param left the left sub-expression
	 */
	public void setLeft(Expression left) {
		this.left = left;
	}
	
	/**
	 * 
	 * @return right sub-expression
	 */
	public Expression getRight() {
		return this.right;
	}
	
	/**
	 * 
	 * @param right the right sub-expression
	 */
	public void setRight(Expression right) {
		this.right = right;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "BinaryExpression [collectId=" + clnId + ", compType=" + compType + 
				", path=" + path + ", left=" + left + ", right=" + right + "]";
	}

	
}
