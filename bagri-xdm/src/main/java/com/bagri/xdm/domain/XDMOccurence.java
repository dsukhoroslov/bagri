package com.bagri.xdm.domain;

public class XDMOccurence {
	
	public static final XDMOccurence zeroOrOne = new XDMOccurence(0, 1);
	public static final XDMOccurence zeroOrMany = new XDMOccurence(0, -1);
	public static final XDMOccurence onlyOne = new XDMOccurence(1, 1);
	public static final XDMOccurence oneOrMany = new XDMOccurence(1, -1);
	
	private int low;
	private int high;
	
	private XDMOccurence(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public static XDMOccurence getOccurence(int low, int high) {
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
		return new XDMOccurence(low, high);
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
