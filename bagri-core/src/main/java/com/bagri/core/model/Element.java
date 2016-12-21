package com.bagri.core.model;


/**
 * Contains XDM value.
 *  
 * @author Denis Sukhoroslov
 * @since 05.2013 
 */
public class Element implements Comparable<Element> { 

	private int elementId;
	private int parentId;
	private Object value = null;
	//private int positionInParent;
	
	/**
	 * default constructor
	 */
	public Element() {
		//
	}
	
	/**
	 * 
	 * @param elementId the element's id
	 * @param parentId the element's parent id
	 * @param value the element's value
	 */
	public Element(int elementId, int parentId, Object value) {
		super();
		this.elementId = elementId;
		this.parentId = parentId;
		this.value = value;
	}

	/**
	 * @return the element's id
	 */
	public int getElementId() {
		return elementId;
	}

	/**
	 * @param elementId the element Id to set
	 */
	public void setElementId(int elementId) {
		this.elementId = elementId;
	}

	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

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
	
	/**
	 * 
	 * @return integer value
	 */
	public int asInt() {
		if (value == null) {
			return 0; 
		}
		if (value instanceof Integer) {
			return (Integer) value;
		}
		return Integer.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return long value
	 */
	public long asLong() {
		if (value == null) {
			return 0; 
		}
		if (value instanceof Long) {
			return (Long) value;
		}
		return Long.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return boolean value
	 */
	public boolean asBoolean() {
		if (value == null) {
			return false; 
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return Boolean.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return byte value
	 */
	public byte asByte() {
		if (value == null) {
			return 0; 
		}
		if (value instanceof Byte) {
			return (Byte) value;
		}
		return Byte.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return short value
	 */
	public short asShort() {
		if (value == null) {
			return 0; 
		}
		if (value instanceof Short) {
			return (Short) value;
		}
		return Short.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return float value
	 */
	public float asFloat() {
		if (value == null) {
			return Float.NaN;
		}
		if (value instanceof Float) {
			return (Float) value;
		}
		return Float.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return double value
	 */
	public double asDouble() {
		if (value == null) {
			return Double.NaN;
		}
		if (value instanceof Double) {
			return (Double) value;
		}
		return Double.valueOf(value.toString());
	}
	
	/**
	 * 
	 * @return String value
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Element other) {
		return (int) (this.elementId - other.elementId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Element [elementId=" + elementId + ", parentId=" + parentId + ", value=" + value + "]";
	}

	
}
