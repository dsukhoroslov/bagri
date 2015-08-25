package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_sequence_closed;

import java.util.List;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQResultSequence;

public class ScrollableXQResultSequence extends ScrollableXQSequence implements	XQResultSequence {

	private BagriXQDynamicContext expression;
	
	ScrollableXQResultSequence(BagriXQDynamicContext expression, List sequence) {
		super(expression.connection, expression.connection.getProcessor(), sequence);
		this.expression = expression;
	}

	@Override
	public XQConnection getConnection() throws XQException {
		
		checkState(ex_sequence_closed);
		return expression.connection;
	}
	
	@Override
	public XQItem getItem() throws XQException {
		
		return new BagriXQResultItem(this.getItemType(), this.getAtomicValue(), this);
	}

	@Override
	public boolean isClosed() {
		
		return expression.isClosed() || super.isClosed();
	}
	
}
