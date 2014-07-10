package com.bagri.xdm.domain;

import java.io.Serializable;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.1
 */
public class XDMElement { //implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6158653199731009185L;
	
	private long elementId;
	private long parentId;
	private long documentId;
	private int pathId;
	private XDMNodeKind kind;
	private String name;
	private String value;
	// transient path
	private String path;
	//private int positionInParent;
	
	public XDMElement() {
		//
		kind = XDMNodeKind.comment;
	}
	
	public XDMElement(long elementId, long parentId, long documentId, XDMNodeKind kind,
			int pathId, String name, String value) {
		super();
		this.elementId = elementId;
		this.parentId = parentId;
		this.documentId = documentId;
		this.kind = kind;
		this.pathId = pathId;
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public long getElementId() {
		return elementId;
	}

	/**
	 * @param long elementId the element Id to set
	 */
	public void setElementId(long elementId) {
		this.elementId = elementId;
	}

	/**
	 * @return the parentId
	 */
	public long getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the documentId
	 */
	public long getDocumentId() {
		return documentId;
	}

	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(long documentId) {
		this.documentId = documentId;
	}

	/**
	 * @return the kind
	 */
	public XDMNodeKind getKind() {
		return kind;
	}

	/**
	 * @return the kind
	 */
	public String getNodeKind() {
		return XDMNodeKind.getNodeKindAsString(kind);
	}

	/**
	 * @param kind the kind to set
	 */
	public void setKind(XDMNodeKind kind) {
		this.kind = kind;
	}

	/**
	 * @return the path Id
	 */
	public int getPathId() {
		return pathId;
	}

	/**
	 * @param pathId the path Id to set
	 */
	public void setPathId(int pathId) {
		this.pathId = pathId;
		this.path = null;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	public int asInt() {
		return Integer.valueOf(value);
	}
	
	public long asLong() {
		return Long.valueOf(value);
	}
	
	public boolean asBoolean() {
		return Boolean.valueOf(value);
	}
	
	public byte asByte() {
		return Byte.valueOf(value);
	}
	
	public short asShort() {
		return Short.valueOf(value);
	}
	
	public float asFloat() {
		return Float.valueOf(value);
	}
	
	public double asDouble() {
		return Double.valueOf(value);
	}
	
	//public Date asDate() {
	//	
	//}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMElement [elementId=" + elementId + ", parentId=" + parentId + ", documentId="
				+ documentId + ", kind=" + kind + ", pathId=" + pathId + ", name="
				+ name + ", value=" + value + "]";
	}
	
	//public static String getNodeKind(byte kind) {
	//	switch
	//}
	
}
