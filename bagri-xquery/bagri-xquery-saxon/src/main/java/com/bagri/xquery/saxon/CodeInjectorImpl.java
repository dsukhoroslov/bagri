package com.bagri.xquery.saxon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.flwor.Clause;
import net.sf.saxon.expr.parser.CodeInjector;
import net.sf.saxon.om.StructuredQName;

public class CodeInjectorImpl implements CodeInjector {

	private static final Logger logger = LoggerFactory.getLogger(CodeInjectorImpl.class);

	@Override
	public Expression inject(Expression exp, StaticContext env, int construct, StructuredQName qName) {
		logger.debug("inject; exp: {}; env: {}; construct: {}; qName: {}", exp, env, construct, qName);
		return exp;
	}

	@Override
	public Clause injectClause(Clause target, StaticContext env) {
		logger.debug("injectClause; traget: {}; env: {}", target, env);
		return target;
	}

}
