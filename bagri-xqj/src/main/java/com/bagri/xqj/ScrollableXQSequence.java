package com.bagri.xqj;

import java.util.List;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;

import com.bagri.xquery.api.XQProcessor;

public class ScrollableXQSequence extends BagriXQSequence implements XQSequence {
	
	private List sequence;
	private int position;
	
	ScrollableXQSequence(BagriXQDataFactory xqFactory, XQProcessor xqProcessor, List sequence) {
		super(xqFactory, xqProcessor);
		this.sequence = sequence;
		position = 0;
	}
	
	private void setCurrent() throws XQException {
		// @TODO: construct XQItemType properly..
		if (position > 0 && position <= sequence.size()) {
			Object current = sequence.get(position - 1); 
			if (current instanceof XQItem) {
				XQItem item = (XQItem) current;
				setCurrent(item.getItemType(), item.getObject());
			} else {
				setCurrent(BagriXQUtils.getTypeForObject(xqFactory, current), current);
			}
		} else {
			setCurrent(null, null);
		}
	}

	@Override
	public boolean absolute(int itempos) throws XQException {
		
		checkSequence();
		if (sequence.size() > 0) {
			if (itempos >= 0) {
				position = itempos;
			} else {
				position = sequence.size() + itempos + 1;
			}
			if (position < 0) {
				position = 0;
			} else if (position > sequence.size() + 1) {
				position = sequence.size() + 1;
			}
			setCurrent();
			return position > 0 && position <= sequence.size();
		}
		return false;
	}

	@Override
	public void afterLast() throws XQException {
		
		checkSequence();
		position = sequence.size() + 1;
		setCurrent();
	}

	@Override
	public void beforeFirst() throws XQException {
		
		checkSequence();
		position = 0;
		setCurrent();
	}

	@Override
	public void close() throws XQException {
		
		super.close();
		sequence.clear();
		sequence = null;
	}

	@Override
	public int count() throws XQException {
		
		checkSequence();
		return sequence.size();
	}

	//@Override
	//Iterator getIterator() throws XQException {
		
	//	if (isClosed()) {
	//		throw new XQException("Sequence is closed");
	//	}
	//	return sequence.iterator();
	//}

	@Override
	public int getPosition() throws XQException {
		
		checkSequence();
		return position;
	}

	@Override
	public boolean isOnItem() throws XQException {
		
		checkSequence();
		return position > 0 && position <= sequence.size();
	}

	@Override
	public boolean isScrollable() throws XQException {
		
		checkSequence();
		return true;
	}

	@Override
	public boolean first() throws XQException {
		
		checkSequence();
		if (sequence.size() > 0) {
			position = 1;
			setCurrent();
			return true;
		}
		return false;
	}

	@Override
	public boolean isAfterLast() throws XQException {

		checkSequence();
		return position > sequence.size(); //sequence.size() > 0 && position == sequence.size() - 1;
	}

	@Override
	public boolean isBeforeFirst() throws XQException {

		checkSequence();
		return position == 0 && sequence.size() > 0;
	}

	@Override
	public boolean isFirst() throws XQException {

		checkSequence();
		return position == 1;
	}

	@Override
	public boolean isLast() throws XQException {
		
		checkSequence();
		return position == sequence.size();
	}

	@Override
	public boolean last() throws XQException {
		
		checkSequence();
		if (sequence.size() > 0) {
			position = sequence.size();
			setCurrent();
			return true;
		}
		return false;
	}

	@Override
	public boolean next() throws XQException {

		checkSequence();
		position++;
		if (position <= sequence.size() && sequence.size() > 0) {
			setCurrent();
			return true;
		}
		return false;
	}

	@Override
	public boolean previous() throws XQException {

		checkSequence();
		position--;
		if (position > 0 && sequence.size() > 0) {
			setCurrent();
			return true;
		}
		return false;
	}

	@Override
	public boolean relative(int itempos) throws XQException {
		
		checkSequence();
		if (sequence.size() > 0) {
			position += itempos;
			if (position < 0) {
				position = 0;
			} else if (position > sequence.size() + 1) {
				position = sequence.size() + 1;
			}
			setCurrent();
			return position > 0 && position <= sequence.size();
		}
		return false;
	}
	
}
