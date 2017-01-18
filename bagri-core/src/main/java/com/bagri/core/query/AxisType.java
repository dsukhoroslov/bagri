package com.bagri.core.query;

/**
 * An internal enumeration representing XPath axis types. 
 * 
 * @author Denis Sukhoroslov
 *
 */
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
		switch (this) {
			//case ANCESTOR: return "/ancestor"
			//case ANCESTOR_OR_SELF: return "/ancestor-or-self";
			case ATTRIBUTE: return "/@";
			case CHILD: return "/"; // "/child"
			case DESCENDANT: return "//"; // return "/descendant";
			//case DESCENDANT_OR_SELF: "/descendant-or-self"
			//case FOLLOWING: return "/following";
			//case FOLLOWING_SIBLING: return "/following-sibling";
			case NAMESPACE: return "/#";
			case PARENT: return "/.."; // "/parent"
			//case PRECEDING: return "/preceding";
			//case PRECEDING_SIBLING: return "/preceding-sibling";
			case SELF: return "/."; // "/self"
		}
		return "";
	}

}
