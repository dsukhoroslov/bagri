package com.bagri.xdm.query;

import java.util.Collection;

/**
 * Represents an expression comparing values belonging to some (x-)path with parameter value.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class PathExpression extends Expression {
	
	private String paramName;
	private QueriedPath cachedPath;
	
	/**
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 * @param paramName the parameter name
	 */
	public PathExpression(int clnId, Comparison compType, PathBuilder path, String paramName) {
		super(clnId, compType, path);
		this.paramName = paramName;
	}

	/**
	 * 
	 * @param clnId the collection identifier
	 * @param compType the comparison type
	 * @param path the expression path
	 * @param paramName the parameter name
	 * @param cachedPath the prepared cached path
	 */
	public PathExpression(int clnId, Comparison compType, PathBuilder path, String paramName, 
			QueriedPath cachedPath) {
		this(clnId, compType, path, paramName);
		this.cachedPath = cachedPath;
	}
	
	/**
	 * 
	 * @return the prepared cached path
	 */
	public QueriedPath getCachedPath() {
		return cachedPath;
	}

	/**
	 * @return true if cached path is assigned, false otherwise
	 */
	@Override
	public boolean isCached() {
		return cachedPath != null;
	}
	
	/**
	 * 
	 * @return the name of parameter to compare with
	 */
	public String getParamName() {
		return this.paramName;
	}
	
	/**
	 * 
	 * @return true if the expression path contains wildcards, false otherwise
	 */
	public boolean isRegex() {
		return path.hasRegex(); 
	}
	
	/**
	 * 
	 * @return the regular expression representing underlying path if it has wildcards 
	 */
	public String getRegex() {
		// depends on axis...
		if (isRegex()) {
			return PathBuilder.regexFromPath(path.getFullPath());
		}
		return null;
	}
	
	/**
	 * 
	 * @param dataType the XQJ data type
	 * @param indexed is path indexed or not
	 * @param pathIds resolved model path identifiers
	 */
	public void setCachedPath(int dataType, boolean indexed, Collection<Integer> pathIds) {
		this.cachedPath = new QueriedPath(dataType, indexed, pathIds);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "PathExpression [path=" + path + ", param=" + paramName
				+ ", collectId=" + clnId + ", compType=" + compType
				+ ", cachedPath=" + cachedPath + "]";
	}
	

}
