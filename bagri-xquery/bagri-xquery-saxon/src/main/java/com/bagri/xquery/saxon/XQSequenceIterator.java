package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.convertXQItem;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public class XQSequenceIterator implements SequenceIterator {
	
	private static final Logger logger = LoggerFactory.getLogger(XQSequenceIterator.class);
	
	private XQSequence xqs;
	private Configuration config;
	
	public XQSequenceIterator(XQSequence xqs, Configuration config) {
		this.xqs = xqs;
		this.config = config;
	}
	
	@Override
	public Item next() throws XPathException {
		try {
			if (xqs.next()) {
				return convertXQItem(xqs.getItem(), config);
			}
		} catch (XQException ex) {
			throw new XPathException(ex);
		}
		return null;
	}

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
		return null; // new XQSequenceIterator(xqs, config); //??
	}

	@Override
	public int getProperties() {
		return 0;
	}

}
