package com.bagri.xdm.common;

/**
 * The key constructed from two Strings
 * 
 * @author Denis Sukhoroslov
 *
 */
public class StringStringKey {
	
	private String left;
	private String right;
	
	/**
	 * 
	 * @param left the left key part
	 * @param right the right key part
	 */
	public StringStringKey(String left, String right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * @return the left key part 
	 */
	public String getLeft() {
		return left;
	}

	/**
	 * @return the right key part 
	 */
	public String getRight() {
		return right;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + left.hashCode();
		result = prime * result + right.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringStringKey other = (StringStringKey) obj;
		if (!left.equals(other.left)) {
			return false;
		} else if (!right.equals(other.right)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "StringStringKey [left=" + left + ", right=" + right + "]";
	}
	
}
