package com.bagri.xdm.domain;

public class XDMCardinality {
	
	public static final XDMCardinality zeroOrOne = new XDMCardinality(0, 1);
	public static final XDMCardinality zeroOrMany = new XDMCardinality(0, -1);
	public static final XDMCardinality onlyOne = new XDMCardinality(1, 1);
	public static final XDMCardinality oneOrMany = new XDMCardinality(1, -1);
	
	private int low;
	private int high;
	
	private XDMCardinality(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public static XDMCardinality getCardinality(int low, int high) {
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
		return new XDMCardinality(low, high);
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
	
	public boolean isOptional() {
		return low == 0;
	}

	public boolean isMandatory() {
		return low > 0;
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
