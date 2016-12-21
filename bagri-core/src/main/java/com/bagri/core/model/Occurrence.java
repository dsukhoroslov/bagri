package com.bagri.core.model;

/**
 * The node multiplicity
 * 
 * @author Denis Sukhoroslov
 *
 */
public class Occurrence {
	
	/**
	 * zero or one 
	 */
	public static final Occurrence zeroOrOne = new Occurrence(0, 1);
	
	/**
	 * zero or mane
	 */
	public static final Occurrence zeroOrMany = new Occurrence(0, -1);
	
	/**
	 * only one
	 */
	public static final Occurrence onlyOne = new Occurrence(1, 1);
	
	/**
	 * one or many
	 */
	public static final Occurrence oneOrMany = new Occurrence(1, -1);
	
	private int low;
	private int high;
	
	/**
	 * 
	 * @param low low bound
	 * @param high high bound
	 */
	private Occurrence(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	/**
	 * 
	 * @param low low bound
	 * @param high high bound
	 * @return XDMOccurence static instance if it exists or constructs the new one 
	 */
	public static Occurrence getOccurrence(int low, int high) {
		if (low == 0) {
			if (high == 1) {
				return zeroOrOne;
			}
			if (high < 0) {
				return zeroOrMany;
			}
		} else if (low == 1) {
			if (high == 1) {
				return onlyOne;
			}
			if (high < 0) {
				return oneOrMany;
			}
		}
		return new Occurrence(low, high);
	}
	
	/**
	 * 
	 * @return the low bound
	 */
	public int getLowBound() {
		return low;
	}
	
	/**
	 * 
	 * @return the high bound
	 */
	public int getHighBound() {
		return high;
	}
	
	/**
	 * 
	 * @return true if high bound is greater then one, false otherwise
	 */
	public boolean isArray() {
		return high > 1;
	}
	
	/**
	 * 
	 * @return true if low bound equals high bound, false otherwise
	 */
	public boolean isConstant() {
		return low == high;
	}
	
	/**
	 * 
	 * @return true if low bound is greater then zero, false otherwise
	 */
	public boolean isMandatory() {
		return low > 0;
	}
	
	/**
	 * 
	 * @return true if low bound equals to zero, false otherwise
	 */
	public boolean isOptional() {
		return low == 0;
	}

	/**
	 * 
	 * @return true if high bound is equals to one, false otherwise
	 */
	public boolean isSingle() {
		return high == 1;
	}
	
	/**
	 * 
	 * @return true if high bound equals to negative one (-1), false otherwise
	 */
	public boolean isUnbounded() {
		return high == -1;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + low + ":" + high + "}";
	}

}
