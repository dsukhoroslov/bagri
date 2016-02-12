package com.bagri.common.query;

import java.util.Collection;

public class PathExpression extends Expression {
	
	private String paramName;
	private QueriedPath cachedPath;
	
	public PathExpression(int clnId, Comparison compType, PathBuilder path, String paramName) {
		super(clnId, compType, path);
		this.paramName = paramName;
	}

	public PathExpression(int clnId, Comparison compType, PathBuilder path, String paramName, 
			QueriedPath cachedPath) {
		this(clnId, compType, path, paramName);
		this.cachedPath = cachedPath;
	}
	
	public QueriedPath getCachedPath() {
		return cachedPath;
	}

	@Override
	public boolean isCached() {
		return cachedPath != null;
	}
	
	public String getParamName() {
		return this.paramName;
	}
	
	//public void setValue(Object value) {
	//	this.value = value;
	//}
	
	public boolean isRegex() {
		return path.hasRegex(); // .contains("/*/");
	}
	
	public String getRegex() {
		// depends on axis...
		if (isRegex()) {
			return PathBuilder.regexFromPath(path.getFullPath());
		}
		return null;
	}
	
	public void setCachedPath(int dataType, boolean indexed, Collection<Integer> pathIds) {
		this.cachedPath = new QueriedPath(dataType, indexed, pathIds);
	}
	
	@Override
	public String toString() {
		return "PathExpression [path=" + path + ", param=" + paramName
				+ ", collectId=" + clnId + ", compType=" + compType
				+ ", cachedPath=" + cachedPath + "]";
	}
	

}
