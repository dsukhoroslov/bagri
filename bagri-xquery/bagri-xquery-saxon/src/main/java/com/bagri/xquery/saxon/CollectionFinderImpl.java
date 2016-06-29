package com.bagri.xquery.saxon;

import static com.bagri.xdm.common.XDMConstants.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.impl.ModelManagementBase;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.query.AxisType;
import com.bagri.xdm.query.Comparison;
import com.bagri.xdm.query.ExpressionBuilder;
import com.bagri.xdm.query.ExpressionContainer;
import com.bagri.xdm.query.PathBuilder;
import com.bagri.xdm.query.PathSegment;
import com.bagri.xdm.query.QueryBuilder;
import com.bagri.xdm.system.Collection;
import com.bagri.xdm.system.Schema;

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GeneralComparison10;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

public class CollectionFinderImpl implements CollectionFinder {

	private static final Logger logger = LoggerFactory.getLogger(CollectionFinderImpl.class);

    private SchemaRepository repo;
    private QueryBuilder query;
    private XQueryExpression exp;

	private PathBuilder currentPath;
	//private int collectType;
	private int currentType;
    
    public CollectionFinderImpl(SchemaRepository repo) {
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
	public ResourceCollection findCollection(XPathContext context, String collectionURI) throws XPathException {

		logger.trace("findCollection. context: {}; uri: {}", context, collectionURI);
		long stamp = System.currentTimeMillis();

		int collectId;
		if (collectionURI == null || collectionURI.isEmpty()) {
			// means default collection: all schema documents
			collectId = Document.clnDefault;
			currentType = Document.clnDefault;
		} else {
			collectId = getCollectionId(collectionURI, exp.getExpression().getStaticBaseURIString());
			currentType = collectId; //0;
			logger.trace("findCollection. got collection type: {} for uri: {}", collectId, collectionURI);
		}

		if (query == null) {
			query = new QueryBuilder();
			currentPath = new PathBuilder();
        	//query.addContainer(currentType, new ExpressionContainer());
			iterate(exp.getExpression(), context); 
		} else if (query.hasEmptyParams()) {
			iterateParams(exp.getExpression(), context);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.debug("findCollection; time taken: {}; query: {}; this: {}", stamp, query, this); 

		ExpressionContainer exCont = getCurrentContainer();
		if (exCont.getBuilder().getRoot() == null) {
			exCont.addExpression(currentType);
   			logger.trace("findCollection; added always expression for type: {}", currentType);
		}
		
		// provide builder's copy here.
		exCont = query.getContainer(collectId);
		ResourceCollection result = new ResourceCollectionImpl(collectionURI, repo, exCont); 
		logger.trace("findCollection. returning result: {} for collection ID: {}", result, collectId);
		return result;
	}

	private int getCollectionId(String uri, String baseUri) {
		Schema schema = ((com.bagri.xdm.cache.api.SchemaRepository) repo).getSchema();
		if (baseUri != null && !baseUri.isEmpty() && uri.startsWith(baseUri)) {
			uri = uri.substring(baseUri.length());
		}
		Collection cln = schema.getCollection(uri);
		if (cln != null) {
			return cln.getId();
		}
		logger.info("getCollectionId; no collection found for uri: {}; baseUri: {}, collections: {}", uri, baseUri, schema.getCollections());
		return ModelManagementBase.WRONG_PATH;
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
		com.bagri.xdm.query.Expression ex = eb.getExpression(exIndex);
		if (ex != null) {
    		path.setPath(ex.getPath()); 
        	logger.trace("iterate; path switched to: {}; from index: {}", path, exIndex);
		}
	}
	
    private void iterateParams(Expression ex, XPathContext ctx) throws XPathException {

    	if (ex instanceof Block) {
    		return;
    	}
    	
    	Iterator<Operand> itr = ex.operands().iterator();
    	while(itr.hasNext()) {
    		Expression e = itr.next().getChildExpression(); 
    		iterateParams(e, ctx);
    	}

    	if (ex instanceof GeneralComparison10 || ex instanceof GeneralComparison20 || ex instanceof ValueComparison) {
    		BinaryExpression be = (BinaryExpression) ex;
    		Object value = null;
    		String pName = null;
    		Expression le = be.getLhsExpression();

    		Comparison compType = getComparison(be.getOperator());
    		if (compType == null) {
            	logger.debug("iterate; can't get comparison from {}", be);
    	    	throw new XPathException("Unknown comparison type for expression: " + be);
    		} 

    		Expression var;
    		if (le instanceof VariableReference || le instanceof Literal) {
    			compType = Comparison.negate(compType);
    			var = le;
    		} else {
    			var = be.getRhsExpression();
    		}
    		
   			if (var instanceof VariableReference) {
   				Binding bind = ((VariableReference) var).getBinding();
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
   	    		//value = getValues(ctx.evaluateLocalVariable(bind.getLocalSlotNumber()));
   				try {
   					value = getValues(bind.evaluateVariable(ctx));
   				} catch (NullPointerException ee) {
   					value = null;
   				}
   			//} else if (var instanceof StringLiteral) {
   			//	value = ((StringLiteral) var).getStringValue();
   			//} else if (var instanceof Literal) {
   			//	value = getValues(((Literal) var).getValue()); 
   			}
    			
			logger.trace("iterateParams; got reference: {}, value: {}", pName, value);
			if (pName != null && value != null) {
				query.setEmptyParam(pName, value);
			}
    	}  
    }
	
    private void iterate(Expression ex, XPathContext ctx) throws XPathException {
    	logger.trace("start: {}; path: {}", ex.getClass().getName(), ex); 

    	PathBuilder path = currentPath;
    	if (ex instanceof Block) {
        	logger.trace("end: {}; path: {}", ex.getClass().getName(), path.getFullPath());
    		return;
    	}
    	
       	if (ex instanceof FunctionCall) {
       		FunctionCall clx = (FunctionCall) ex;
       		if ("collection".equals(clx.getDisplayName())) {
       			String collectUri = "";
       			if (clx.getArity() > 0) {
       				Expression arg = clx.getArg(0);
       				if (arg instanceof StringLiteral) {
       					collectUri = ((StringLiteral) arg).getStringValue();
       				} else {
       					// evaluate ?
       					collectUri = arg.evaluateAsString(ctx).toString();
       				}
       			}

       			int clnId = getCollectionId(collectUri, ex.getStaticBaseURIString());
       			if (clnId < 0) {
       				clnId = -1 * (query.getContainers().size() + 1); 
       			}
       			currentType = clnId;
	    	    logger.trace("iterate; set collectionId: {} for uri: {}", currentType, collectUri);
	    	    currentPath = new PathBuilder();
	    	    path = currentPath;
	    	    ExpressionContainer exCont = new ExpressionContainer();
	    	    query.addContainer(currentType, exCont);
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
    	    	throw new XPathException("Unknown comparison type for expression: " + ex);
    		}
    	}

    	Iterator<Operand> itr = ex.operands().iterator();
    	while(itr.hasNext()) {
    		Expression e = itr.next().getChildExpression(); 
    		iterate(e, ctx);
    	}
    	
    	if (ex instanceof GeneralComparison10 || ex instanceof GeneralComparison20 || ex instanceof ValueComparison) {
    		BinaryExpression be = (BinaryExpression) ex;
    		Object value = null;
    		String pName = null;
    		Expression le = be.getLhsExpression();

    		Comparison compType = getComparison(be.getOperator());
    		if (compType == null) {
            	logger.debug("iterate; can't get comparison from {}", be);
    	    	throw new XPathException("Unknown comparison type for expression: " + be);
    		} 

    		Expression var;
    		if (le instanceof VariableReference || le instanceof Literal) {
    			compType = Comparison.revert(compType);
    			var = le;
    		} else {
    			var = be.getRhsExpression();
    		}
    		
   			if (var instanceof VariableReference) {
   				Binding bind = ((VariableReference) var).getBinding();
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
   	    		//value = getValues(ctx.evaluateLocalVariable(bind.getLocalSlotNumber()));
   				try {
   					value = getValues(bind.evaluateVariable(ctx));
   				} catch (NullPointerException ee) {
   					value = null;
   				}
       			logger.trace("iterate; got reference: {}, value: {}", pName, value);
   			} else if (var instanceof StringLiteral) {
   				value = ((StringLiteral) var).getStringValue();
   			} else if (var instanceof Literal) {
   				value = getValues(((Literal) var).getValue()); 
   			}
    			
   			ExpressionContainer exCont = getCurrentContainer();
   			exIndex = exCont.addExpression(currentType, compType, path, pName, value);
   			logger.trace("iterate; added path expression at index: {}", exIndex);
   			setParentPath(exCont.getBuilder(), exIndex, path);
   			logger.trace("iterate; parent path {} set at index: {}", path, exIndex);
    	}  

    	if (ex instanceof BooleanExpression) {
			ExpressionContainer exCont = getCurrentContainer();
    		setParentPath(exCont.getBuilder(), exIndex, path);
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
