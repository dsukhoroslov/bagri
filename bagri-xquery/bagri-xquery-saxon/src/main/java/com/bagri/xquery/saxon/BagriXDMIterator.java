package com.bagri.xquery.saxon;

import java.util.Collection;
import java.util.Iterator;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.AnyURIValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.xdm.access.api.XDMDocumentManagement;

public class BagriXDMIterator implements SequenceIterator<Item>, LookaheadIterator<Item> {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXDMIterator.class);
	
	private XDMDocumentManagement dataMgr;
	private ExpressionBuilder query;
	//private Collection<String> docIds = new ArrayList<String>();
	private Iterator<String> iter;
	private Item current;
	private int position = -1;
	
	public BagriXDMIterator(ExpressionBuilder query) {
		this.query = query;
	}
	
	public XDMDocumentManagement getDataManager() {
		return dataMgr;
	}
	
	public void setDataManager(XDMDocumentManagement dataMgr) {
		this.dataMgr = dataMgr;
	}
	
	private void loadData() {
		Collection<String> ids = dataMgr.getDocumentURIs(query);
		iter = ids.iterator();
		//position = 0;
	}

	@Override
	public Item next() throws XPathException {
		logger.trace("next; position: {}", position);
		if (position < 0) {
			loadData();
		}
		if (iter.hasNext()) {
			String curr = iter.next();
			position++;
			current = new AnyURIValue(curr); //"/library/" + curr); // QNameValue("", "", key);
		} else {
			current = null;
		}
		return current;
	}

	@Override
	public Item current() {
		logger.trace("current; returning: {}", current);
		return current;
	}

	@Override
	public int position() {
		logger.trace("position; returning: {}", position);
		return position;
	}

	@Override
	public void close() {
		logger.trace("close;");
		iter = null;
		position = -1;
	}

	@Override
	public SequenceIterator<Item> getAnother() throws XPathException {
		// TODO Auto-generated method stub
		logger.trace("getAnother; returning: {}", this);
		return this; //null;
	}

	@Override
	public int getProperties() {
		return LOOKAHEAD;
	}

	@Override
	public boolean hasNext() {
		boolean result = iter.hasNext();
		logger.trace("hasNext; returning: {}", result);
		return result;
	}

}
