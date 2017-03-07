package com.bagri.core.model;

import java.util.Arrays;

/**
 * Contains XDM value.
 *  
 * @author Denis Sukhoroslov
 * @since 05.2013 
 */
public class Element implements Comparable<Element> { 

	private int[] position;
	private Object value = null;
	
	/**
	 * default constructor
	 */
	public Element() {
		position = new int[0];
	}
	
	/**
	 * 
	 * @param elementId the element's id
	 * @param parentId the element's parent id
	 * @param value the element's value
	 */
	public Element(int[] position, Object value) {
		this.position = position;
		this.value = value;
	}

	/**
	 * @return the position
	 */
	public int[] getPosition() {
		return position;
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
		for (int i=0; i < position.length; i++) {
			if (other.position.length > i) {
				int r = position[i] - other.position[i];
				if (r != 0) {
					return r;
				}
			} else {
				return 1;
			}
		}
		if (position.length == other.position.length) {
			return 0;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Element [position=" + Arrays.toString(position) + ", value=" + value + "]";
	}

	
}
