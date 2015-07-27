package com.bagri.xquery.saxon;

import static com.bagri.xqj.BagriXQConstants.bg_schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.LookaheadIterator;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceExtent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.ExpressionContainer;
import com.bagri.xdm.api.XDMQueryManagement;


public class CollectionIterator implements SequenceIterator<Item>, 
	GroundedIterator<Item>, LastPositionFinder<Item>, LookaheadIterator<Item> {
	
    private static final Logger logger = LoggerFactory.getLogger(CollectionIterator.class);
	
	private XDMQueryManagement queryMgr;
	private ExpressionContainer query;
	private Collection<Long> docIds = null;
	private Iterator<Long> iter;
	private Item current;
	private int position = -1;
	
	public CollectionIterator(Collection<Long> docIds) {
		// for debug only
		this.docIds = new ArrayList<Long>(docIds);
		iter = this.docIds.iterator();
	}
	
	public CollectionIterator(XDMQueryManagement queryMgr, ExpressionContainer query) {
		this.queryMgr = queryMgr;
		this.query = query;
	}
	
	private void loadData() {
		docIds = queryMgr.getDocumentIDs(query);
		logger.trace("loadData; got {} document ids", docIds.size());
		iter = docIds.iterator();
	}
	
	private Item getCurrentItem(long docId) {
		return new AnyURIValue(bg_schema + ":/" + docId); 
	}		

	@Override
	public Item next() throws XPathException {
		logger.trace("next.enter; position: {}", position);
		if (docIds == null) {
			loadData();
		}
		if (iter.hasNext()) {
			current = getCurrentItem(iter.next()); 
			position++;
		} else {
			current = null;
		}
		logger.trace("next.exit; returning: {}", current);
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
		logger.trace("close; at position: {}", position);
		docIds.clear();
		docIds = null;
		iter = null;
		position = -1;
	}

	@Override
	public SequenceIterator<Item> getAnother() throws XPathException {
		// should reset iterator, probably!
		logger.trace("getAnother; returning: {}", this);
		return this; 
	}

	@Override
	public int getProperties() {
		return 0; //GROUNDED | LAST_POSITION_FINDER | LOOKAHEAD;
	}

	@Override
	public GroundedValue materialize() throws XPathException {
		if (docIds == null) {
			loadData();
		}
		List<Item> items = new ArrayList<Item>(docIds.size());
		for (Long docId: docIds) {
			items.add(getCurrentItem(docId));
		}
		logger.trace("materialize.exit; returning list of {} items", items.size());
		return new SequenceExtent<Item>(items);
	}

	@Override
	public int getLength() throws XPathException {
		if (docIds == null) {
			loadData();
		}
		int length = docIds.size();
		logger.trace("getLength.exit; returning {}", length);
		return length;
	}

	@Override
	public boolean hasNext() {
		boolean result = iter.hasNext();
		logger.trace("hasNext; returning: {}", result);
		return result;
	}

	@Override
	public String toString() {
		return "CollectionIterator [queryMgr=" + queryMgr + ", query=" + query
				+ ", docIds=" + docIds + ", current=" + current + ", position=" + position + "]";
	}

}
