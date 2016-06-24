package com.bagri.xdm.common.query;

public class AlwaysExpression extends Expression {
	
	public AlwaysExpression(int clnId) {
		this(clnId, Comparison.EQ, null);
	}

	public AlwaysExpression(int clnId, Comparison compType, PathBuilder path) {
		super(clnId, compType, path);
	}

	public String getFullPath() {
		return null;
	}
	
	@Override
	public String toString() {
		return "AlwaysExpression [collectId=" + clnId + "]";
	}
	
}
