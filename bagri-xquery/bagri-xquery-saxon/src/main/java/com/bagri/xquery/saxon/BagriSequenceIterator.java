package com.bagri.xquery.saxon;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Base64BinaryValue;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.CalendarValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.DurationValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.HexBinaryValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.QualifiedNameValue;

import com.bagri.xqj.BagriXQUtils;

public class BagriSequenceIterator implements Iterator {
	
	private static DatatypeFactory factory;
	static {
		try {
			factory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("Can not instantiate datatype factory");
		}
	}

	private XQDataFactory xqFactory;
	private SequenceIterator iter;
	private Item current = null;
	private Item next = null;
	
	public BagriSequenceIterator(XQDataFactory xqFactory, SequenceIterator iter) {
		this.xqFactory = xqFactory;
		this.iter = iter;
		try {
			current = iter.next();
			next = iter.next();
		} catch (XPathException e) {
			//
		}
	}

	@Override
	public boolean hasNext() {
		//if ((SequenceIterator.LOOKAHEAD & iter.getProperties()) != 0) {
		//	return ((LookaheadIterator) iter).hasNext();
		//}
		return current != null;
	}
	
	@Override
	public Object next() {
		Item item = current;
		current = next;
		try {
			next = iter.next();
		} catch (XPathException ex) {
			//
			// throw Runtime ex?
			// todo: log it, at least..
		}
		
		if (item != null) {
			try {
				return itemToXQItem(item);
			} catch (XPathException e) {
				// todo: log it, at least..
			} catch (XQException e) {
				// todo: log it, at least..
			}
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Operation 'remove' is not supported");
	}

    private static XMLGregorianCalendar getCalendar(CalendarValue c, int cType) { //throws XPathException {
    	GregorianCalendar cal = c.getCalendar(); 
    	switch (cType) {
    		case XQItemType.XQBASETYPE_DATE:
    			return factory.newXMLGregorianCalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 
    					cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.ZONE_OFFSET)); 
    		case XQItemType.XQBASETYPE_GDAY: 
    			return factory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					DatatypeConstants.FIELD_UNDEFINED, cal.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GMONTH:  
    			return factory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					cal.get(Calendar.MONTH) + 1, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GMONTHDAY:  
    			return factory.newXMLGregorianCalendarDate(DatatypeConstants.FIELD_UNDEFINED, 
    					cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GYEAR:  
    			return factory.newXMLGregorianCalendarDate(cal.get(Calendar.YEAR), 
    					DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_GYEARMONTH: 
    			return factory.newXMLGregorianCalendarDate(cal.get(Calendar.YEAR), 
    					cal.get(Calendar.MONTH) + 1, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
    		case XQItemType.XQBASETYPE_TIME:
    			return factory.newXMLGregorianCalendarTime(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), 
    					cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND), cal.get(Calendar.ZONE_OFFSET)); 
    		default: //XQItemType.XQBASETYPE_DATETIME 
    	}
		return factory.newXMLGregorianCalendar(cal);
    }

    private static Duration getDuration(DurationValue d, int type) { //throws XPathException {
   		switch (type) {
   			case 0: return factory.newDuration(d.getStringValue());
   			case 1: return factory.newDurationDayTime(d.getStringValue());
   			case 2: return factory.newDurationYearMonth(d.getStringValue());
   		}
   		return null;
    }

    private XQItem itemToXQItem(Item item) throws XPathException, XQException {
        if (item instanceof AtomicValue) {
        	int type;
        	Object value;
        	
            AtomicValue p = ((AtomicValue)item);
            int t = p.getItemType().getPrimitiveType();
            switch (t) {
                case StandardNames.XS_ANY_URI:
                	type = XQItemType.XQBASETYPE_ANYURI; 	
                    value = p.getStringValue();
                    break;
                case StandardNames.XS_BASE64_BINARY:
                	type = XQItemType.XQBASETYPE_BASE64BINARY;
                    value = ((Base64BinaryValue)p).getBinaryValue();
                    break;
                case StandardNames.XS_BOOLEAN:
                	type = XQItemType.XQBASETYPE_BOOLEAN;
                    value = Boolean.valueOf(((BooleanValue)p).getBooleanValue());
                    break;
                case StandardNames.XS_DATE:
                	type = XQItemType.XQBASETYPE_DATE;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_DATE);
                    break;
                case StandardNames.XS_TIME:
                	type = XQItemType.XQBASETYPE_TIME;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_TIME);
                    break;
                case StandardNames.XS_DATE_TIME:
                	type = XQItemType.XQBASETYPE_DATETIME;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_DATETIME);
                    break;
                case StandardNames.XS_DECIMAL:
                	type = XQItemType.XQBASETYPE_DECIMAL;
                    value = ((DecimalValue)p).getDecimalValue();
                    break;
                case StandardNames.XS_DOUBLE:
                	type = XQItemType.XQBASETYPE_DOUBLE;
                    value = ((DoubleValue)p).getDoubleValue();
                    break;
                case StandardNames.XS_DURATION:
                	type = XQItemType.XQBASETYPE_DURATION;
                    value = getDuration((DurationValue) p, 0);
                    break;
                case StandardNames.XS_DAY_TIME_DURATION:
                	type = XQItemType.XQBASETYPE_DAYTIMEDURATION;
                    value = getDuration((DurationValue) p, 1);
                    break;
                case StandardNames.XS_YEAR_MONTH_DURATION:
                	type = XQItemType.XQBASETYPE_YEARMONTHDURATION;
                    value = getDuration((DurationValue) p, 2);
                    break;
                case StandardNames.XS_FLOAT:
                	type = XQItemType.XQBASETYPE_FLOAT;
                    value = ((FloatValue)p).getFloatValue();
                    break;
                case StandardNames.XS_G_DAY:
                	type = XQItemType.XQBASETYPE_GDAY;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_GDAY);
                    break;
                case StandardNames.XS_G_MONTH:
                	type = XQItemType.XQBASETYPE_GMONTH;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_GMONTH);
                    break;
                case StandardNames.XS_G_MONTH_DAY:
                	type = XQItemType.XQBASETYPE_GMONTHDAY;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_GMONTHDAY);
                    break;
                case StandardNames.XS_G_YEAR:
                	type = XQItemType.XQBASETYPE_GYEAR;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_GYEAR);
                    break;
                case StandardNames.XS_G_YEAR_MONTH:
                	type = XQItemType.XQBASETYPE_GYEARMONTH;
                    value = getCalendar((CalendarValue) p, XQItemType.XQBASETYPE_GYEARMONTH);
                    break;
                case StandardNames.XS_HEX_BINARY:
                	type = XQItemType.XQBASETYPE_HEXBINARY;
                    value = ((HexBinaryValue)p).getBinaryValue();
                    break;
                case StandardNames.XS_INTEGER:
                    if (p instanceof BigIntegerValue) {
                    	type = XQItemType.XQBASETYPE_INTEGER;
                        value = ((BigIntegerValue)p).asBigInteger();
                    } else {
                        int sub = ((AtomicType)p.getItemType()).getFingerprint();
                        switch (sub) {
                            case StandardNames.XS_INTEGER:
                            	type = XQItemType.XQBASETYPE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_NEGATIVE_INTEGER:
                            	type = XQItemType.XQBASETYPE_NEGATIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_NON_NEGATIVE_INTEGER:
                            	type = XQItemType.XQBASETYPE_NONNEGATIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_NON_POSITIVE_INTEGER:
                            	type = XQItemType.XQBASETYPE_NONPOSITIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_POSITIVE_INTEGER:
                            	type = XQItemType.XQBASETYPE_POSITIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_UNSIGNED_LONG:
                            	type = XQItemType.XQBASETYPE_UNSIGNED_LONG;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_BYTE:
                            	type = XQItemType.XQBASETYPE_BYTE;
                                value = Byte.valueOf(((Int64Value)p).getStringValue());
                                break;
                            case StandardNames.XS_INT:
                            	type = XQItemType.XQBASETYPE_INT;
                                value = Integer.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_UNSIGNED_SHORT:
                            	type = XQItemType.XQBASETYPE_UNSIGNED_SHORT;
                                value = Integer.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_LONG:
                            	type = XQItemType.XQBASETYPE_LONG;
                                value = Long.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_UNSIGNED_INT:
                            	type = XQItemType.XQBASETYPE_UNSIGNED_INT;
                                value = Long.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_SHORT:
                            	type = XQItemType.XQBASETYPE_SHORT;
                                value = Short.valueOf((short)((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_UNSIGNED_BYTE:
                            	type = XQItemType.XQBASETYPE_UNSIGNED_BYTE;
                                value = Short.valueOf((short)((Int64Value)p).longValue());
                                break;
                            default:
                                throw new XPathException("Unrecognized integer subtype " + sub);
                        }
                    }
                    break;
                case StandardNames.XS_STRING:
                    value = p.getStringValue();
                    int sub = ((AtomicType)p.getItemType()).getFingerprint();
                    switch (sub) {
	                    case StandardNames.XS_NAME:
	                    	type = XQItemType.XQBASETYPE_NAME;
	                    	break;
	                    case StandardNames.XS_NCNAME:
	                    	type = XQItemType.XQBASETYPE_NCNAME;
	                        break;
	                    case StandardNames.XS_NMTOKEN:
	                    	type = XQItemType.XQBASETYPE_NMTOKEN;
	                    	break;
	                    default:
	                    	type = XQItemType.XQBASETYPE_STRING;
                    }
                    break;
                case StandardNames.XS_QNAME:
                	type = XQItemType.XQBASETYPE_QNAME;
                    value = ((QualifiedNameValue)p).toJaxpQName();
                    break;
                case StandardNames.XS_UNTYPED_ATOMIC:
                	type = XQItemType.XQBASETYPE_UNTYPEDATOMIC;
                    value = p.getStringValue();
                    break;
                default:
                    throw new XPathException("unsupported type");
            }
			XQItemType xqt = xqFactory.createAtomicType(type); 
        	return xqFactory.createItemFromObject(value, xqt);
        } else if (item instanceof NodeInfo) {
        	org.w3c.dom.Node node = NodeOverNodeInfo.wrap((NodeInfo)item);
        	XQItemType xqt = BagriXQUtils.getTypeForNode(xqFactory, node);
        	return xqFactory.createItemFromNode(node, xqt);
        } else if (item instanceof ObjectValue) {
        	XQItemType xqt;
        	Object value = ((ObjectValue) item).getObject();
        	if (value instanceof XQItem) {
        		return (XQItem) value;
        	} else {
				xqt = BagriXQUtils.getTypeForObject(xqFactory, value);
            	return xqFactory.createItemFromObject(value, xqt);
        	}
        }
        return null; //item.;
    }
}
