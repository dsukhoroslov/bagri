package com.bagri.xqj;

import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import com.bagri.xquery.api.XQProcessor;

public class BagriXQItem extends BagriXQItemAccessor implements XQItem {
	
	//BagriXQItem(XQItemType type, Object value) {
	//	super();
	//	setCurrent(type, value);
	//}

	BagriXQItem(XQProcessor xqProcessor, XQItemType type, Object value) {
		super(xqProcessor);
		setCurrent(type, value);
	}

	public String toString() {
		
		return String.valueOf(value);
	}

}
