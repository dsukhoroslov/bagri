package com.bagri.xquery.saxon;

import java.math.BigInteger;
import java.util.Iterator;

import javax.xml.xquery.XQItem;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison10;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.lib.CollectionURIResolver;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.XQueryExpression;
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
import net.sf.saxon.xqj.SaxonDuration;
import net.sf.saxon.xqj.SaxonXMLGregorianCalendar;
import static net.sf.saxon.om.StandardNames.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.PathBuilder;
import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMSchemaDictionary;

public class BagriCollectionResolver implements CollectionURIResolver {

    /**
	 * need it because CollectionURIResolver extends Serializable
	 */
	private static final long serialVersionUID = -3339879838382944740L;

	private static final Logger logger = LoggerFactory.getLogger(BagriCollectionResolver.class);

    private XPathContext ctx;
    private XQueryExpression exp;
    private XDMDocumentManagement mgr;

    public BagriCollectionResolver(XDMDocumentManagement mgr) {
    	this.mgr = mgr;
    }

	@Override
	public SequenceIterator<Item> resolve(String href, String base, XPathContext context) throws XPathException {
		
		logger.debug("resolve. href: {}; base: {}; context: {}", new Object[] {href, base, context});
		//return EmptyIterator.getInstance();

		this.ctx = context;
		XDMSchemaDictionary dict = mgr.getSchemaDictionary();
		String root = dict.normalizePath(href);
		int docType = dict.getDocumentType(root);
		
		ExpressionBuilder eb = new ExpressionBuilder();
		String path = iterate(docType, exp.getExpression(), new PathBuilder(), eb);
		logger.debug("resolve; expressions: {}; path: {}", eb, path); 
		// if (eb.size == 0) eb.addExpression(path, ..)

		BagriXDMIterator iter = new BagriXDMIterator(eb);
		iter.setDataManager(mgr);
		logger.trace("resolve. xdm: {}; returning iter: {}", mgr, iter);
		//context.
		return iter;
	}
	
	private Object getValue(GroundedValue value) throws XPathException {
		if (value != null) {
			Item item = value.head();
			if (item != null) {
				return itemToObject(item);
			}
		}
		return null;
	}
	
	private Object getVariable(int slot) throws XPathException {
		Sequence sq = ctx.evaluateLocalVariable(slot);
		if (sq != null) {
			Item item = sq.head();
			if (item != null) {
				return itemToObject(item);
			}
		}
		return null;
	}
	
	private AxisType getAxisType(byte axis) {
    	switch (axis) {
			case AxisInfo.ANCESTOR: return AxisType.ANCESTOR;
			case AxisInfo.ANCESTOR_OR_SELF: return AxisType.ANCESTOR_OR_SELF;
			case AxisInfo.ATTRIBUTE: return AxisType.ATTRIBUTE;
			case AxisInfo.CHILD: return AxisType.CHILD; 
				//path.append("/");
			case AxisInfo.DESCENDANT: return AxisType.DESCENDANT; 
				//path.append("//");
			case AxisInfo.DESCENDANT_OR_SELF: return AxisType.DESCENDANT_OR_SELF;
			case AxisInfo.FOLLOWING: return AxisType.FOLLOWING;
			case AxisInfo.FOLLOWING_SIBLING: return AxisType.FOLLOWING_SIBLING;
			case AxisInfo.NAMESPACE: return AxisType.NAMESPACE;
			case AxisInfo.PARENT: return AxisType.PARENT;
				//path.append("/../");
			case AxisInfo.PRECEDING: return AxisType.PRECEDING;
			case AxisInfo.PRECEDING_OR_ANCESTOR: return null; //??
			case AxisInfo.PRECEDING_SIBLING: return AxisType.PRECEDING_SIBLING;
			case AxisInfo.SELF: return AxisType.SELF;
		}
		return null;
	}
	
	private Comparison getComparison(int operator) {
		switch (operator) {
			case Token.AND: return Comparison.AND;
			case Token.OR: return Comparison.OR;
			case Token.FEQ:
			case Token.EQUALS: return Comparison.EQ;
			case Token.FLE:
			case Token.LE: return Comparison.LE;
			case Token.FLT:
			case Token.LT: return Comparison.LT;
			case Token.FGE:
			case Token.GE: return Comparison.GE;
			case Token.FGT:
			case Token.GT: return Comparison.GT;
			default: return null;
		}
	}
	
	private void setParentPath(ExpressionBuilder eb, int exIndex, PathBuilder path) {
		com.bagri.common.query.Expression ex = eb.getExpression(exIndex);
		if (ex != null) {
    		path.setPath(ex.getPath()); 
        	logger.trace("iterate; path switched to: {}; from index: {}", path, exIndex);
		}
	}

    private String iterate(int docType, Expression ex, PathBuilder path, ExpressionBuilder eb) throws XPathException {
    	logger.trace("start: {}; path: {}", ex.getClass().getName(), ex); //ex.getObjectName());

    	if (ex instanceof Block) {
        	logger.trace("end: {}; path: {}", ex.getClass().getName(), path);
    		return path.toString();
    	}
    	
    	if (ex instanceof AxisExpression) {
    		AxisExpression ae = (AxisExpression) ex;
        	logger.trace("iterate: axis: {}", AxisInfo.axisName[ae.getAxis()]);

        	AxisType axis = getAxisType(ae.getAxis());
        	String namespace = null;
        	String segment = null;
    		NodeTest test = ae.getNodeTest();
    		if (test != null) {
		    	int code = test.getFingerprint();
		    	if (code >= 0) {
		    		StructuredQName name = ctx.getNamePool().getStructuredQName(code);
		    		namespace = mgr.getSchemaDictionary().getNamespacePrefix(name.getURI());
		    		segment = name.getLocalPart();
		    	} else {
		    		// case with regex..
		        	logger.trace("iterate: empty code; test: {}", test);
		        	// depends on axis...
		        	segment = "*";
		    	}
    		}
        	path.addPathSegment(axis, namespace, segment);
    	}

    	int exIndex = -1;
    	if (ex instanceof BooleanExpression) {
    		Comparison compType = getComparison(((BooleanExpression) ex).getOperator());
    		if (compType != null) {
   	    		exIndex = eb.addExpression(docType, compType, path, null);
	        	logger.trace("iterate; added expression at index: {}", exIndex);
    		} else {
    	    	throw new IllegalStateException("Unexpected expression: " + ex);
    		}
    	}
    	
    	Iterator<Expression> ie = ex.iterateSubExpressions();
    	while (ie.hasNext()) {
    		Expression e = ie.next();
    		iterate(docType, e, path, eb);
    	}
    	
    	if (ex instanceof GeneralComparison10 || ex instanceof GeneralComparison20 || ex instanceof ValueComparison) {
    		BinaryExpression be = (BinaryExpression) ex;
    		int varIdx = 0;
    		Object value = null;
    		for (Expression e: be.getOperands()) {
    			if (e instanceof VariableReference) {
    	    		value = getVariable(((VariableReference) e).getBinding().getLocalSlotNumber());
    	    		break;
    			} else if (e instanceof StringLiteral) {
    				value = ((StringLiteral) e).getStringValue();
    				break;
    			} else if (e instanceof Literal) {
    				value = getValue(((Literal) e).getValue()); 
    				break;
    			}
    			varIdx++;
    		}
    		Comparison compType = getComparison(be.getOperator());
    		if (compType == null) {
            	logger.debug("iterate; can't get comparison from {}", be);
    	    	throw new IllegalStateException("Unexpected expression: " + ex);
    		} else if (value == null) {
            	logger.debug("iterate; can't get value from {}; operands: {}", be, be.getOperands());
    	    	throw new IllegalStateException("Unexpected expression: " + ex);
    		} else {
    			if (varIdx == 0) {
    				compType = Comparison.negate(compType);
    			}
        		exIndex = eb.addExpression(docType, compType, path, value);
        		setParentPath(eb, exIndex, path);
    		}
    	}  

    	if (ex instanceof BooleanExpression) {
    		setParentPath(eb, exIndex, path);
    	}
    	
    	if (ex instanceof Atomizer) {
    		if (!(((Atomizer) ex).getBaseExpression() instanceof BindingReference)) {
    			//path.append("/text()");
    			path.addPathSegment(AxisType.CHILD, null, "text()");
    		}
    	}
    	logger.trace("end: {}; path: {}", ex.getClass().getName(), path.getFullPath());
    	return path.toString();
    }
    
	void setExpression(XQueryExpression exp) {
		this.exp = exp;
	}

    private static Object itemToObject(Item item) throws XPathException {
        if (item instanceof AtomicValue) {
            AtomicValue p = ((AtomicValue)item);
            int t = p.getItemType().getPrimitiveType();
            switch (t) {
                case XS_ANY_URI:
                    return p.getStringValue();
                case XS_BASE64_BINARY:
                    return ((Base64BinaryValue)p).getBinaryValue();
                case XS_BOOLEAN:
                    return Boolean.valueOf(((BooleanValue)p).getBooleanValue());
                case XS_DATE:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DATE_TIME:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DECIMAL:
                    return ((DecimalValue)p).getDecimalValue();
                case XS_DOUBLE:
                    return new Double(((DoubleValue)p).getDoubleValue());
                case XS_DURATION:
                    return new SaxonDuration((DurationValue)p);
                case XS_FLOAT:
                    return new Float(((FloatValue)p).getFloatValue());
                case XS_G_DAY:
                case XS_G_MONTH:
                case XS_G_MONTH_DAY:
                case XS_G_YEAR:
                case XS_G_YEAR_MONTH:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
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
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DAY_TIME_DURATION:
                    return new SaxonDuration((DurationValue)p);
                case XS_YEAR_MONTH_DURATION:
                    return new SaxonDuration((DurationValue)p);
                default:
                    throw new XPathException("unsupported type");
            }
        } else if (item instanceof NodeInfo) {
            return NodeOverNodeInfo.wrap((NodeInfo)item);
            //try {
				//return QueryResult.serialize((NodeInfo)item);
			//} catch (XPathException ex) {
			//	throw new XQException(ex.getMessage());
			//}
        } else if (item instanceof ObjectValue) {
        	Object value = ((ObjectValue) item).getObject();
        	if (value instanceof XQItem) {
        		//
        		//return ((XQItem) value).getObject();
        		return value;
        	}
        }
        return item;
    }
    
}
