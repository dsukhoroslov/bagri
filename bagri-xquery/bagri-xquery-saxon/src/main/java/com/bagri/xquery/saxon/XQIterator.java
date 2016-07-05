package com.bagri.xquery.saxon;

import java.util.Iterator;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class XQIterator implements Iterator {

	private static final Logger logger = LoggerFactory.getLogger(XQIterator.class);
	
	private XQDataFactory xqFactory;
	private SequenceIterator iter;
	private Item next;
	
	public XQIterator(XQDataFactory xqFactory, SequenceIterator iter) {
		this.xqFactory = xqFactory;
		this.iter = iter;
		try {
			next = iter.next();
		} catch (XPathException e) {
			logger.error("<init>.error", e);
		}
	}
	
	public int getFullSize() {
		if ((iter.getProperties() & SequenceIterator.LAST_POSITION_FINDER) != 0) {
			try {
				return ((LastPositionFinder) iter).getLength();
			} catch (XPathException ex) {
				logger.error("getFullSize.error", ex);
			}
		}
		if (next == null) {
			return 0;
		}
		return -1; // ONE_OR_MORE
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}
	
	@Override
	public Object next() {
		Item item = next;
		try {
			next = iter.next();
		} catch (XPathException ex) {
			// throw Runtime ex?
			logger.error("next 1.error", ex);
		}
		
		if (item != null) {
			try {
				return SaxonUtils.itemToXQItem(item, xqFactory);
			} catch (XPathException ex) {
				logger.error("next 2.error", ex);
			} catch (XQException ex) {
				logger.error("next 3.error", ex);
			}
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Operation 'remove' is not supported");
	}

}
