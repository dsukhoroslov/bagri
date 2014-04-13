package com.bagri.xdm;

import java.io.Serializable;

public class XDMPath implements Comparable { //implements Serializable {
	
	/**
	 * 
	 */
	//private static final long serialVersionUID = -2761432259956491362L;
	
	private String path;
	private int typeId;
	private XDMNodeKind kind;
	private int pathId;
	private int parentId;
	private int postId;
	
	public XDMPath() {
		super();
	}
	
	public XDMPath(String path, int typeId, XDMNodeKind kind, int pathId, int parentId, int postId) {
		super();
		this.path = path;
		this.typeId = typeId;
		this.kind = kind;
		this.pathId = pathId;
		this.parentId = parentId;
		this.postId = postId;
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * @return the NodeKind
	 */
	public XDMNodeKind getNodeKind() {
		return kind;
	}
	
	/**
	 * @return the typeId
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return the pathId
	 */
	public int getPathId() {
		return pathId;
	}
	
	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @return the postId
	 */
	public int getPostId() {
		return postId;
	}
	
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @param postId the postId to set
	 */
	public void setPostId(int postId) {
		this.postId = postId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMPath [path=" + path + ", pathId=" + pathId + ", typeId="
				+ typeId + ", kind=" + kind + ", parentId=" + parentId
				+ ", postId=" + postId + "]";
	}

	@Override
	public int compareTo(Object other) {
		
		return this.pathId - ((XDMPath) other).pathId;
	}
	
	

}
