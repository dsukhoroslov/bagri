package com.bagri.xqj;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

public class BagriXQItem extends BagriXQItemAccessor implements XQItem {
	
	BagriXQItem(XQItemType type, Object value) {
		super();
		setCurrent(type, value);
	}


	public String toString() {
		
		//if (closed) {
		//	throw new XQException();
		//}
		return String.valueOf(value);
	}

}
