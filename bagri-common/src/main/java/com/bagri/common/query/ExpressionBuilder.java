package com.bagri.common.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExpressionBuilder {

	private int exIndex = -1;
	private List<Expression> expressions = new ArrayList<Expression>();
	
	public int addExpression(Expression exp) {
		expressions.add(exp);
		exIndex = expressions.size() - 1;
		return resolveCurrentParent();
	}
	
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
	
	private Expression getCurrentExpression() {
		return getExpression(exIndex);
	}
	
	public Expression getExpression(int exIdx) {
		if (exIdx < 0) {
			return null;
		}
		if (exIdx >= expressions.size()) {
			return null;
		}
		return expressions.get(exIdx);
	}
	
	public List<Expression> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

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
	
	//public void setCurrentExpression(int idx) {
	//	if (idx >= 0 && idx < expressions.size()) {
	//		exIndex = idx;
	//	}
	//}
	
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
