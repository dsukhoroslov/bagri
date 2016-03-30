package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.convertXQItem;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class XQSequenceIterator implements SequenceIterator {
	
	private static final Logger logger = LoggerFactory.getLogger(XQSequenceIterator.class);
	
	private int position;
	private XQSequence xqs;
	private Configuration config;
	
	public XQSequenceIterator(XQSequence xqs, Configuration config) {
		this.xqs = xqs;
		this.config = config;
		this.position = 0;
	}
	
	private Item convertToItem(XQItem item) throws XPathException, XQException { 
		return convertXQItem(item, config);
	}

	@Override
	public Item next() throws XPathException {
		try {
			if (xqs.next()) {
				position++;
				return convertToItem(xqs.getItem());
			}
		} catch (XQException ex) {
			throw new XPathException(ex);
		}
		return null;
	}

	//@Override
	//public Item current() {
	//	try {
	//		if (xqs.isOnItem()) {
	//			return convertToItem(xqs.getItem());
	//		}
	//	} catch (XQException | XPathException ex) {
	//		logger.error("current", ex);
	//	}
	//	return null;
	//}

	//@Override
	//public int position() {
	//	return position;
	//}

	@Override
	public void close() {
		try {
			xqs.close();
		} catch (XQException ex) {
			logger.error("close", ex);
		}
	}

	@Override
	public SequenceIterator getAnother() throws XPathException {
		return null;
	}

	@Override
	public int getProperties() {
		return 0;
	}

}
