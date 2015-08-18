package com.bagri.xdm.domain;

//import java.io.Serializable;
//import static javax.xml.xquery.XQItemType.*

public class XDMPath implements Comparable<XDMPath> { //implements Serializable {
	
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
	// the type constant from javax.xml.xquery.XQItemType.*
	// change it to QName?
	private int dataType;
	private XDMCardinality cardinality = XDMCardinality.zeroOrOne;
	
	// cache it!
	private String name = null; 
	
	public XDMPath() {
		super();
	}
	
	public XDMPath(String path, int typeId, XDMNodeKind kind, int pathId, int parentId, int postId, 
			int dataType, XDMCardinality cardinality) {
		super();
		this.path = path;
		this.typeId = typeId;
		this.kind = kind;
		this.pathId = pathId;
		this.parentId = parentId;
		this.postId = postId;
		this.dataType = dataType;
		if (cardinality != null) {
			this.cardinality = cardinality;
		}
	}
	
	public XDMCardinality getCardinality() {
		return cardinality;
	}
	
	/*
	 * return XQJ data type
	 */
	public int getDataType() {
		return dataType;
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * @return the last path portion
	 */
	public String getName() {
		if (kind == XDMNodeKind.document || kind == XDMNodeKind.comment) {
			return null;
		}
		
		if (name == null) {
			String last;
			String[] segments = path.split("/");
	
			switch (kind) {
				case attribute: //@
				case namespace: //#
				case pi: 		//?
					last = segments[segments.length-1];
					name = last.substring(1);
					break;
				case text: 
					name = segments[segments.length-2];
					break;
				case element:
					if (segments.length > 0) {
						name = segments[segments.length-1];
					} else {
						name = path;
					}
					break;
				//case document:
				//case comment:
				default:
					return null;
			}
		}
		return name;
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

	
	@Override
	public int hashCode() {
		return 31 + path.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		XDMPath other = (XDMPath) obj;
		return path.equals(other.path);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMPath [path=" + path + ", pathId=" + pathId + ", typeId="
				+ typeId + ", kind=" + kind + ", parentId=" + parentId
				+ ", postId=" + postId + ", dataType=" + dataType
				+ ", cardinality=" + cardinality.toString() + "]";
	}

	@Override
	public int compareTo(XDMPath other) {
		
		return this.pathId - other.pathId;
	}
	
}
