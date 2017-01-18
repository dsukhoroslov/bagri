package com.bagri.core.query;

/**
 * The base expression class. Represents an expression in some query language 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class Expression {

	protected int clnId;
	protected PathBuilder path; 
	protected Comparison compType;
	
	/**
	 * 
	 * @param clnId the collection identifier. -1 for default collection
	 * @param compType the comparison type
	 * @param path the expression path
	 */
	protected Expression(int clnId, Comparison compType, PathBuilder path) {
		this.clnId = clnId;
		this.compType = compType;
		if (path != null) {
			this.path = new PathBuilder(path);
		}
	}
	
	/**
	 * 
	 * @return the collection id
	 */
	public int getCollectionId() {
		return this.clnId;
	}
	
	/**
	 * 
	 * @return the comparison type
	 */
	public Comparison getCompType() {
		return this.compType;
	}

	/**
	 * 
	 * @return the expression path
	 */
	public PathBuilder getPath() {
		return this.path;
	}
	
	/**
	 * 
	 * @return the full expression path
	 */
	public String getFullPath() {
		return path.getFullPath();
	}
	
	/**
	 * 
	 * @return true if expression cached, false otherwise
	 */
	public boolean isCached() {
		return false;
	}
	
}
