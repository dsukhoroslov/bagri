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

import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.BinaryExpression;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.BooleanExpression;
import net.sf.saxon.expr.ComparisonExpression;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.FunctionCall;
import net.sf.saxon.expr.GeneralComparison10;
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
import net.sf.saxon.expr.flwor.WhereClause;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.Token;
import net.sf.saxon.expr.parser.ExpressionTool.ExpressionPredicate;
import net.sf.saxon.functions.IntegratedFunctionCall;
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
	// private int collectType;
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
			currentType = collectId; // 0;
			logger.trace("findCollection. got collection type: {} for uri: {}", collectId, collectionURI);
		}

		if (query == null) {
			query = new QueryBuilder();
			currentPath = new PathBuilder();
			// query.addContainer(currentType, new ExpressionContainer());
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

	private void setParentPath(ExpressionBuilder eb, int exIndex, PathBuilder path) {
		com.bagri.core.query.Expression ex = eb.getExpression(exIndex);
		if (ex != null) {
			path.setPath(ex.getPath());
			logger.trace("setParentPath; path switched to: {}; from index: {}", path, exIndex);
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
			logger.trace("resolveComparison; got reference: {}, value: {}", result[0], result[1]);
		} else if (var instanceof StringLiteral) {
			result[1] = ((StringLiteral) var).getStringValue();
		} else if (var instanceof Literal) {
			result[1] = getValues(((Literal) var).getValue());
		}
		return result;
	}
	
	private void iterateParams(Expression ex, XPathContext ctx) throws XPathException {

		// if (ex instanceof Block) {
		// return;
		// }

		Iterator<Operand> itr = ex.operands().iterator();
		while (itr.hasNext()) {
			Expression e = itr.next().getChildExpression();
			iterateParams(e, ctx);
		}

		if (ex instanceof GeneralComparison10 || ex instanceof ComparisonExpression) { // GeneralComparison20
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

				try {
					value = getValues(bind.evaluateVariable(ctx));
				} catch (NullPointerException ee) {
					value = null;
				}
				// } else if (var instanceof StringLiteral) {
				// value = ((StringLiteral) var).getStringValue();
				// } else if (var instanceof Literal) {
				// value = getValues(((Literal) var).getValue());
			}

			logger.trace("iterateParams; got reference: {}, value: {}", pName, value);
			if (pName != null && value != null) {
				query.setEmptyParam(pName, value);
			}
		}
	}
	
	private void iterate(Expression ex, XPathContext ctx) throws XPathException {
		logger.trace("iterate.start: {}; expression: {}", ex.getClass().getName(), ex);

		PathBuilder path = currentPath;
		// if (ex instanceof Block) {
		// logger.trace("end: {}; path: {}", ex.getClass().getName(),
		// path.getFullPath());
		// return;
		// }

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
				currentPath = new PathBuilder();
				path = currentPath;
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

		int exIndex = -1;
		if (ex instanceof BooleanExpression) {
			Comparison compType = getComparison(((BooleanExpression) ex).getOperator());
			if (compType != null) {
				// if (currentType == collectType) {
				ExpressionContainer exCont = getCurrentContainer();
				exIndex = exCont.addExpression(currentType, compType, path);
				logger.trace("iterate; added {} expression {} at index: {}", compType, exCont, exIndex);
				// }
			} else {
				throw new XPathException("Unknown comparison type for expression: " + ex);
			}
		}

		if (ex instanceof GeneralComparison20) {
			ExpressionContainer exCont = getCurrentContainer();
			//if (exCont.getBuilder().getRoot() != null) {
				exIndex = exCont.addExpression(currentType, Comparison.AND, path);
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
			iterate(op.getChildExpression(), ctx);
		}

		if (ex instanceof GeneralComparison10 || ex instanceof ComparisonExpression) { 
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
				PathBuilder p = resolveCurrentPath(ex, ctx);
				ExpressionContainer exCont = getCurrentContainer();
				exIndex = exCont.addExpression(currentType, compType, p, pName, value);
				logger.trace("iterate; resolved path: {}", p);
			}

			if (path.getSegments().size() > 0) {
				ExpressionContainer exCont = getCurrentContainer();
				exIndex = exCont.addExpression(currentType, compType, path, pName, value);
				logger.trace("iterate; added path expression at index: {}", exIndex);
				setParentPath(exCont.getBuilder(), exIndex, path);
				logger.trace("iterate; parent path {} set at index: {}", path, exIndex);
				//path.removeLastSegment();
			}
		}

		if (ex instanceof BooleanExpression) {
			ExpressionContainer exCont = getCurrentContainer();
			setParentPath(exCont.getBuilder(), exIndex, path);
			logger.trace("iterate; parent path {} set at index: {}", path, exIndex);
		}
/*
		if (ex instanceof IntegratedFunctionCall) {
			IntegratedFunctionCall ifc = (IntegratedFunctionCall) ex;
			if ("map:get".equals(ifc.getDisplayName())) {
				if (isParentComparison(ex)) {
					Expression arg = ifc.getArg(1);
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
*/		
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
				ExpressionContainer exCont = getCurrentContainer();
				Object[] refs = resolveComparison(sfc.getOperanda().getOperand(1).getChildExpression(), ctx);
				String pName = (String) refs[0];
				Object value = refs[1];
				if (path.getSegments().size() == 0) {
					PathBuilder p = resolveCurrentPath(ex, ctx);
					exIndex = exCont.addExpression(currentType, compType, p, pName, value);
					logger.trace("iterate; resolved path: {}", p);
				} else {
					exIndex = exCont.addExpression(currentType, compType, path, pName, value);
					logger.trace("iterate; added functional path expression at index: {}", exIndex);
					//setParentPath(exCont.getBuilder(), exIndex, path);
					//logger.trace("iterate; parent path {} set at index: {}", path, exIndex);
				}
			}
			//logger.trace("iterate; removing last segment after string comparison call; current path: {}", path.getFullPath());
			//path.removeLastSegment();
		}

		if (ex instanceof Atomizer) {
			Atomizer at = (Atomizer) ex;
			logger.trace("iterate; atomizing: {}", at.getBaseExpression());
			if ((at.getBaseExpression() instanceof BindingReference) ||
				(at.getBaseExpression() instanceof IntegratedFunctionCall && 
						"map:get".equals(((IntegratedFunctionCall) at.getBaseExpression()).getDisplayName()))) {
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
	
	private PathBuilder resolveCurrentPath(Expression ex, XPathContext ctx) {
		PathBuilder cp = new PathBuilder();
		gatherGetPaths(ex, cp, ctx);
		return cp;
	}

	private void gatherGetPaths(Expression exp, PathBuilder path, XPathContext ctx) {
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
				gatherGetPaths((Expression) f.get(si), path, ctx);
			} catch (Exception e) {
				//
			}
		} else if (exp instanceof LocalVariableReference) {
			Binding bind = ((LocalVariableReference) exp).getBinding();
			path.addPathSegment(AxisType.CHILD, null, bind.getVariableQName().getLocalPart());
		} else {
			for (Operand op: exp.operands()) {
				gatherGetPaths(op.getChildExpression(), path, ctx);
			}
		}
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
