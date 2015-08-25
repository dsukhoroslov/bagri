package com.bagri.xqj;

import java.util.Iterator;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQSequence;

public class BagriXQSequenceIterator implements Iterator {
	
	private XQSequence sequence;
	private boolean isLast = false; 

	BagriXQSequenceIterator(XQSequence sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public boolean hasNext() {
		
		try {
			if (sequence.isScrollable()) {
				return !(sequence.isLast() || sequence.isAfterLast());
			}
		} catch (XQException ex) {
			
			//e.printStackTrace();
		}
		return !isLast;
	}

	@Override
	public Object next() {
		
		try {
			if (sequence.next()) {
				return sequence.getObject();
			} else {
				isLast = false;
			}
		} catch (XQException ex) {
			
			// e.printStackTrace();
		}
		return null;
	}

	@Override
	public void remove() {
		
		throw new UnsupportedOperationException("Remove is not supported");
	}

}
