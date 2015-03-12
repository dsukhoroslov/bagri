package com.bagri.xquery.saxon;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQItem;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison10;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.GeneralVariable;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.functions.Collection;
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
import net.sf.saxon.value.SaxonDuration;
import net.sf.saxon.value.SaxonXMLGregorianCalendar;
//import net.sf.saxon.xqj.SaxonDuration;
//import net.sf.saxon.xqj.SaxonXMLGregorianCalendar;
import static net.sf.saxon.om.StandardNames.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;
import com.bagri.common.query.PathBuilder.PathSegment;
import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;

public class CollectionURIResolverImpl implements CollectionURIResolver {

    /**
	 * need it because CollectionURIResolver extends Serializable
	 */
	private static final long serialVersionUID = -3339879838382944740L;

	private static final Logger logger = LoggerFactory.getLogger(CollectionURIResolverImpl.class);

	private PathBuilder currentPath;
	private int collectType;
	private int currentType;
    private XPathContext ctx;
    private XDMRepository repo;
    private QueryBuilder query;
    private XQueryExpression exp;
    //private ExpressionContainer exCont;

    public CollectionURIResolverImpl(XDMRepository repo) {
    	this.repo = repo;
    }
    
    QueryBuilder getQuery() {
    	// should return Container's copy!
    	return query;
    }
    
    void setQuery(QueryBuilder query) {
    	// copy it!
		logger.trace("setQuery. got: {}", query);
    	this.query = query;
    }

	void setExpression(XQueryExpression exp) {
		this.exp = exp;
	}

	@Override
	public SequenceIterator<Item> resolve(String href, String base, XPathContext context) throws XPathException {
		
		logger.trace("resolve. href: {}; base: {}; context: {}", href, base, context);
		this.ctx = context;
		long stamp = System.currentTimeMillis();

		if (href == null) {
			// means default collection: all schema documents
			currentType = -1;
		} else {
			collectType = getCollectionType(href);
			currentType = 0;
		}

		if (query == null) {
			query = new QueryBuilder();
			currentPath = new PathBuilder();
			iterate(exp.getExpression()); 
		} else if (query.hasEmptyParams()) {
			iterateParams(exp.getExpression());
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.debug("resolve; time taken: {}; query: {}", stamp, query); 

		// provide builder's copy here.
		ExpressionContainer exCont = query.getContainer(collectType);
		CollectionIterator iter = new CollectionIterator(repo.getQueryManagement(), exCont);

		logger.trace("resolve. xdm: {}; returning iter: {} for collection type: {}", 
				repo, iter, collectType);
		return iter;
	}
	
	private int getCollectionType(String uri) {
		XDMModelManagement dict = repo.getModelManagement();
		String root = dict.normalizePath(uri);
		return dict.getDocumentType(root);
	}
	
	private Object getValues(Sequence sq) throws XPathException {
		if (sq != null) {
			List result = new ArrayList();
			SequenceIterator itr = sq.iterate();
			while (true) {
				Item item = itr.next();
				if (item == null) {
					break;
				}
				Object o = itemToObject(item);
				//logger.trace("getVariable; got item: {}", o);
				result.add(o);
			} 
			return result;
		}
		return null;
	}
	
	private AxisType getAxisType(byte axis) {
    	switch (axis) {
			case AxisInfo.ANCESTOR: return AxisType.ANCESTOR;
			case AxisInfo.ANCESTOR_OR_SELF: return AxisType.ANCESTOR_OR_SELF;
			case AxisInfo.ATTRIBUTE: return AxisType.ATTRIBUTE;
			case AxisInfo.CHILD: return AxisType.CHILD; 
			case AxisInfo.DESCENDANT: return AxisType.DESCENDANT; 
			case AxisInfo.DESCENDANT_OR_SELF: return AxisType.DESCENDANT_OR_SELF;
			case AxisInfo.FOLLOWING: return AxisType.FOLLOWING;
			case AxisInfo.FOLLOWING_SIBLING: return AxisType.FOLLOWING_SIBLING;
			case AxisInfo.NAMESPACE: return AxisType.NAMESPACE;
			case AxisInfo.PARENT: return AxisType.PARENT;
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
	
	//private Object normalizeValue(Object value) {
	//	if (value instanceof String) {
	//		value = ((String) value).replaceAll("&amp;", "&");
	//	}
	//	return value;
	//}
	
    private void iterateParams(Expression ex) throws XPathException {

    	if (ex instanceof Block) {
    		return;
    	}
    	
    	Iterator<Expression> ie = ex.iterateSubExpressions();
    	while (ie.hasNext()) {
    		Expression e = ie.next();
    		iterateParams(e); 
    	}
    	
    	if (ex instanceof GeneralComparison10 || ex instanceof GeneralComparison20 || ex instanceof ValueComparison) {
    		BinaryExpression be = (BinaryExpression) ex;
    		//int varIdx = 0;
    		Object value = null;
    		String pName = null;
    		for (Expression e: be.getOperands()) {
    			if (e instanceof VariableReference) {
    				Binding bind = ((VariableReference) e).getBinding();
    				if (bind instanceof LetExpression) {
    					Expression e2 = ((LetExpression) bind).getSequence();
    					if (e2 instanceof Atomizer) {
    			    		e2 = ((Atomizer) e2).getBaseExpression();
    			    		if (e2 instanceof VariableReference) {
    			    			// paired ref to the e
    			    			pName = ((VariableReference) e2).getBinding().getVariableQName().getLocalPart(); 
    			    		}
    			    	}
    				}
    				
    				if (pName == null) {
    					pName = bind.getVariableQName().getClarkName();
    				}
    	    		value = getValues(ctx.evaluateLocalVariable(bind.getLocalSlotNumber()));
        			logger.trace("iterateParams; got reference: {}, value: {}", pName, value);
        			if (pName != null && value != null) {
        				query.setEmptyParam(pName, value);
        			}
    	    		break;
    			//} else if (e instanceof StringLiteral) {
    			//	value = ((StringLiteral) e).getStringValue();
    			//	break;
    			//} else if (e instanceof Literal) {
    			//	value = getValue(((Literal) e).getValue()); 
    			//	break;
    			}
    		}
			//logger.trace("iterateParams; added path expression at index: {}", exIndex);
    	}  
    	
    	if (ex instanceof VariableReference) {
    		VariableReference var = (VariableReference) ex;
    		if (var.getBinding() instanceof GeneralVariable) {
   				Expression ex2 = ((XQueryExpression) var.getContainer()).getExpression(); 
   				String vName = ex2.getObjectName().getClarkName();
   				String pName = ((GeneralVariable) var.getBinding()).getVariableQName().getLocalPart();
       			logger.trace("iterateParams; got var: {}, with name: {}", pName, vName);
       			//vars.put(vName, pName);
    		}
    	}
    	
    }
	
    private void iterate(Expression ex /*, PathBuilder path*/) throws XPathException {
    	logger.trace("start: {}; path: {}", ex.getClass().getName(), ex); 

    	PathBuilder path = currentPath;
    	if (ex instanceof Block) {
        	logger.trace("end: {}; path: {}", ex.getClass().getName(), path.getFullPath());
    		return;
    	}
    	
    	if (ex instanceof Collection) {
    		Collection clx = (Collection) ex;
    		for (Expression e: clx.getArguments()) {
    			if (e instanceof StringLiteral) {
    				String uri = ((StringLiteral) e).getStringValue();
    				currentType = getCollectionType(uri);
    	        	logger.trace("iterate; set docType: {} for uri: {}", currentType, uri);
    	        	currentPath = new PathBuilder();
    	        	path = currentPath;
    	        	ExpressionContainer exCont = new ExpressionContainer();
    	        	query.addContainer(currentType, exCont);
    				break;
    			}
    		}
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
		    		namespace = repo.getModelManagement().getNamespacePrefix(name.getURI());
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
    			//if (currentType == collectType) {
    			ExpressionContainer exCont = query.getContainer(currentType);
    				exIndex = exCont.addExpression(currentType, compType, path);
    				logger.trace("iterate; added expression at index: {}", exIndex);
    			//}
    		} else {
    	    	throw new IllegalStateException("Unexpected expression: " + ex);
    		}
    	}

    	//if (ex instanceof LetExpression) {
    	//	LetExpression let = (LetExpression) ex;
    	//	StructuredQName lName = let.getObjectName();
    	//	logger.trace("iterate; let: {}", lName);
    	//}
    	
    	Iterator<Expression> ie = ex.iterateSubExpressions();
    	while (ie.hasNext()) {
    		Expression e = ie.next();
    		iterate(e); //, path); //, vars);
    	}
    	
    	if (ex instanceof GeneralComparison10 || ex instanceof GeneralComparison20 || ex instanceof ValueComparison) {
    		BinaryExpression be = (BinaryExpression) ex;
    		//int varIdx = 0;
    		Object value = null;
    		String pName = null;
    		for (Expression e: be.getOperands()) {
    			if (e instanceof VariableReference) {
    				Binding bind = ((VariableReference) e).getBinding();
    				if (bind instanceof LetExpression) {
    					Expression e2 = ((LetExpression) bind).getSequence();
    					if (e2 instanceof Atomizer) {
    			    		e2 = ((Atomizer) e2).getBaseExpression();
    			    		if (e2 instanceof VariableReference) {
    			    			// paired ref to the e
    			    			pName = ((VariableReference) e2).getBinding().getVariableQName().getLocalPart(); 
    			    		}
    			    	}
    				}
    				
    				if (pName == null) {
    					pName = bind.getVariableQName().getClarkName();
    				}
    	    		value = getValues(ctx.evaluateLocalVariable(bind.getLocalSlotNumber()));
        			logger.trace("iterate; got reference: {}, value: {}", pName, value);
    	    		break;
    			} else if (e instanceof StringLiteral) {
    				value = ((StringLiteral) e).getStringValue();
    				break;
    			} else if (e instanceof Literal) {
    				value = getValues(((Literal) e).getValue()); 
    				break;
    			}
    			//varIdx++;
    		}
    		Comparison compType = getComparison(be.getOperator());
    		if (compType == null) {
            	logger.debug("iterate; can't get comparison from {}", be);
    	    	throw new IllegalStateException("Unexpected expression: " + ex);
    		} else if (value == null) {
            	logger.debug("iterate; can't get value from {}; operands: {}", be, be.getOperands());
            	// thrown in case of join. have to think about this..
    	    	//throw new IllegalStateException("Unexpected expression: " + ex);
    		} //else {
    			// simse we do not need this workaround any more..
    			//if (varIdx == 0) {
    			//	compType = Comparison.negate(compType);
    			//}
    			//if (currentType == collectType) {
    				//value = normalizeValue(value);
    			ExpressionContainer exCont = query.getContainer(currentType);
    				exIndex = exCont.addExpression(currentType, compType, path, pName, value);
    				logger.trace("iterate; added path expression at index: {}", exIndex);
    				setParentPath(exCont.getExpression(), exIndex, path);
    			//}
    		//}
    	}  

    	if (ex instanceof BooleanExpression) {
			ExpressionContainer exCont = query.getContainer(currentType);
    		setParentPath(exCont.getExpression(), exIndex, path);
    	}
    	
    	if (ex instanceof Atomizer) {
    		Atomizer at = (Atomizer) ex;
    		if (at.getBaseExpression() instanceof BindingReference) {
       			//logger.trace("iterate; got base ref: {}", at.getBaseExpression());
    		} else {
    			PathSegment ps = path.getLastSegment();
    			if (ps != null && ps.getAxis() == AxisType.CHILD) {
    				path.addPathSegment(AxisType.CHILD, null, "text()");
    			}
    		}
    	}
    	
    	//if (ex instanceof VariableReference) {
    	//	VariableReference var = (VariableReference) ex;
    	//	if (var.getBinding() instanceof GeneralVariable) {
   		//		Expression ex2 = ((XQueryExpression) var.getContainer()).getExpression(); 
   		//		String vName = ex2.getObjectName().getClarkName();
   		//		String pName = ((GeneralVariable) var.getBinding()).getVariableQName().getLocalPart();
       	//		logger.trace("iterate; got var: {}, with name: {}", pName, vName);
       	//		vars.put(vName, pName);
    	//	}
    	//}
    	
    	logger.trace("end: {}; path: {}", ex.getClass().getName(), path.getFullPath());
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
