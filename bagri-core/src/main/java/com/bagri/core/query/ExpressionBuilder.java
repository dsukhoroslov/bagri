package com.bagri.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Build expressions of right type from parameters provided.
 * Contains expressions belonging to the same collection only?
 * 
 * @author Denis Sukhoroslov
 *
 */
public class ExpressionBuilder {

	private int exIndex = -1;
	private List<Expression> expressions = new ArrayList<>();
	
	/**
	 * Add expression to internal expression list
	 * 
	 * @param exp the expression
	 * @return the index at which expression is stored in internal expression list
	 */
	public int addExpression(Expression exp) {
		expressions.add(exp);
		exIndex = expressions.size() - 1;
		return resolveCurrentParent();
	}
	
	/**
	 * Build expression and add it to internal expression list
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 * @param param the parameter name
	 * @return the index at which expression is stored in internal expression list
	 */
	public int addExpression(int clnId, Comparison compType, PathBuilder path, String param) {
		Expression ex;
		switch (compType) {
			case AND:
			case OR:
				ex = new BinaryExpression(clnId, compType, path);
				break;
			case NOT:
				return -1;
			default:
				ex = new PathExpression(clnId, compType, path, param);
		}
		return addExpression(ex);
	}
	
	/**
	 * 
	 * @return the last added expression
	 */
	private Expression getCurrentExpression() {
		return getExpression(exIndex);
	}
	
	/**
	 * 
	 * @param exIdx the expression index
	 * @return the expression if it is found by the index provided, null otherwise
	 */
	public Expression getExpression(int exIdx) {
		if (exIdx < 0) {
			return null;
		}
		if (exIdx >= expressions.size()) {
			return null;
		}
		return expressions.get(exIdx);
	}
	
	/**
	 * 
	 * @return the list of containing expressions 
	 */
	public List<Expression> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

	/**
	 * 
	 * @return root expression
	 */
	public Expression getRoot() {
		if (expressions.size() > 0) {
			return expressions.get(0);
		}
		return null;
	}
	
	private int resolveCurrentParent() {
		Expression current = getCurrentExpression();
		for (int i=exIndex - 1; i >= 0; i--) {
			Expression ex = expressions.get(i);
			if (ex instanceof BinaryExpression) {
				BinaryExpression be = (BinaryExpression) ex;
				if (be.getLeft() == null) {
					be.setLeft(current);
					return i;
				} else if (be.getRight() == null) {
					be.setRight(current);
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		buff.append("ExpressionBuilder; size: ").append(expressions.size());
		if (expressions.size() > 0) {
			buff.append(" [");
			buff.append(expressions.get(0));
			buff.append("]");
		}
		return buff.toString();
	}
	
}
