package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.*;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQSequence;

import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.ObjectValue;
//import net.sf.saxon.xqj.SaxonDuration;
//import net.sf.saxon.xqj.SaxonXMLGregorianCalendar;


public class JPConverterImpl extends JPConverter {

	@Override
	public Sequence convert(Object object, XPathContext context) throws XPathException {
		if (object instanceof XQItem) {
			XQItem item = (XQItem) object;
			try {
				BuiltInAtomicType type = getAtomicType(item.getItemType());
				if (type == null) {
					return convertToItem(item.getObject(), context.getConfiguration(), item.getItemType().getItemKind());
				}
				return convertToItem(item.getObject(), context.getConfiguration(), type);
			} catch (XQException e) {
				throw new XPathException(e);
			}
		} else if (object instanceof XQSequence) {
			XQSequence sequence = (XQSequence) object;
			return new ObjectValue(sequence);
		}
		return null; //convertToItem(object, context.getConfiguration());
	}

	@Override
	public ItemType getItemType() {
		return AnyItemType.getInstance(); 
	}

}
