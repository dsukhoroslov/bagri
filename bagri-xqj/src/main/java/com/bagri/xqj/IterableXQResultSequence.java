package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_sequence_closed;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQResultSequence;

import com.bagri.core.api.ResultCursor;

public class IterableXQResultSequence extends IterableXQSequence implements XQResultSequence {

	private BagriXQDynamicContext expression;
	
	IterableXQResultSequence(BagriXQDynamicContext expression, ResultCursor cursor) {
		super(expression.connection, expression.connection.getProcessor(), cursor);
		this.expression = expression;
	}

	@Override
	public XQConnection getConnection() throws XQException {

		checkState(ex_sequence_closed);
		return expression.connection;
	}
	
	@Override
	public XQItem getItem() throws XQException {
		
		checkState(ex_sequence_closed);
		super.getItem();
		return new BagriXQResultItem(type, value, this);
	}

	@Override
	public boolean isClosed() {
		
		return expression.isClosed() || super.isClosed();
	}
}
