package com.bagri.common.query;

public enum AxisType {

	ANCESTOR,
	ANCESTOR_OR_SELF,
	ATTRIBUTE,
	CHILD, 
	DESCENDANT, 
	DESCENDANT_OR_SELF,
	FOLLOWING,
	FOLLOWING_SIBLING,
	NAMESPACE,
	PARENT,
	PRECEDING,
	//PRECEDING_OR_ANCESTOR,
	PRECEDING_SIBLING,
	SELF;
	
	public String getAxis() {
		switch (this.ordinal()) {
			//case 0:
			//case 1: return "";
			case 2: return "/@";
			case 3: return "/";
			case 4: return "//";
			//case 5:
			//case 6:
			//case 7: return "";
			//case 8: return "#";
			case 9: return "/..";
			//case 10:
			//case 11: return "";
			case 12: return "/.";
		}
		return "";
	}

}
