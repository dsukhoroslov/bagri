package com.bagri.xquery.saxon;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.system.DataType;
import com.bagri.support.util.XMLUtils;
import com.bagri.support.util.XQUtils;
import com.bagri.xquery.saxon.ext.doc.GetDocumentContent;
import com.bagri.xquery.saxon.ext.doc.GetDocumentUris;
import com.bagri.xquery.saxon.ext.doc.QueryDocumentUris;
import com.bagri.xquery.saxon.ext.doc.RemoveDocuments;
import com.bagri.xquery.saxon.ext.doc.RemoveDocument;
import com.bagri.xquery.saxon.ext.doc.StoreDocument;
import com.bagri.xquery.saxon.ext.http.HttpGet;
import com.bagri.xquery.saxon.ext.tx.BeginTransaction;
import com.bagri.xquery.saxon.ext.tx.CommitTransaction;
import com.bagri.xquery.saxon.ext.tx.RollbackTransaction;
import com.bagri.xquery.saxon.ext.util.GetUuid;
import com.bagri.xquery.saxon.ext.util.LogOutput;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Sender;
import net.sf.saxon.evpull.PullEventSource;
import net.sf.saxon.evpull.StaxToEventBridge;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.*;

import static net.sf.saxon.om.StandardNames.*;
import static javax.xml.xquery.XQItemType.*;

public class SaxonUtils {
	
	public static final int saxon_xquery_version = 31;

    public static Object itemToObject(Item item) throws XPathException {
        if (item instanceof AtomicValue) {
            AtomicValue p = ((AtomicValue) item).asAtomic(); // ((AtomicValue)item);
            int t = p.getItemType().getPrimitiveType();
            switch (t) {
                case XS_ANY_URI:
                case XS_ANY_ATOMIC_TYPE:
                    return p.getStringValue();
                case XS_BASE64_BINARY:
                    return ((Base64BinaryValue)p).getBinaryValue();
                case XS_BOOLEAN:
                    return Boolean.valueOf(((BooleanValue)p).getBooleanValue());
                case XS_DATE:
                    return getCalendar((CalendarValue) p, XQBASETYPE_DATE); // new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DATE_TIME:
                    return getCalendar((CalendarValue) p, XQBASETYPE_DATETIME); // new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DECIMAL:
                    return ((DecimalValue)p).getDecimalValue();
                case XS_DOUBLE:
                    return new Double(((DoubleValue)p).getDoubleValue());
                case XS_DURATION:
                    return getDuration((DurationValue)p, XQBASETYPE_DURATION);
                case XS_FLOAT:
                    return new Float(((FloatValue)p).getFloatValue());
                case XS_G_DAY:
                	return getCalendar((CalendarValue) p, XQBASETYPE_GDAY); 
                case XS_G_MONTH:
                	return getCalendar((CalendarValue) p, XQBASETYPE_GMONTH); 
                case XS_G_MONTH_DAY:
                	return getCalendar((CalendarValue) p, XQBASETYPE_GMONTHDAY); 
                case XS_G_YEAR:
                	return getCalendar((CalendarValue) p, XQBASETYPE_GYEAR); 
                case XS_G_YEAR_MONTH:
                	return getCalendar((CalendarValue) p, XQBASETYPE_GYEARMONTH); 
                case XS_HEX_BINARY:
                    return ((HexBinaryValue)p).getBinaryValue();
                case XS_INTEGER:
                    if (p instanceof BigIntegerValue) {
                        return ((BigIntegerValue)p).asBigInteger();
                    } else {
                        int sub = ((AtomicType)p.getItemType()).getFingerprint();
                        switch (sub) {
                            case XS_INTEGER:
                            case XS_NEGATIVE_INTEGER:
                            case XS_NON_NEGATIVE_INTEGER:
                            case XS_NON_POSITIVE_INTEGER:
                            case XS_POSITIVE_INTEGER:
                            case XS_UNSIGNED_LONG:
                                return BigInteger.valueOf(((Int64Value)p).longValue());
                            case XS_BYTE:
                                return Byte.valueOf((byte)((Int64Value)p).longValue());
                            case XS_INT:
                            case XS_UNSIGNED_SHORT:
                                return Integer.valueOf((int)((Int64Value)p).longValue());
                            case XS_LONG:
                            case XS_UNSIGNED_INT:
                                return Long.valueOf(((Int64Value)p).longValue());
                            case XS_SHORT:
                            case XS_UNSIGNED_BYTE:
                                return Short.valueOf((short)((Int64Value)p).longValue());
                            default:
                                throw new XPathException("Unrecognized integer subtype " + sub);
                        }
                    }
                case XS_QNAME:
                    return ((QualifiedNameValue)p).toJaxpQName();
                case XS_STRING:
                case XS_UNTYPED_ATOMIC:
                    return p.getStringValue();
                case XS_TIME:
                    return getCalendar((CalendarValue) p, XQBASETYPE_TIME);
                case XS_DAY_TIME_DURATION:
                    return getDuration((DurationValue)p, XQBASETYPE_DAYTIMEDURATION);
                case XS_YEAR_MONTH_DURATION:
                    return getDuration((DurationValue)p, XQBASETYPE_YEARMONTHDURATION);
                default:
                    throw new XPathException("unsupported type: " + t);
            }
        } else if (item instanceof NodeInfo) {
            return NodeOverNodeInfo.wrap((NodeInfo)item);
        } else if (item instanceof ObjectValue) {
        	Object value = ((ObjectValue) item).getObject();
        	if (value instanceof XQItem) {
        		//
        		//return ((XQItem) value).getObject();
        		return value;
        	}
        } else if (item instanceof ArrayItemImpl) {
        	return ((ArrayItemImpl) item).getSource();
        } else if (item instanceof ArrayItem) {
        	return itemToList((ArrayItem) item);
        } else if (item instanceof MapItemImpl) {
        	return ((MapItemImpl) item).getSource();
        } else if (item instanceof MapItem) {
        	return itemToMap((MapItem) item);
        }
        return item;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Item objectToItem(Object value, Configuration config) throws XPathException {
        if (value == null) {
            return null;
        }

        // convert to switch..
        if (value instanceof Boolean) {
            return BooleanValue.get(((Boolean)value).booleanValue());
        } else if (value instanceof byte[]) {
            return new HexBinaryValue((byte[])value);
        } else if (value instanceof Byte) {
            return new Int64Value(((Byte)value).byteValue(), BuiltInAtomicType.BYTE, false);
        } else if (value instanceof Float) {
            return new FloatValue(((Float)value).floatValue());
        } else if (value instanceof Double) {
            return new DoubleValue(((Double)value).doubleValue());
        } else if (value instanceof Integer) {
            return new Int64Value(((Integer)value).intValue(), BuiltInAtomicType.INT, false);
        } else if (value instanceof Long) {
            return new Int64Value(((Long)value).longValue(), BuiltInAtomicType.LONG, false);
        } else if (value instanceof Short) {
            return new Int64Value(((Short)value).shortValue(), BuiltInAtomicType.SHORT, false);
        } else if (value instanceof String) {
            return new StringValue((String)value);
        } else if (value instanceof BigDecimal) {
            return new DecimalValue((BigDecimal)value);
        } else if (value instanceof BigInteger) {
            return new BigIntegerValue((BigInteger)value);
        } else if (value instanceof SaxonDuration) {
            return ((SaxonDuration)value).getDurationValue();
        } else if (value instanceof Duration) {
            // this is simpler and safer (but perhaps slower) than extracting all the components
            //return DurationValue.makeDuration(value.toString()).asAtomic();
          	Duration dv = (Duration) value;
          	return new DurationValue(dv.getSign() >= 0, dv.getYears(), dv.getMonths(), dv.getDays(), 
          			dv.getHours(), dv.getMinutes(), dv.getSeconds(), 0); // take correct millis..
        } else if (value instanceof SaxonXMLGregorianCalendar) {
            return ((SaxonXMLGregorianCalendar)value).toCalendarValue();
        } else if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar g = (XMLGregorianCalendar)value;
            QName gtype = g.getXMLSchemaType();
            if (gtype.equals(DatatypeConstants.DATETIME)) {
                return DateTimeValue.makeDateTimeValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.DATE)) {
                return DateValue.makeDateValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.TIME)) {
                return TimeValue.makeTimeValue(value.toString()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GYEAR)) {
                return GYearValue.makeGYearValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GYEARMONTH)) {
                return GYearMonthValue.makeGYearMonthValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GMONTH)) {
             	// a workaround for W3C schema bug
               	String val = value.toString();
               	if (val.endsWith("--")) {
               		val = val.substring(0, val.length() - 2);
               	}
                return GMonthValue.makeGMonthValue(val).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GMONTHDAY)) {
                return GMonthDayValue.makeGMonthDayValue(value.toString()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GDAY)) {
                return GDayValue.makeGDayValue(value.toString()).asAtomic();
            } else {
                throw new AssertionError("Unknown Gregorian date type");
            }
        } else if (value instanceof QName) {
            QName q = (QName)value;
            return new QNameValue(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()); //BuiltInAtomicType.QNAME, null);
        } else if (value instanceof URI) {
          	return new AnyURIValue(value.toString());
        } else if (value instanceof List) {
           	return new ArrayItemImpl((List) value, config);
        } else if (value instanceof Collection) {
           	return new ArrayItemImpl(new ArrayList<>((Collection) value), config);
        } else if (value instanceof Map) {
           	return new MapItemImpl((Map) value, config);
        } else {
           	return new ObjectValue(value);
        }
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Item convertToItem(Object value, Configuration config, BuiltInAtomicType type) throws XPathException {
        if (value == null) {
            return null;
        }

        // convert to switch..
        if (value instanceof Boolean) {
            return BooleanValue.get(((Boolean)value).booleanValue());
        } else if (value instanceof byte[]) {
            return new HexBinaryValue((byte[])value);
        } else if (value instanceof Byte) {
            return new Int64Value(((Byte)value).byteValue(), type, false); //BuiltInAtomicType.BYTE, false);
        } else if (value instanceof Float) {
            return new FloatValue(((Float)value).floatValue(), type);
        } else if (value instanceof Double) {
            return new DoubleValue(((Double)value).doubleValue(), type);
        } else if (value instanceof Integer) {
            return new Int64Value(((Integer)value).intValue(), type, false); //BuiltInAtomicType.INT, false);
        } else if (value instanceof Long) {
            return new Int64Value(((Long)value).longValue(), type, false); //BuiltInAtomicType.LONG, false);
        } else if (value instanceof Short) {
            return new Int64Value(((Short)value).shortValue(), type, false); //BuiltInAtomicType.SHORT, false);
        } else if (value instanceof String) {
            return new StringValue((String)value, type);
        } else if (value instanceof BigDecimal) {
            return new DecimalValue((BigDecimal)value);
        } else if (value instanceof BigInteger) {
            return new BigIntegerValue((BigInteger)value, type);
        } else if (value instanceof SaxonDuration) {
            return ((SaxonDuration)value).getDurationValue();
        } else if (value instanceof Duration) {
            // this is simpler and safer (but perhaps slower) than extracting all the components
            //return DurationValue.makeDuration(value.toString()).asAtomic();
         	Duration dv = (Duration) value;
           	return new DurationValue(dv.getSign() >= 0, dv.getYears(), dv.getMonths(), dv.getDays(), 
           			dv.getHours(), dv.getMinutes(), dv.getSeconds(), 0, type); // take correct millis..
        } else if (value instanceof SaxonXMLGregorianCalendar) {
            return ((SaxonXMLGregorianCalendar)value).toCalendarValue();
        } else if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar g = (XMLGregorianCalendar)value;
            QName gtype = g.getXMLSchemaType();
            if (gtype.equals(DatatypeConstants.DATETIME)) {
                return DateTimeValue.makeDateTimeValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.DATE)) {
                return DateValue.makeDateValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.TIME)) {
                return TimeValue.makeTimeValue(value.toString()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GYEAR)) {
                return GYearValue.makeGYearValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GYEARMONTH)) {
                return GYearMonthValue.makeGYearMonthValue(value.toString(), config.getConversionRules()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GMONTH)) {
             	// a workaround for W3C schema bug
               	String val = value.toString();
               	if (val.endsWith("--")) {
               		val = val.substring(0, val.length() - 2);
               	}
                return GMonthValue.makeGMonthValue(val).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GMONTHDAY)) {
                return GMonthDayValue.makeGMonthDayValue(value.toString()).asAtomic();
            } else if (gtype.equals(DatatypeConstants.GDAY)) {
                return GDayValue.makeGDayValue(value.toString()).asAtomic();
            } else {
                throw new AssertionError("Unknown Gregorian date type");
            }
        } else if (value instanceof QName) {
            QName q = (QName)value;
            return new QNameValue(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart(), type);
        } else if (value instanceof URI) {
          	return new AnyURIValue(value.toString());
        } else if (value instanceof List) {
        	// use type somehow?
           	return new ArrayItemImpl((List) value, config);
        } else if (value instanceof Collection) {
           	return new ArrayItemImpl(new ArrayList<>((Collection) value), config);
        } else if (value instanceof Map) {
           	return new MapItemImpl((Map) value, config);
        } else {
        	return new ObjectValue(value);
            //throw new XPathException("Java object cannot be converted to an XQuery value");
        }
    }

    public static Item convertToItem(Object value, Configuration config, int kind) throws XPathException {
    	if (value instanceof String) {
    		try {
				value = XMLUtils.textToDocument((String) value);
			} catch (IOException ex) {
				throw new XPathException(ex);
			}
    	}
    	
    	if (value instanceof Node) {
	    	JPConverter jp;
			switch (kind) {
				case XQITEMKIND_DOCUMENT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Document.class, config);
			        break;
				case XQITEMKIND_ELEMENT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Element.class, config);
			        break;
				case XQITEMKIND_ATTRIBUTE:
			        jp = DOMObjectModel.getInstance().getJPConverter(Attr.class, config);
			        break;
				case XQITEMKIND_COMMENT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Comment.class, config);
			        break;
				case XQITEMKIND_PI:
			        jp = DOMObjectModel.getInstance().getJPConverter(ProcessingInstruction.class, config);
			        break;
				case XQITEMKIND_TEXT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Text.class, config);
			        break;
				//case XQItemType.XQITEMKIND_NODE:
				default:
			        jp = DOMObjectModel.getInstance().getJPConverter(Node.class, config);
			}
	        //return Value.asItem(DOMObjectModel.getInstance().convertObjectToXPathValue(value, config));
	        return SequenceTool.asItem(jp.convert(value, new EarlyEvaluationContext(config)));
	    } else if (value instanceof Source) {
	        // Saxon extension to the XQJ specification
	        PipelineConfiguration pipe = config.makePipelineConfiguration();
	        Builder b = new TinyBuilder(pipe);
	        Sender.send((Source)value, b, null);
	        NodeInfo node = b.getCurrentRoot();
	        b.reset();
	        return node;
	    } else if (value instanceof XMLStreamReader) {
	        // Saxon extension to the XQJ specification
	        StaxToEventBridge bridge = new StaxToEventBridge();
	        bridge.setXMLStreamReader((XMLStreamReader)value);
	        PipelineConfiguration pipe = config.makePipelineConfiguration();
	        bridge.setPipelineConfiguration(pipe);
	        Builder b = new TinyBuilder(pipe);
	        Sender.send(new PullEventSource(bridge), b, null);
	        NodeInfo node = b.getCurrentRoot();
	        b.reset();
	        return node;
	    }
    	return null;
    }
    
	public static Item convertXQItem(XQItem xqItem, Configuration config) throws XQException, XPathException {
		BuiltInAtomicType type = getAtomicType(xqItem.getItemType());
		if (type == null) {
			return convertToItem(xqItem.getObject(), config, xqItem.getItemType().getItemKind());
		}
		return convertToItem(xqItem.getObject(), config, type);
	}
    
	public static BuiltInAtomicType getAtomicType(XQItemType type) throws XQException {
		
		int kind = type.getItemKind();
		if (XQUtils.isBaseTypeSupported(kind)) {
			switch (type.getBaseType()) {
				case XQBASETYPE_ANYATOMICTYPE: return BuiltInAtomicType.ANY_ATOMIC;
				case XQBASETYPE_ANYSIMPLETYPE: return null; 
				case XQBASETYPE_ANYTYPE: return null; 
				case XQBASETYPE_ANYURI: return BuiltInAtomicType.ANY_URI; 
				case XQBASETYPE_BASE64BINARY: return BuiltInAtomicType.BASE64_BINARY; 
				case XQBASETYPE_BOOLEAN: return BuiltInAtomicType.BOOLEAN;
				case XQBASETYPE_BYTE: return BuiltInAtomicType.BYTE;
				case XQBASETYPE_DATE: return BuiltInAtomicType.DATE;
				case XQBASETYPE_DATETIME: return BuiltInAtomicType.DATE_TIME;
				case XQBASETYPE_DAYTIMEDURATION: return BuiltInAtomicType.DAY_TIME_DURATION;
				case XQBASETYPE_DECIMAL: return BuiltInAtomicType.DECIMAL;
				case XQBASETYPE_DOUBLE: return BuiltInAtomicType.DOUBLE;
				case XQBASETYPE_DURATION: return BuiltInAtomicType.DURATION;
				case XQBASETYPE_ENTITIES: return null; 
				case XQBASETYPE_ENTITY: return BuiltInAtomicType.ENTITY;
				case XQBASETYPE_FLOAT: return BuiltInAtomicType.FLOAT;
				case XQBASETYPE_GDAY: return BuiltInAtomicType.G_DAY;
				case XQBASETYPE_GMONTH: return BuiltInAtomicType.G_MONTH;
				case XQBASETYPE_GMONTHDAY: return BuiltInAtomicType.G_MONTH_DAY;
				case XQBASETYPE_GYEAR: return BuiltInAtomicType.G_YEAR;
				case XQBASETYPE_GYEARMONTH: return BuiltInAtomicType.G_YEAR_MONTH;
				case XQBASETYPE_HEXBINARY: return BuiltInAtomicType.HEX_BINARY;
				case XQBASETYPE_ID: return BuiltInAtomicType.ID;
				case XQBASETYPE_IDREF: return BuiltInAtomicType.IDREF;
				case XQBASETYPE_IDREFS: return null;
				case XQBASETYPE_INT: return BuiltInAtomicType.INT;
				case XQBASETYPE_INTEGER: return BuiltInAtomicType.INTEGER;
				case XQBASETYPE_LANGUAGE: return BuiltInAtomicType.LANGUAGE;
				case XQBASETYPE_LONG: return BuiltInAtomicType.LONG;
				case XQBASETYPE_NAME: return BuiltInAtomicType.NAME;
				case XQBASETYPE_NCNAME: return BuiltInAtomicType.NCNAME;
				case XQBASETYPE_NEGATIVE_INTEGER: return BuiltInAtomicType.NEGATIVE_INTEGER;
				case XQBASETYPE_NMTOKEN: return BuiltInAtomicType.NMTOKEN;
				case XQBASETYPE_NMTOKENS: return null;
				case XQBASETYPE_NONNEGATIVE_INTEGER: return BuiltInAtomicType.NON_NEGATIVE_INTEGER;
				case XQBASETYPE_NONPOSITIVE_INTEGER: return BuiltInAtomicType.NON_POSITIVE_INTEGER;
				case XQBASETYPE_NORMALIZED_STRING: return BuiltInAtomicType.NORMALIZED_STRING;
				case XQBASETYPE_NOTATION: return BuiltInAtomicType.NOTATION;
				case XQBASETYPE_POSITIVE_INTEGER: return BuiltInAtomicType.POSITIVE_INTEGER;
				case XQBASETYPE_QNAME: return BuiltInAtomicType.QNAME;
				case XQBASETYPE_SHORT: return BuiltInAtomicType.SHORT;
				case XQBASETYPE_STRING: return BuiltInAtomicType.STRING;
				case XQBASETYPE_TIME: return BuiltInAtomicType.TIME;
				case XQBASETYPE_TOKEN: return BuiltInAtomicType.TOKEN;
				case XQBASETYPE_UNSIGNED_BYTE: return BuiltInAtomicType.UNSIGNED_BYTE;
				case XQBASETYPE_UNSIGNED_INT: return BuiltInAtomicType.UNSIGNED_INT;
				case XQBASETYPE_UNSIGNED_LONG: return BuiltInAtomicType.UNSIGNED_LONG;
				case XQBASETYPE_UNSIGNED_SHORT: return BuiltInAtomicType.UNSIGNED_SHORT;
				case XQBASETYPE_UNTYPED: return null;
				case XQBASETYPE_UNTYPEDATOMIC: return BuiltInAtomicType.UNTYPED_ATOMIC;
				case XQBASETYPE_YEARMONTHDURATION: return BuiltInAtomicType.YEAR_MONTH_DURATION;
			}
		}
		return null;
	}
	
	public static List<Object> itemToList(ArrayItem ai) throws XPathException {
		List<Object> result = new ArrayList<>(ai.size());
    	for (Sequence sq: ai) {
    		result.add(itemToObject(sq.head().atomize().head()));
    	}
    	return result;
	}
		
	public static Map<String, Object> itemToMap(MapItem mi) throws XPathException {
    	AtomicValue key;
    	AtomicIterator itr = mi.keys();
		Map<String, Object> result = new HashMap<>(mi.size());
    	while ((key = itr.next()) != null) {
    		Sequence value = mi.get(key);
    		result.put(key.getStringValue(), itemToObject(value.head().atomize().head()));
    	}
    	return result;
	}
	
	public static SequenceType type2Sequence(DataType type) {
		ItemType it = type2Item(type.getType());
		int cardinality;
		switch (type.getCardinality()) {
			case one_or_more: cardinality = StaticProperty.ALLOWS_ONE_OR_MORE; break; 
			case zero_or_one: cardinality = StaticProperty.ALLOWS_ZERO_OR_ONE; break; 
			case zero_or_more: cardinality = StaticProperty.ALLOWS_ZERO_OR_MORE; break;
			default: cardinality = StaticProperty.ALLOWS_ONE;  
		}
		return SequenceType.makeSequenceType(it, cardinality);
	}
	
    public static com.bagri.core.system.Cardinality getCardinality(int cardinality) {
    	switch (cardinality) {
    		case StaticProperty.ALLOWS_ONE_OR_MORE: return com.bagri.core.system.Cardinality.one_or_more;  
    		case StaticProperty.ALLOWS_ZERO_OR_ONE: return com.bagri.core.system.Cardinality.zero_or_one;
    		case StaticProperty.ALLOWS_ZERO_OR_MORE: return com.bagri.core.system.Cardinality.zero_or_more;
    	}
    	return com.bagri.core.system.Cardinality.one;  
    }

	public static ItemType type2Item(String type) {

		switch (type) {
			case "anyAtomicType": return BuiltInAtomicType.ANY_ATOMIC;
			//case "anySimpleType": return XQBASETYPE_ANYSIMPLETYPE;
			//case "anyType": return XQBASETYPE_ANYTYPE;
			case "anyURI": return BuiltInAtomicType.ANY_URI;
			case "base64Binary": return BuiltInAtomicType.BASE64_BINARY;
			case "boolean": return BuiltInAtomicType.BOOLEAN; 
			case "byte": return BuiltInAtomicType.BYTE; 
			case "date": return BuiltInAtomicType.DATE;
			case "dateTime": return BuiltInAtomicType.DATE_TIME; 
    		case "dayTimeDuration": return BuiltInAtomicType.DAY_TIME_DURATION;
    		case "decimal": return BuiltInAtomicType.DECIMAL;
			case "double": return BuiltInAtomicType.DOUBLE;
    		case "duration": return BuiltInAtomicType.DURATION;
    		//case "ENTITIES": return BuiltInAtomicType.ENTITIES;
    		case "ENTITY": return BuiltInAtomicType.ENTITY;
			case "float": return BuiltInAtomicType.FLOAT;
    		case "gDay": return BuiltInAtomicType.G_DAY;
    		case "gMonth": return BuiltInAtomicType.G_MONTH;
    		case "gMonthDay": return BuiltInAtomicType.G_MONTH_DAY;
    		case "gYear": return BuiltInAtomicType.G_YEAR;
    		case "gYearMonth": return BuiltInAtomicType.G_YEAR_MONTH;
    		case "hexBinary": return BuiltInAtomicType.HEX_BINARY;
    		case "ID": return BuiltInAtomicType.ID;
    		case "IDREF": return BuiltInAtomicType.IDREF;
    		//case "IDREFS": return BuiltInAtomicType.IDREFS;
			case "int": return BuiltInAtomicType.INT; 
			case "integer": return BuiltInAtomicType.INTEGER;
    		case "language": return BuiltInAtomicType.LANGUAGE;
			case "long": return BuiltInAtomicType.LONG;
    		case "Name": return BuiltInAtomicType.NAME;
    		case "NCName": return BuiltInAtomicType.NCNAME;
    		case "negativeInteger": return BuiltInAtomicType.NEGATIVE_INTEGER;
    		case "NMTOKEN": return BuiltInAtomicType.NMTOKEN;
    		//case "NMTOKENS": return BuiltInAtomicType.NMTOKENS;
    		case "nonNegativeInteger": return BuiltInAtomicType.NON_NEGATIVE_INTEGER;
    		case "nonPositiveInteger": return BuiltInAtomicType.NON_POSITIVE_INTEGER;
    		case "normalizedString": return BuiltInAtomicType.NORMALIZED_STRING;
    		case "NOTATION": return BuiltInAtomicType.NOTATION;
    		case "positiveInteger": return BuiltInAtomicType.POSITIVE_INTEGER;
    		case "QName": return BuiltInAtomicType.QNAME;
			case "short": return BuiltInAtomicType.SHORT; 
			case "string": return BuiltInAtomicType.STRING; 
    		case "time": return BuiltInAtomicType.TIME;
    		case "token": return BuiltInAtomicType.TOKEN;
    		case "unsignedByte": return BuiltInAtomicType.UNSIGNED_BYTE;
    		case "unsignedInt": return BuiltInAtomicType.UNSIGNED_INT;
    		case "unsignedLong": return BuiltInAtomicType.UNSIGNED_LONG;
    		case "unsignedShort": return BuiltInAtomicType.UNSIGNED_SHORT;
    		//case "untyped": return BuiltInAtomicType.UNTYPED;
    		case "untypedAtomic": return BuiltInAtomicType.UNTYPED_ATOMIC;
    		case "yearMonthDuration": return BuiltInAtomicType.YEAR_MONTH_DURATION;
		}
		return BuiltInAtomicType.ANY_ATOMIC; 
	}

    private static XMLGregorianCalendar getCalendar(CalendarValue c, int cType) { 
    	GregorianCalendar cal = c.getCalendar(); 
    	return XMLUtils.getXMLCalendar(cal, cType);
    }

    private static Duration getDuration(DurationValue d, int type) { 
    	return XMLUtils.getXMLDuration(d.getStringValue(), type);
    }

    public static String getTypeName(ItemType type) {
    	if (type.isAtomicType()) {
    		return type.getAtomizedItemType().getTypeName().getLocalPart();
    	}
    	return type.toString(); 
    }

    @SuppressWarnings({ "rawtypes" })
	public static XQItemAccessor itemToXQItem(Item item, XQDataFactory xqFactory) throws XPathException, XQException {
        if (item instanceof AtomicValue) {
        	int type;
            Object value;
        	
            AtomicValue p = ((AtomicValue)item);
            int t = p.getItemType().getPrimitiveType();
            switch (t) {
                case XS_ANY_URI:
                    type = XQBASETYPE_ANYURI; 	
                    value = p.getStringValue();
                    break;
                case XS_ANY_ATOMIC_TYPE:
                    type = XQBASETYPE_ANYATOMICTYPE; 	
                    value = p.getStringValue();
                    break;
                case XS_BASE64_BINARY:
                    type = XQBASETYPE_BASE64BINARY;
                    value = ((Base64BinaryValue)p).getBinaryValue();
                    break;
                case XS_BOOLEAN:
                    type = XQBASETYPE_BOOLEAN;
                    value = Boolean.valueOf(((BooleanValue)p).getBooleanValue());
                    break;
                case XS_DATE:
                    type = XQBASETYPE_DATE;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_DATE);
                    break;
                case XS_TIME:
                    type = XQBASETYPE_TIME;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_TIME);
                    break;
                case XS_DATE_TIME:
                    type = XQBASETYPE_DATETIME;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_DATETIME);
                    break;
                case XS_DECIMAL:
                    type = XQBASETYPE_DECIMAL;
                    value = ((DecimalValue)p).getDecimalValue();
                    break;
                case XS_DOUBLE:
                    type = XQBASETYPE_DOUBLE;
                    value = ((DoubleValue)p).getDoubleValue();
                    break;
                case XS_DURATION:
                    type = XQBASETYPE_DURATION;
                    value = getDuration((DurationValue) p, type);
                    break;
                case XS_DAY_TIME_DURATION:
                    type = XQBASETYPE_DAYTIMEDURATION;
                    value = getDuration((DurationValue) p, type);
                    break;
                case XS_YEAR_MONTH_DURATION:
                    type = XQBASETYPE_YEARMONTHDURATION;
                    value = getDuration((DurationValue) p, type);
                    break;
                case XS_FLOAT:
                    type = XQBASETYPE_FLOAT;
                    value = ((FloatValue)p).getFloatValue();
                    break;
                case XS_G_DAY:
                    type = XQBASETYPE_GDAY;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_GDAY);
                    break;
                case XS_G_MONTH:
                    type = XQBASETYPE_GMONTH;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_GMONTH);
                    break;
                case XS_G_MONTH_DAY:
                    type = XQBASETYPE_GMONTHDAY;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_GMONTHDAY);
                    break;
                case XS_G_YEAR:
                    type = XQBASETYPE_GYEAR;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_GYEAR);
                    break;
                case XS_G_YEAR_MONTH:
                    type = XQBASETYPE_GYEARMONTH;
                    value = getCalendar((CalendarValue) p, XQBASETYPE_GYEARMONTH);
                    break;
                case XS_HEX_BINARY:
                    type = XQBASETYPE_HEXBINARY;
                    value = ((HexBinaryValue)p).getBinaryValue();
                    break;
                case XS_INTEGER:
                    if (p instanceof BigIntegerValue) {
                    	type = XQBASETYPE_INTEGER;
                        value = ((BigIntegerValue)p).asBigInteger();
                    } else {
                        int sub = ((AtomicType)p.getItemType()).getFingerprint();
                        switch (sub) {
                            case XS_INTEGER:
                            	type = XQBASETYPE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case XS_NEGATIVE_INTEGER:
                            	type = XQBASETYPE_NEGATIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case XS_NON_NEGATIVE_INTEGER:
                            	type = XQBASETYPE_NONNEGATIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case XS_NON_POSITIVE_INTEGER:
                            	type = XQBASETYPE_NONPOSITIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case XS_POSITIVE_INTEGER:
                            	type = XQBASETYPE_POSITIVE_INTEGER;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case XS_UNSIGNED_LONG:
                            	type = XQBASETYPE_UNSIGNED_LONG;
                                value = BigInteger.valueOf(((Int64Value)p).longValue());
                                break;
                            case XS_BYTE:
                            	type = XQBASETYPE_BYTE;
                                value = Byte.valueOf(((Int64Value)p).getStringValue());
                                break;
                            case XS_INT:
                            	type = XQBASETYPE_INT;
                                value = Integer.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case XS_UNSIGNED_SHORT:
                            	type = XQBASETYPE_UNSIGNED_SHORT;
                                value = Integer.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case XS_LONG:
                            	type = XQBASETYPE_LONG;
                                value = Long.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case XS_UNSIGNED_INT:
                            	type = XQBASETYPE_UNSIGNED_INT;
                                value = Long.valueOf((int)((Int64Value)p).longValue());
                                break;
                            case StandardNames.XS_SHORT:
                            	type = XQBASETYPE_SHORT;
                                value = Short.valueOf((short)((Int64Value)p).longValue());
                                break;
                            case XS_UNSIGNED_BYTE:
                            	type = XQBASETYPE_UNSIGNED_BYTE;
                                value = Short.valueOf((short)((Int64Value)p).longValue());
                                break;
                            default:
                                throw new XPathException("Unrecognized integer subtype " + sub);
                        }
                    }
                    break;
                case XS_STRING:
                    value = p.getStringValue();
                    int sub = ((AtomicType)p.getItemType()).getFingerprint();
                    switch (sub) {
	                    case XS_NAME:
	                    	type = XQBASETYPE_NAME;
	                    	break;
	                    case XS_NCNAME:
	                    	type = XQBASETYPE_NCNAME;
	                        break;
	                    case XS_NMTOKEN:
	                    	type = XQBASETYPE_NMTOKEN;
	                    	break;
	                    default:
	                    	type = XQBASETYPE_STRING;
                    }
                    break;
                case XS_QNAME:
                	type = XQBASETYPE_QNAME;
                    value = ((QualifiedNameValue)p).toJaxpQName();
                    break;
                case XS_UNTYPED_ATOMIC:
                	type = XQBASETYPE_UNTYPEDATOMIC;
                    value = p.getStringValue();
                    break;
                default:
                    throw new XPathException("unsupported type: " + t);
            }
			XQItemType xqt = xqFactory.createAtomicType(type); 
        	return xqFactory.createItemFromObject(value, xqt);
        } else if (item instanceof NodeInfo) {
        	org.w3c.dom.Node node = NodeOverNodeInfo.wrap((NodeInfo)item);
        	XQItemType xqt = XQUtils.getTypeForNode(xqFactory, node);
        	return xqFactory.createItemFromNode(node, xqt);
        } else if (item instanceof ObjectValue) {
        	Object value = ((ObjectValue) item).getObject();
        	if (value instanceof XQItem) { //Accessor) {
        		return (XQItemAccessor) value;
        	} else {
            	XQItemType xqt = XQUtils.getTypeForObject(xqFactory, value);
            	return xqFactory.createItemFromObject(value, xqt);
        	}
        } else if (item instanceof MapItem) {
        	// TODO: we'll need a new XQ type for maps, probably..
        	AtomicValue key;
        	MapItem mi = (MapItem) item;
        	AtomicIterator itr = mi.keys();
        	List<XQSequence> pairs = new ArrayList<>(); 
        	while ((key = itr.next()) != null) {
        		Sequence val = mi.get(key);
        		List<XQItemAccessor> pair = new ArrayList<>(2);
        		pair.add(itemToXQItem(key, xqFactory));
        		if (val instanceof Item) {
        			pair.add(itemToXQItem((Item) val, xqFactory));
        		} else {
        			pair.add(xqFactory.createSequence(new XQIterator(xqFactory, val.iterate())));
        		}
        		pairs.add(xqFactory.createSequence(pair.iterator()));
        	}
        	return xqFactory.createSequence(pairs.iterator());
        } else if (item instanceof Sequence) {
        	// ArrayItem is also handled here
        	return xqFactory.createSequence(new XQIterator(xqFactory, ((Sequence) item).iterate()));
        }
        return null; 
    }
    
	public static Properties sequence2Properties(Sequence sq) throws XPathException {
		Properties props = new Properties();
		Item head = sq.head();
		if (head instanceof MapItem) {
			props.putAll(itemToMap((MapItem) head));
		} else {
			SequenceIterator itr = sq.iterate();
			do {
				Item item = itr.next();
				if (item != null) {
					String prop = item.getStringValue();
					int pos = prop.indexOf("=");
					if (pos > 0) {
						props.setProperty(prop.substring(0, pos), prop.substring(pos + 1));
					}
				} else {
					break;
				}
			} while (true);
		}
		return props;
	}
	
	public static void registerExtensions(Configuration config, SchemaRepository xRepo) {
        config.registerExtensionFunction(new GetUuid());
        config.registerExtensionFunction(new LogOutput());
        config.registerExtensionFunction(new HttpGet());
        if (xRepo == null) {
            config.registerExtensionFunction(new GetDocumentContent(null));
            config.registerExtensionFunction(new GetDocumentUris(null));
            config.registerExtensionFunction(new RemoveDocument(null));
            config.registerExtensionFunction(new StoreDocument(null));
            config.registerExtensionFunction(new RemoveDocuments(null));
            config.registerExtensionFunction(new QueryDocumentUris(null));
            config.registerExtensionFunction(new BeginTransaction(null));
            config.registerExtensionFunction(new CommitTransaction(null));
            config.registerExtensionFunction(new RollbackTransaction(null));
        } else {
            config.registerExtensionFunction(new GetDocumentContent(xRepo.getDocumentManagement()));
            config.registerExtensionFunction(new GetDocumentUris(xRepo.getDocumentManagement()));
            config.registerExtensionFunction(new RemoveDocument(xRepo.getDocumentManagement()));
            config.registerExtensionFunction(new StoreDocument(xRepo.getDocumentManagement()));
            config.registerExtensionFunction(new RemoveDocuments(xRepo.getDocumentManagement()));
            config.registerExtensionFunction(new QueryDocumentUris(xRepo.getQueryManagement()));
            config.registerExtensionFunction(new BeginTransaction(xRepo.getTxManagement()));
            config.registerExtensionFunction(new CommitTransaction(xRepo.getTxManagement()));
            config.registerExtensionFunction(new RollbackTransaction(xRepo.getTxManagement()));
        }
	}
    
}
