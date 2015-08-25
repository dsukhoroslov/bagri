package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_item_closed;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQResultItem;
import javax.xml.xquery.XQResultSequence;

import com.bagri.xquery.api.XQProcessor;

public class BagriXQResultItem extends BagriXQItem implements XQResultItem {
	
	private XQResultSequence parent;

	BagriXQResultItem(XQProcessor xqProcessor, XQItemType type, Object value) {
		super(xqProcessor, type, value);
	}

	BagriXQResultItem(XQItemType type, Object value, XQResultSequence parent) {
		this(((BagriXQSequence) parent).getXQProcessor(), type, value);
		this.parent = parent;
	}

	@Override
	public XQConnection getConnection() throws XQException {
		
		checkState(ex_item_closed);
		return parent.getConnection();
	}
	
	@Override
	public boolean isClosed() {
		
		return parent.isClosed() || super.isClosed();
	}
	

}
