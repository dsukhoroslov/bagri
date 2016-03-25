package com.bagri.xquery.saxon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.GeneralComparison10;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
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
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;
import com.bagri.common.query.PathBuilder.PathSegment;
import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.bagri.xdm.client.common.impl.ModelManagementBase;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMSchema;

public class CollectionURIResolverImpl implements CollectionURIResolver {

    /**
	 * need it because CollectionURIResolver extends Serializable
	 */
	private static final long serialVersionUID = -3339879838382944740L;

	private static final Logger logger = LoggerFactory.getLogger(CollectionURIResolverImpl.class);

	private PathBuilder currentPath;
	//private int collectType;
	private int currentType;
    private XPathContext ctx;
    private XDMRepository repo;
    private QueryBuilder query;
    private XQueryExpression exp;

    public CollectionURIResolverImpl(XDMRepository repo) {
    	this.repo = repo;
    }
    
    QueryBuilder getQuery() {
    	// should return Container's copy!?
    	return query;
    }
    
    void setQuery(QueryBuilder query) {
    	// copy it! already copied, actually
		logger.trace("setQuery. got: {}; this: {}", query, this);
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

		int collectType;
		if (href == null) {
			// means default collection: all schema documents
			collectType = XDMDocument.clnDefault;
			currentType = XDMDocument.clnDefault;
		} else {
			collectType = getCollectionId(href);
			currentType = 0;
		}

		if (query == null) {
			query = new QueryBuilder();
			currentPath = new PathBuilder();
        	//query.addContainer(currentType, new ExpressionContainer());
			iterate(exp.getExpression()); 
		} else if (query.hasEmptyParams()) {
			iterateParams(exp.getExpression());
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.debug("resolve; time taken: {}; query: {}; this: {}", stamp, query, this); 

		ExpressionContainer exCont = getCurrentContainer();
		if (exCont.getExpression().getRoot() == null) {
			exCont.addExpression(currentType);
   			logger.trace("resolve; added always expression for type: {}", currentType);
		}
		
		// provide builder's copy here.
		exCont = query.getContainer(collectType);
		CollectionIterator iter = new CollectionIterator((XDMQueryManagement) repo.getQueryManagement(), exCont);
		logger.trace("resolve. returning iter: {} for collection type: {}", iter, collectType);
		return iter;
	}
	
	private int getCollectionId(String uri) {
		XDMSchema schema = ((com.bagri.xdm.cache.api.XDMRepository) repo).getSchema();
		XDMCollection cln = schema.getCollection(uri);
		if (cln != null) {
			return cln.getId();
		}
		logger.info("getCollectionId; no collection found for uri: {}; collections: {}", uri, schema.getCollections());
		return ModelManagementBase.WRONG_PATH;
		//XDMModelManagement dict = repo.getModelManagement();
		//String root = dict.normalizePath(uri);
		//return dict.getDocumentType(root);
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
				Object o = SaxonUtils.itemToObject(item);
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
   				if (ex2 == null) {
   	       			logger.debug("iterateParams; got null expression for var: {}", var);
   				} else if (ex2.getObjectName() == null) {
   	       			logger.debug("iterateParams; got null object name for var: {}; ex: {}", var, ex2);
   				} else {
   					String vName = ex2.getObjectName().getClarkName();
   					String pName = ((GeneralVariable) var.getBinding()).getVariableQName().getLocalPart();
   					logger.trace("iterateParams; got var: {}, with name: {}", pName, vName);
   					//vars.put(vName, pName);
   				}
    		}
    	}
    	
    }
	
    private void iterate(Expression ex) throws XPathException {
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
    				currentType = getCollectionId(uri);
    	        	logger.trace("iterate; set collectionId: {} for uri: {}", currentType, uri);
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
    			ExpressionContainer exCont = getCurrentContainer();
    			exIndex = exCont.addExpression(currentType, compType, path);
    			logger.trace("iterate; added expression at index: {}", exIndex);
    			//}
    		} else {
    	    	throw new XPathException("Unknown comparison type for expression: " + ex, ex);
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
    		int varIdx = 0;
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
    			varIdx++;
    		}
    		Comparison compType = getComparison(be.getOperator());
    		if (compType == null) {
            	logger.debug("iterate; can't get comparison from {}", be);
    	    	throw new XPathException("Unknown comparison type for expression: " + be, be);
    		} else if (value == null) {
            	logger.debug("iterate; can't get value from {}; operands: {}", be, be.getOperands());
            	// TODO: the join use case. have to think about this..
    	    	//throw new IllegalStateException("Unexpected expression: " + ex);
    		} //else {

    		// it seems we still need this workaround ..
    		if (varIdx == 0 && compType != Comparison.EQ) {
    			compType = Comparison.negate(compType);
    		}
    			
   			ExpressionContainer exCont = getCurrentContainer();
   			exIndex = exCont.addExpression(currentType, compType, path, pName, value);
   			logger.trace("iterate; added path expression at index: {}", exIndex);
   			setParentPath(exCont.getExpression(), exIndex, path);
   			logger.trace("iterate; parent path {} set at index: {}", path, exIndex);
    	}  

    	if (ex instanceof BooleanExpression) {
			ExpressionContainer exCont = getCurrentContainer();
    		setParentPath(exCont.getExpression(), exIndex, path);
   			logger.trace("iterate; parent path {} set at index: {}", path, exIndex);
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
    	
    	//if (ex instanceof Collection) {
   		//	ExpressionContainer exCont = getCurrentContainer();
   		//	if (exCont.getExpression().getRoot() == null) {
   		//		exCont.addExpression(currentType);
   	   	//		logger.trace("iterate; added always expression for type: {}", currentType);
   		//	}
    	//}    	
    	
    	logger.trace("end: {}; path: {}", ex.getClass().getName(), path.getFullPath());
    }
    
    private ExpressionContainer getCurrentContainer() {
		ExpressionContainer exCont = query.getContainer(currentType);
		if (exCont == null) {
			exCont = new ExpressionContainer();
			// not sure is it ok for currentType = -1!
			query.addContainer(currentType, exCont);
    		logger.trace("getCurrentContainer; added new container {} for cType: {}", exCont, currentType);
		}  
    	return exCont;
    }
    
}
