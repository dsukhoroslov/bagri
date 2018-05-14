package com.bagri.xquery.saxon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.model.Document;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.Comparison;
import com.bagri.core.query.ExpressionBuilder;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.query.PathSegment;
import com.bagri.core.query.QueryBuilder;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.server.api.impl.ModelManagementBase;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Schema;

import net.sf.saxon.expr.Assignation;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AttributeGetter;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.ForExpression;
import net.sf.saxon.expr.FunctionCall;
//import net.sf.saxon.expr.GeneralComparison10;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.ForClause;
import net.sf.saxon.expr.flwor.LetClause;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.FingerprintedQName;
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
	private XQueryExpression exp;
	//private QueryBuilder query;

	//private PathBuilder currentPath;
	// private int collectType;
	//private int currentType;
	
	private ThreadLocal<QueryBuilder> thQuery = new ThreadLocal<QueryBuilder>() {
		
		@Override
		protected QueryBuilder initialValue() {
			return new QueryBuilder();
 		}
	};
	


	public CollectionFinderImpl(SchemaRepository repo) {
		this.repo = repo;
	}

	QueryBuilder getQuery() {
		// should return Container's copy!?
		return thQuery.get(); //query;
	}

	void setQuery(QueryBuilder query) {
		// copy it! already copied, actually
		logger.trace("setQuery. got: {}; this: {}", query, this);
		this.thQuery.set(query); // query = query;
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
		} else {
			collectId = getCollectionId(collectionURI, exp.getExpression().getStaticBaseURIString());
		}
		int currentType = collectId; 
		logger.trace("findCollection. got collection type: {} for uri: {}", collectId, collectionURI);

		QueryBuilder query = getQuery();
		if (query == null) {
			query = new QueryBuilder();
			PathBuilder currentPath = new PathBuilder();
			// query.addContainer(currentType, new ExpressionContainer());
			iterate(exp.getExpression(), context, query, currentType, currentPath);
		} else if (query.hasEmptyParams()) {
			iterateParams(exp.getExpression(), context, query);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.debug("findCollection; time taken: {}; query: {}; this: {}", stamp, query, this);

		ExpressionContainer exCont = getCurrentContainer(query, currentType);
		if (exCont.getBuilder().getRoot() == null) {
			exCont.addExpression(currentType);
			logger.trace("findCollection; added always expression for type: {}", currentType);
		}

		// provide builder's copy here.
		exCont = query.getContainer(collectId);
		logger.trace("findCollection. query: {}; collectID: {}", query, collectId);
		ResourceCollection result = new ResourceCollectionImpl(collectionURI, repo, exCont);
		logger.trace("findCollection. returning result: {} for collection ID: {}", result, collectId);
		return result;
	}

	private int getCollectionId(String uri, String baseUri) {
		Schema schema = repo.getSchema();
		if (baseUri != null && !baseUri.isEmpty() && uri.startsWith(baseUri)) {
			uri = uri.substring(baseUri.length());
		}
		Collection cln = schema.getCollection(uri);
		if (cln != null) {
			return cln.getId();
		}
		logger.info("getCollectionId; no collection found for uri: {}; baseUri: {}, collections: {}", uri, baseUri,
				schema.getCollections());
		return ModelManagementBase.WRONG_PATH;
	}

	private Object getValues(Sequence sq) throws XPathException {
		if (sq != null) {
			List<Object> result = new ArrayList<>();
			SequenceIterator itr = sq.iterate();
			while (true) {
				Item item = itr.next();
				if (item == null) {
					break;
				}
				Object o = SaxonUtils.itemToObject(item);
				// logger.trace("getVariable; got item: {}", o);
				result.add(o);
			}
			return result;
		}
		return null;
	}

	private AxisType getAxisType(byte axis) {
		switch (axis) {
		case AxisInfo.ANCESTOR:
			return AxisType.ANCESTOR;
		case AxisInfo.ANCESTOR_OR_SELF:
			return AxisType.ANCESTOR_OR_SELF;
		case AxisInfo.ATTRIBUTE:
			return AxisType.ATTRIBUTE;
		case AxisInfo.CHILD:
			return AxisType.CHILD;
		case AxisInfo.DESCENDANT:
			return AxisType.DESCENDANT;
		case AxisInfo.DESCENDANT_OR_SELF:
			return AxisType.DESCENDANT_OR_SELF;
		case AxisInfo.FOLLOWING:
			return AxisType.FOLLOWING;
		case AxisInfo.FOLLOWING_SIBLING:
			return AxisType.FOLLOWING_SIBLING;
		case AxisInfo.NAMESPACE:
			return AxisType.NAMESPACE;
		case AxisInfo.PARENT:
			return AxisType.PARENT;
		case AxisInfo.PRECEDING:
			return AxisType.PRECEDING;
		case AxisInfo.PRECEDING_OR_ANCESTOR:
			return null; // ??
		case AxisInfo.PRECEDING_SIBLING:
			return AxisType.PRECEDING_SIBLING;
		case AxisInfo.SELF:
			return AxisType.SELF;
		}
		return null;
	}

	private Comparison getComparison(int operator) {
		switch (operator) {
		case Token.AND:	
			return Comparison.AND;
		case Token.OR:
			return Comparison.OR;
		case Token.FEQ:
		case Token.EQUALS:
			return Comparison.EQ;
		case Token.FLE:
		case Token.LE:
			return Comparison.LE;
		case Token.FLT:
		case Token.LT:
			return Comparison.LT;
		case Token.FGE:
		case Token.GE:
			return Comparison.GE;
		case Token.FGT:
		case Token.GT:
			return Comparison.GT;
		default:
			return null;
		}
	}
	
	private void setParentPath(ExpressionBuilder eb, int exIndex, PathBuilder path) {
		com.bagri.core.query.Expression ex = eb.getExpression(exIndex);
		if (ex != null) {
			String old = path.getFullPath();
			path.setPath(ex.getPath());
			logger.trace("setParentPath; path {} switched to: {}; from index: {}", old, path, exIndex);
		}
	}

	private Object[] resolveComparison(Expression var, XPathContext ctx) throws XPathException {
		
		Object[] result = new Object[] {null, null};
		if (var instanceof VariableReference) {
			Binding bind = ((VariableReference) var).getBinding();
			if (bind instanceof LetExpression) {
				Expression e2 = ((LetExpression) bind).getSequence();
				if (e2 instanceof Atomizer) {
					e2 = ((Atomizer) e2).getBaseExpression();
					if (e2 instanceof VariableReference) {
						// paired ref to the e
						result[0] = ((VariableReference) e2).getBinding().getVariableQName().getLocalPart();
					}
				}
			}

			if (result[0] == null) {
				result[0] = bind.getVariableQName().getClarkName();
			}
			// value = getValues(ctx.evaluateLocalVariable(bind.getLocalSlotNumber()));
			try {
				result[1] = getValues(bind.evaluateVariable(ctx));
			} catch (NullPointerException ee) {
				result[1] = null;
			}
		} else if (var instanceof Literal) {
			result[0] = "literal_" + var.getLocation().getLineNumber() + "_" + var.getLocation().getColumnNumber();
			result[1] = getValues(((Literal) var).getValue());
		}
		logger.trace("resolveComparison; returning param: {}, value: {}", result[0], result[1]);
		return result;
	}
	
	private void iterateParams(Expression ex, XPathContext ctx, QueryBuilder query) throws XPathException {

		if (ex instanceof UserFunctionCall) {
			UserFunctionCall ufc = (UserFunctionCall) ex;
			logger.trace("iterateParams; switching to UDF: {}", ufc.getFunctionName());
			ex = ufc.getFunction().getBody();
		}

		Iterator<Operand> itr = ex.operands().iterator();
		while (itr.hasNext()) {
			Expression e = itr.next().getChildExpression();
			iterateParams(e, ctx, query);
		}

		if (ex instanceof ComparisonExpression) { // GeneralComparison20 || ex instanceof ValueComparison) {
			BinaryExpression be = (BinaryExpression) ex;
			Expression le = be.getLhsExpression();

			Comparison compType = getComparison(be.getOperator());
			if (compType == null) {
				logger.debug("iterateParams; can't get comparison from {}", be);
				throw new XPathException("Unknown comparison type for expression: " + be);
			}

			Expression var;
			if (le instanceof VariableReference || le instanceof Literal) {
				compType = Comparison.revert(compType);
				var = le;
			} else {
				var = be.getRhsExpression();
			}

			Object[] refs = resolveComparison(var, ctx);
			String pName = (String) refs[0];
			Object value = refs[1];

			if (value != null) {
				query.setEmptyParam(pName, value);
			}
		}
	
		if (ex instanceof SystemFunctionCall) {
			Comparison compType = null;
			SystemFunctionCall sfc = (SystemFunctionCall) ex;
			if ("starts-with".equals(sfc.getDisplayName())) {
				compType = Comparison.SW;
			} else if ("ends-with".equals(sfc.getDisplayName())) {
				compType = Comparison.EW;
			} else if ("contains".equals(sfc.getDisplayName())) {
				compType = Comparison.CNT;
			} else {
				//
			}
				
			if (compType != null) {
				Object[] refs = resolveComparison(sfc.getOperanda().getOperand(1).getChildExpression(), ctx);
				String pName = (String) refs[0];
				Object value = refs[1];
				logger.trace("iterateParams; got operand: {}, value: {}", pName, value);
				if (value != null) {
					query.setEmptyParam(pName, value);
				}
			}
		}
	
	}
	
	private void iterate(Expression ex, XPathContext ctx, QueryBuilder query, int currentType, PathBuilder path) throws XPathException {
		logger.trace("iterate.start: {}; expression: {}", ex.getClass().getName(), ex);

		if (ex instanceof SystemFunctionCall) {
			SystemFunctionCall clx = (SystemFunctionCall) ex;
			if (clx.isCallOnSystemFunction("collection") || clx.isCallOnSystemFunction("uri-collection")) {
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
				path = new PathBuilder();
				ExpressionContainer exCont = new ExpressionContainer();
				query.addContainer(currentType, exCont);
			}
		}

		if (ex instanceof AxisExpression) {
			AxisExpression ae = (AxisExpression) ex;
			logger.trace("iterate; axis: {}", AxisInfo.axisName[ae.getAxis()]);

			AxisType axis = getAxisType(ae.getAxis());
			String namespace = null;
			String segment = null;
			NodeTest test = ae.getNodeTest();
			if (test != null) {
				int code = test.getFingerprint();
				if (code >= 0) {
					StructuredQName name = ctx.getNamePool().getStructuredQName(code);
					//namespace = repo.getModelManagement().getNamespacePrefix(name.getURI());
					namespace = name.getURI();
					segment = name.getLocalPart();
				} else {
					// case with regex..
					logger.trace("iterate: empty code; test: {}", test);
					// depends on axis...
					segment = "*";
				}
			}
			path.addPathSegment(axis, namespace, segment);
			logger.trace("iterate; added path segment {}:{} for axis: {}; now path is: {}", namespace, segment, axis, path);
		}

		if (ex instanceof AttributeGetter) {
			FingerprintedQName an = ((AttributeGetter) ex).getAttributeName();
			path.addPathSegment(AxisType.ATTRIBUTE, an.getURI(), an.getLocalPart());
		}
		
		int exIdx = -1;
		if (ex instanceof BooleanExpression) {
			Comparison compType = getComparison(((BooleanExpression) ex).getOperator());
			if (compType != null) {
				// if (currentType == collectType) {
				ExpressionContainer exCont = getCurrentContainer(query, currentType);
				exIdx = exCont.addExpression(currentType, compType, path);
				logger.trace("iterate; added {} expression {} at index: {}", compType, exCont, exIdx);
				// }
			} else {
				throw new XPathException("Unknown comparison type for expression: " + ex);
			}
		}

		if (ex instanceof GeneralComparison20) {
			ExpressionContainer exCont = getCurrentContainer(query, currentType);
			//if (exCont.getBuilder().getRoot() != null) {
				int exIndex = exCont.addExpression(currentType, Comparison.AND, path);
				logger.trace("iterate; added explicit AND expression {} at index: {}", exCont, exIndex);
			//}
		}
		
		if (ex instanceof UserFunctionCall) {
			UserFunctionCall ufc = (UserFunctionCall) ex;
			logger.trace("iterate; switching to UDF: {}", ufc.getFunctionName());
			// ex = ufc.getTargetFunction(ctx).getBody();
			ex = ufc.getFunction().getBody();
		}

		for (Operand op: ex.operands()) {
			iterate(op.getChildExpression(), ctx, query, currentType, path);
		}

		if (ex instanceof ComparisonExpression) { // GeneralComparison20 || ex instanceof ValueComparison) {
		//if (ex instanceof GeneralComparison10 || ex instanceof ComparisonExpression) { 
			// || ex instanceof ValueComparison) {
			BinaryExpression be = (BinaryExpression) ex;
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

			Object[] refs = resolveComparison(var, ctx);
			String pName = (String) refs[0];
			Object value = refs[1];

			if (path.getSegments().size() == 0) {
				PathBuilder p = resolveCurrentPath(ex, true);
				ExpressionContainer exCont = getCurrentContainer(query, currentType);
				int exIndex = exCont.addExpression(currentType, compType, p, pName, value);
				logger.trace("iterate; resolved path: {}", p);
			}

			if (path.getSegments().size() > 0) {
				ExpressionContainer exCont = getCurrentContainer(query, currentType);
				exIdx = exCont.addExpression(currentType, compType, path, pName, value);
				logger.trace("iterate; added path expression at index: {}", exIdx);
				setParentPath(exCont.getBuilder(), exIdx, path);
				//path.removeLastSegment();
			}
		}

		if (ex instanceof BooleanExpression) {
			ExpressionContainer exCont = getCurrentContainer(query, currentType);
			setParentPath(exCont.getBuilder(), exIdx, path);
		}
		
		if (ex instanceof SystemFunctionCall) {
			// add FunctionExpression..?
			Comparison compType = null;
			SystemFunctionCall sfc = (SystemFunctionCall) ex;
			if ("starts-with".equals(sfc.getDisplayName())) {
				compType = Comparison.SW;
			} else if ("ends-with".equals(sfc.getDisplayName())) {
				compType = Comparison.EW;
			} else if ("contains".equals(sfc.getDisplayName())) {
				compType = Comparison.CNT;
			} else {
				//
			}
				
			if (compType != null) {
				ExpressionContainer exCont = getCurrentContainer(query, currentType);
				Object[] refs = resolveComparison(sfc.getOperanda().getOperand(1).getChildExpression(), ctx);
				String pName = (String) refs[0];
				Object value = refs[1];
				if (path.getSegments().size() == 0) {
					PathBuilder p = resolveCurrentPath(ex, true);
					int exIndex = exCont.addExpression(currentType, compType, p, pName, value);
					logger.trace("iterate; resolved path: {}", p);
				} else {
					int exIndex = exCont.addExpression(currentType, compType, path, pName, value);
					logger.trace("iterate; added functional path expression at index: {}", exIndex);
					//setParentPath(exCont.getBuilder(), exIndex, path);
				}
			}
		}

		if (ex instanceof Atomizer) {
			Atomizer at = (Atomizer) ex;
			logger.trace("iterate; atomizing: {}", at.getBaseExpression());
			if ((at.getBaseExpression() instanceof BindingReference) ||
				(at.getBaseExpression() instanceof SystemFunctionCall && 
						"map:get".equals(((SystemFunctionCall) at.getBaseExpression()).getDisplayName()))) {
				logger.trace("iterate; got base ref: {}", at.getBaseExpression());
			} else {
				PathSegment ps = path.getLastSegment();
				if (ps != null && ps.getAxis() == AxisType.CHILD) {
					path.addPathSegment(AxisType.CHILD, null, "text()");
					logger.trace("iterate; added text() segment for CHILD in Atomizer");
				}
			}
		}
		
		logger.trace("iterate.end: {}; path: {}", ex.getClass().getName(), path.getFullPath());
	}
	
	private PathBuilder resolveCurrentPath(Expression ex, boolean reset) {
		PathBuilder cp = new PathBuilder();
		gatherGetPaths(ex, cp, reset);
		return cp;
	}

	private void gatherGetPaths(Expression exp, PathBuilder path, boolean reset) {
		if (exp instanceof StringLiteral && exp.getParentExpression() instanceof FunctionCall
			&& "map:get".equals(((FunctionCall) exp.getParentExpression()).getDisplayName())) {
				path.addPathSegment(AxisType.CHILD, null, ((StringLiteral) exp).getStringValue());
		} else if (exp instanceof ContextItemExpression) {
			ContextItemExpression cie = (ContextItemExpression) exp;
			try {
				Field f = cie.getClass().getDeclaredField("staticInfo");
				f.setAccessible(true);
				ContextItemStaticInfo si = (ContextItemStaticInfo) f.get(cie);
				f = si.getClass().getDeclaredField("contextSettingExpression");
				f.setAccessible(true);
				gatherGetPaths((Expression) f.get(si), path, false);
			} catch (Exception e) {
				//
			}
		} else if (exp instanceof LocalVariableReference) { // && reset) {
			Binding bind = ((LocalVariableReference) exp).getBinding();
			PathBuilder pb = null;
			if (bind instanceof Expression) {
				if (!isParentExpression((Expression) bind, exp)) {
					pb = resolveExVariable(bind, (Expression) bind);
				}
			} else {
				pb = resolveExVariable(bind, this.exp.getExpression());
			}
			if (pb != null) {
				for (PathSegment ps: pb.getSegments()) {
					path.addPathSegment(ps.getAxis(), ps.getNamespace(), ps.getSegment());
				}
			//} else {
			//	path.addPathSegment(AxisType.CHILD, null, "$" + bind.getVariableQName().getLocalPart());
			}
				//int slot = ((LocalVariableReference) exp).getSlotNumber();
				//if (bind instanceof LetExpression) {
				//	Expression e2 = ((LetExpression) bind).getSequence();
				//}
		} else {
			for (Operand op: exp.operands()) {
				gatherGetPaths(op.getChildExpression(), path, reset);
			}
		}
    }	

	private boolean isParentExpression(Expression parent, Expression child) {
		if (parent == null) {
			return false;
		}
		if (parent == child) {
			return true;
		}
		return isParentExpression(parent, child.getParentExpression());
	}
	
	private PathBuilder resolveExVariable(Binding bind, Expression ex) {
		PathBuilder pb = null;
		if (ex instanceof Assignation) {
			Assignation as = (Assignation) ex;
			if (as.hasVariableBinding(bind)) {
				return resolveCurrentPath(ex, false);
			}
		} else if (ex instanceof FLWORExpression) {
			FLWORExpression flwor = (FLWORExpression) ex;
			StructuredQName var = bind.getVariableQName();
			for (Clause cls: flwor.clauses) {
				LocalVariableBinding[] lvbs = cls.getRangeVariables();
				for (LocalVariableBinding lvb: lvbs) {
					if (var.equals(lvb.getVariableQName())) {
						switch (cls.getClauseKey()) {
							case Clause.FOR: {
								pb = resolveCurrentPath(((ForClause) cls).getSequence(), false);
								break;
							}
							case Clause.LET: {
								pb = resolveCurrentPath(((LetClause) cls).getSequence(), false);
								break;
							}
							case Clause.WHERE: {
								//
							}
							default: continue;
						}
					}
				}
				if (pb != null && !pb.getSegments().isEmpty()) {
					return pb;
				}
			}
		}
		for (Operand op: ex.operands()) {
			pb = resolveExVariable(bind, op.getChildExpression());
			if (pb != null && !pb.getSegments().isEmpty()) {
				return pb;
			}
		}
		return null;
	}
		
	private ExpressionContainer getCurrentContainer(QueryBuilder query, int currentType) {
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


/*
		if (ex instanceof SystemFunctionCall) {
			SystemFunctionCall sfc = (SystemFunctionCall) ex;
			if ("map:get".equals(sfc.getDisplayName())) {
				if (isParentComparison(ex)) {
					Expression arg = sfc.getArg(1);
					if (arg instanceof StringLiteral) {
						String namespace = null;
						String segment = ((StringLiteral) arg).getStringValue();
						AxisType axis = AxisType.CHILD;
						if (segment.startsWith("@")) {
							axis = AxisType.ATTRIBUTE;
							segment = segment.substring(1);
						} else if (segment.startsWith("#")) {
							axis = AxisType.NAMESPACE;
							segment = segment.substring(1);
						}
						path.addPathSegment(axis, namespace, segment);
						logger.trace("iterate; added path segment {}:{} for axis: {} in IFC {}", namespace, segment, axis, ifc.getDisplayName());
					} else {
						// the arg can be instanceof VariableReference pointing to array of literals. Can it be used in comparison condition..?
					}
				}
			}
		}
		
		if (ex instanceof FLWORExpression) {
			FLWORExpression flwor = (FLWORExpression) ex;
			ExpressionContainer exCont = getCurrentContainer();
			for (com.bagri.core.query.Expression e: exCont.getBuilder().getExpressions()) {
				checkExpressionPath(e, flwor);
			}
		}


	private boolean isParentComparison(Expression ex) {
		while (ex != null) {
			if (ex instanceof GeneralComparison10 || ex instanceof ComparisonExpression || ex instanceof FilterExpression) { 
				return true;
			} else if (ex instanceof FLWORExpression) {
				FLWORExpression fex = (FLWORExpression) ex;
				for (Clause cls: fex.getClauseList()) {
					if (cls instanceof ForClause) {
						if (((ForClause) cls).getSequence() instanceof FilterExpression) {
							return true;
						}
					} else if (cls instanceof WhereClause) {
						return true;
					}
				}
			}
			ex = ex.getParentExpression();
		} 
		return false;
	}


	private void checkExpressionPath(com.bagri.core.query.Expression ex, FLWORExpression flwor) {
		do {
			String path = ex.getPath().getFullPath();
			if (path.contains("/$")) {
				fixExpressionPath(ex, flwor);
			} else {
				break;
			}
		}
		while (true);
	}
	
	private void fixExpressionPath(com.bagri.core.query.Expression ex, FLWORExpression flwor) {
		for (int i=0; i < ex.getPath().getSegments().size(); i++) {
			PathSegment ps = ex.getPath().getSegments().get(i);
			String seg = ps.getSegment();
			if (seg.startsWith("$")) {
				String var = seg.substring(1);
				for (Clause cls: flwor.getClauseList()) {
					LocalVariableBinding[] lvbs = cls.getRangeVariables();
					for (LocalVariableBinding lvb: lvbs) {
						if (var.equals(lvb.getVariableQName().getLocalPart())) {
							PathBuilder pb;
							switch (cls.getClauseKey()) {
								case Clause.FOR: {
									pb = resolveCurrentPath(((ForClause) cls).getSequence());
									break;
								}
								case Clause.LET: {
									pb = resolveCurrentPath(((LetClause) cls).getSequence());
									break;
								}
								case Clause.WHERE: {
									//
								}
								default: continue;
							}
							PathBuilder pa = new PathBuilder();
							for (int j=0; j < i; j++) {
								ps = ex.getPath().getSegments().get(j);
								pa.addPathSegment(ps.getAxis(), ps.getNamespace(), ps.getSegment());
							}
							for (int j=0; j < pb.getSegments().size(); j++) {
								ps = pb.getSegments().get(j);
								pa.addPathSegment(ps.getAxis(), ps.getNamespace(), ps.getSegment());
							}
							for (int j=i+1; j < ex.getPath().getSegments().size(); j++) {
								ps = ex.getPath().getSegments().get(j);
								pa.addPathSegment(ps.getAxis(), ps.getNamespace(), ps.getSegment());
							}
							logger.debug("fixExpressionPath; got fixed path: {}", pa); 
							ex.setPath(pa);
							return;
						}
					}
				}
			}
		}
	}
	
*/