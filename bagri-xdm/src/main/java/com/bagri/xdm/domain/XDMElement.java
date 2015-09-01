package com.bagri.xdm.domain;

import java.io.Serializable;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.1
 */
public class XDMElement implements Comparable<XDMElement> { 

	private long elementId;
	private long parentId;
	private Object value = null;
	// transient path
	//private String path;
	//private int positionInParent;
	
	public XDMElement() {
		//
	}
	
	public XDMElement(long elementId, long parentId, Object value) {
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
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	public int asInt() {
		if (value == null) {
			return 0; //NaN;
		}
		if (value instanceof Integer) {
			return (Integer) value;
		}
		return Integer.valueOf(value.toString());
	}
	
	public long asLong() {
		if (value == null) {
			return 0; //NaN;
		}
		if (value instanceof Long) {
			return (Long) value;
		}
		return Long.valueOf(value.toString());
	}
	
	public boolean asBoolean() {
		if (value == null) {
			return false; //NaN;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return Boolean.valueOf(value.toString());
	}
	
	public byte asByte() {
		if (value == null) {
			return 0; //NaN;
		}
		if (value instanceof Byte) {
			return (Byte) value;
		}
		return Byte.valueOf(value.toString());
	}
	
	public short asShort() {
		if (value == null) {
			return 0; //NaN;
		}
		if (value instanceof Short) {
			return (Short) value;
		}
		return Short.valueOf(value.toString());
	}
	
	public float asFloat() {
		if (value == null) {
			return Float.NaN;
		}
		if (value instanceof Float) {
			return (Float) value;
		}
		return Float.valueOf(value.toString());
	}
	
	public double asDouble() {
		if (value == null) {
			return Double.NaN;
		}
		if (value instanceof Double) {
			return (Double) value;
		}
		return Double.valueOf(value.toString());
	}
	
	public String asString() {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		return value.toString();
	}
	
	//public Date asDate() {
	//	
	//}

	@Override
	public int compareTo(XDMElement other) {
		return (int) (this.elementId - other.elementId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMElement [elementId=" + elementId + ", parentId=" + parentId + /*", path=" + path +*/ ", value=" + value + "]";
	}

	
}
