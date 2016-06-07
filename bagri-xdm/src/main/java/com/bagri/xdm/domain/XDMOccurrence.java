package com.bagri.xdm.domain;

public class XDMOccurrence {
	
	public static final XDMOccurrence zeroOrOne = new XDMOccurrence(0, 1);
	public static final XDMOccurrence zeroOrMany = new XDMOccurrence(0, -1);
	public static final XDMOccurrence onlyOne = new XDMOccurrence(1, 1);
	public static final XDMOccurrence oneOrMany = new XDMOccurrence(1, -1);
	
	private int low;
	private int high;
	
	private XDMOccurrence(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public static XDMOccurrence getOccurence(int low, int high) {
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
		return new XDMOccurrence(low, high);
	}
	
	public int getLowBound() {
		return low;
	}
	
	public int getHighBound() {
		return high;
	}
	
	public boolean isArray() {
		return high > 1;
	}
	
	public boolean isConstant() {
		return low == high;
	}
	
	public boolean isMandatory() {
		return low > 0;
	}
	
	public boolean isOptional() {
		return low == 0;
	}

	public boolean isSingle() {
		return high == 1;
	}
	
	public boolean isUnbounded() {
		return high == -1;
	}
	
	@Override
	public String toString() {
		return "{" + low + ":" + high + "}";
	}

}
