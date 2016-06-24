package com.bagri.xdm.common.query;

public class Expression {

	protected int clnId;
	protected PathBuilder path; 
	protected Comparison compType;
	
	public Expression(int clnId, Comparison compType, PathBuilder path) {
		this.clnId = clnId;
		this.compType = compType;
		if (path != null) {
			this.path = new PathBuilder(path);
		}
	}
	
	public int getCollectionId() {
		return this.clnId;
	}
	
	public Comparison getCompType() {
		return this.compType;
	}

	public PathBuilder getPath() {
		return this.path;
	}
	
	public String getFullPath() {
		return path.getFullPath();
	}
	
	public boolean isCached() {
		return false;
	}
	
	//public void addPath(String add) {
	//	this.path += add;
	//}
}
