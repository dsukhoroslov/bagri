package com.bagri.xdm.domain;

import java.io.Serializable;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.1
 */
public class XDMElement { 

	private long elementId;
	private long parentId;
	private String value;
	// transient path
	//private String path;
	//private int positionInParent;
	
	public XDMElement() {
		//
	}
	
	public XDMElement(long elementId, long parentId, String value) {
		super();
		this.elementId = elementId;
		this.parentId = parentId;
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public long getElementId() {
		return elementId;
	}

	/**
	 * @param elementId the element Id to set
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
	 * @return the path
	 */
	//public String getPath() {
	//	return path;
	//}

	/**
	 * @param path the path to set
	 */
	//public void setPath(String path) {
	//	this.path = path;
	//}

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
		return "XDMElement [elementId=" + elementId + ", parentId=" + parentId + /*", path=" + path +*/ ", value=" + value + "]";
	}
	
}
