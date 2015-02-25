package com.bagri.xqj;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import com.bagri.xquery.api.XQProcessor;

public class BagriXQItem extends BagriXQItemAccessor implements XQItem {
	
	BagriXQItem(XQProcessor xqProcessor, XQItemType type, Object value) {
		super(xqProcessor);
		setCurrent(type, value);
	}

	//@Override
	//public XQItemType getItemType() throws XQException {
	//	if (!positioned) {
	//		throw new XQException("not positioned on the Item");
	//	}
	//	return super.getItemType();
	//}

	public String toString() {
		
		return String.valueOf(value);
	}

}
