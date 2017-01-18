package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.convertXQItem;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;


public class JPConverterImpl extends JPConverter {

	private static final Logger logger = LoggerFactory.getLogger(JPConverterImpl.class);
	
	@Override
	public Sequence convert(Object object, XPathContext context) throws XPathException {
		logger.trace("convert.enter; object: {}", object);
		Sequence result = null;
		if (object instanceof XQItem) {
			try {
				result = convertXQItem((XQItem) object, context.getConfiguration());
			} catch (XQException ex) {
				throw new XPathException(ex);
			}
		} else if (object instanceof XQSequence) {
			XQSequence sequence = (XQSequence) object;
			result = SequenceTool.toMemoSequence(new XQSequenceIterator(sequence, context.getConfiguration()));
		}
		logger.trace("convert.exit; returning: {}", result);
		return result; //convertToItem(object, context.getConfiguration());
	}

	@Override
	public ItemType getItemType() {
		return AnyItemType.getInstance(); 
	}

}
